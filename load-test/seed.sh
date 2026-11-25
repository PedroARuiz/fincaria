#!/usr/bin/env bash
set -euo pipefail

# Seeds a load-test user and a large set of farms for the farms-listing k6 test
# (farms-listing.js).
#
# The user is created through the API so its bcrypt password hash matches the
# real login flow. The farms are bulk-inserted straight into Postgres: minting
# thousands of checksum-valid cadastral references through the API would
# dominate the setup time and is irrelevant to what the listing load test
# measures. The listing query count — and therefore the N+1 vs. batch-fetch
# difference — is unaffected by how the rows got there.
#
# Re-running is safe: it recreates the load-test user's farms from scratch.
#
# Tunable via environment variables (all optional):
#   BASE_URL            API base URL         (default http://localhost:8080)
#   LOAD_TEST_EMAIL     user email           (default loadtest@fincaria.local)
#   LOAD_TEST_PASSWORD  user password        (default loadtest-password)
#   FARM_COUNT          farms to create      (default 500)

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${LOAD_TEST_EMAIL:-loadtest@fincaria.local}"
PASSWORD="${LOAD_TEST_PASSWORD:-loadtest-password}"
FARM_COUNT="${FARM_COUNT:-500}"

# Postgres service + credentials, matching docker-compose.yml.
DB_SERVICE="${DB_SERVICE:-db}"
DB_USER="${DB_USER:-fincaria_local_user}"
DB_NAME="${DB_NAME:-fincaria_local_db}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

psql_exec() {
  docker compose exec -T "$DB_SERVICE" psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" "$@"
}

echo "==> Registering load-test user ($EMAIL) via the API"
status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE_URL/api/v1/users" \
  -H 'Content-Type: application/json' \
  --data-binary "$(printf '{"name":"Load Test","email":"%s","password":"%s"}' "$EMAIL" "$PASSWORD")")

case "$status" in
  201) echo "    created" ;;
  409) echo "    already exists, reusing it" ;;
  *) echo "    unexpected response from POST /api/v1/users: HTTP $status" >&2; exit 1 ;;
esac

user_id="$(psql_exec -tA -c "SELECT id FROM users WHERE email = '$EMAIL'")"
if [[ -z "$user_id" ]]; then
  echo "could not resolve the load-test user id from the database" >&2
  exit 1
fi
echo "    user id: $user_id"

echo "==> Reseeding $FARM_COUNT farms for the load-test user"
psql_exec <<SQL
BEGIN;
DELETE FROM farm_crop_types WHERE farm_id IN (SELECT id FROM farms WHERE user_id = '$user_id');
DELETE FROM farms WHERE user_id = '$user_id';

-- The listing endpoint maps each row back to the CadastralReference domain
-- value object, which validates the Spanish Catastro checksum. So the seeded
-- references must be checksum-valid, not just 20 chars. These two session-local
-- (pg_temp) functions reproduce the exact algorithm from CadastralReference.kt
-- for an 18-digit body (digits only, so a character's value is the digit
-- itself) plus the two trailing check digits.
CREATE FUNCTION pg_temp.check_digit(fragment text) RETURNS text AS \$\$
DECLARE
  weights int[] := ARRAY[13, 15, 12, 5, 4, 17, 9, 21, 3, 7, 1];
  alphabet text := 'MQWERTYUIOPASDFGHJKLBZX';
  weighted_sum int := 0;
  i int;
BEGIN
  FOR i IN 1..11 LOOP
    weighted_sum := weighted_sum + weights[i] * substr(fragment, i, 1)::int;
  END LOOP;
  RETURN substr(alphabet, (weighted_sum % 23) + 1, 1);
END;
\$\$ LANGUAGE plpgsql IMMUTABLE;

CREATE FUNCTION pg_temp.cadastral_reference(seq bigint) RETURNS text AS \$\$
DECLARE
  body text := lpad(seq::text, 18, '0');
BEGIN
  -- substr is 1-indexed; these ranges mirror the Kotlin substrings
  -- [0,7)+[14,18) and [7,14)+[14,18).
  RETURN body
    || pg_temp.check_digit(substr(body, 1, 7) || substr(body, 15, 4))
    || pg_temp.check_digit(substr(body, 8, 7) || substr(body, 15, 4));
END;
\$\$ LANGUAGE plpgsql IMMUTABLE;

INSERT INTO farms (id, user_id, cadastral_reference, verification_status)
SELECT gen_random_uuid(), '$user_id', pg_temp.cadastral_reference(g), 'UNVERIFIED'
FROM generate_series(1, $FARM_COUNT) AS g;

INSERT INTO farm_crop_types (farm_id, crop_type)
SELECT id, 'OLIVE' FROM farms WHERE user_id = '$user_id';
COMMIT;
SQL

total="$(psql_exec -tA -c "SELECT count(*) FROM farms WHERE user_id = '$user_id'")"
echo "==> Done. Load-test user now owns $total farms."
echo "    Run the test with: k6 run $SCRIPT_DIR/farms-listing.js"
