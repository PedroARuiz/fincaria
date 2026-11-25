# Load test — farms listing

A basic [k6](https://k6.io/) load test for the authenticated farms-listing
endpoint `GET /api/v1/farms`, used to record a baseline for Milestone 7 and to
measure the before/after impact of the N+1 fix.

## Files

- `farms-listing.js` — the k6 scenario: log in once, then hit the first page of
  the listing with a constant pool of virtual users.
- `seed.sh` — creates the load-test user and bulk-inserts a large number of
  farms so the listing is big enough for the N+1 to show.

## Prerequisites

- The local stack is up: `docker compose up -d` (Postgres + Redis at least).
- The app is running on the host: `./gradlew bootRun`.
- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) is installed
  (`brew install k6` on macOS).

## Running it

```bash
# 1. Seed the data (defaults to 500 farms for loadtest@fincaria.local).
./load-test/seed.sh

# 2. Run the load test (defaults to 20 VUs for 30s against localhost:8080).
k6 run load-test/farms-listing.js
```

Both scripts are configurable through environment variables — see the comment
headers in each file (`BASE_URL`, `FARM_COUNT`, `VUS`, `DURATION`, `PAGE`,
`SIZE`, …). For example, a heavier run against a bigger dataset:

```bash
FARM_COUNT=2000 ./load-test/seed.sh
VUS=50 DURATION=1m k6 run load-test/farms-listing.js
```

## Observing it

The request flows through the same instrumentation as any other, so the local
Grafana `fincaria-http` dashboard shows the listing's latency and error rate
live while the test runs (Grafana at <http://localhost:3000>, Prometheus scrapes
the app at `host.docker.internal:8080`).

## Results

A documented before/after comparison of the N+1 fix, run with this harness,
lives in
[`docs/performance/farms-listing-load-test.md`](../docs/performance/farms-listing-load-test.md).
