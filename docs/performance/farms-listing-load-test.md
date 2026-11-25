# Farms listing — load test & N+1 before/after

This is the documented before/after performance comparison for Milestone 7. It
measures the impact of the farms-listing N+1 fix
(commit `6b9e283`, _"Fix the farms listing N+1 with DB pagination and batch
fetching"_) on the authenticated endpoint `GET /api/v1/farms` under load.

The harness lives in [`load-test/`](../../load-test/) (a k6 scenario plus a
seeding script); see its [README](../../load-test/README.md) to reproduce these
numbers.

## What changed

Before the fix, listing farms loaded **every** farm owned by the user into
memory and then filtered, sorted and sliced the page in the application. Mapping
each row to the domain also triggered a lazy load of its `crop_types`
collection — one extra query per farm. So a request for a single 20-row page of
a user with 500 farms cost `1 + 500` queries and materialised all 500 rows.

After the fix, pagination, the crop-type filter and sorting run in the database
(`Page<FarmJpaEntity>`), and the `crop_types` collection is batch-loaded
(`@BatchSize(50)`). A page now costs a small, constant number of queries
regardless of how many farms the user owns, and only the page's rows are
materialised. The query-count regression test
(`FarmListingQueryCountTest`) pins this down: before, 2 farms = 3 statements and
6 farms = 7 (the `1 + N` curve); after, both = 2.

This load test confirms that the query-count win translates into a latency and
throughput win end-to-end.

## Method

- **Tool:** [k6](https://k6.io/) `v2.4.0`.
- **Scenario:** 20 constant virtual users for 30s, each repeatedly requesting
  the first page (`GET /api/v1/farms?page=0&size=20`) with a bearer token
  obtained once at setup. (`load-test/farms-listing.js`.)
- **Dataset:** a single user owning **500 farms**, seeded with
  `load-test/seed.sh`.
- **Environment:** app via `./gradlew bootRun` on the host, Postgres and Redis
  from `docker-compose.yml`, all on one local machine (Apple Silicon). Absolute
  numbers are machine-specific and not a production SLO — the **ratio** between
  the two runs is the result.
- **A/B:** the *after* run was taken on `6b9e283` (HEAD of
  `feature/crop-type-cache`); the *before* run on its parent `ff60c52`, with the
  identical seeded dataset and the same k6 invocation.

## Results

| Metric (GET /api/v1/farms) | Before (N+1) | After (fix) | Improvement |
| --- | ---: | ---: | ---: |
| Throughput | 45.98 req/s | 4031.55 req/s | **~88× higher** |
| Total requests in 30s | 1,400 | 121,346 | ~87× more |
| Latency — avg | 431.64 ms | 4.89 ms | **~88× lower** |
| Latency — median | 430.35 ms | 4.75 ms | ~91× lower |
| Latency — p90 | 451.08 ms | 6.58 ms | ~69× lower |
| Latency — p95 | 457.71 ms | 7.33 ms | **~62× lower** |
| Latency — max | 490.00 ms | 50.80 ms | ~10× lower |
| Failed requests | 0.00% | 0.00% | — |

Both runs returned 100% HTTP 200, so this is a pure throughput/latency
difference, not errors under load.

## Interpretation

- **The N+1 dominated the response time.** At 500 farms the before version spent
  nearly all its time issuing ~501 queries and building 500 objects per request,
  pushing p95 to ~458ms. The after version serves the same page in single-digit
  milliseconds.
- **The gap grows with the data.** The before cost scales with the user's total
  farm count; the after cost is bounded by the page size. At 500 farms the
  throughput is ~88× higher — at 5,000 it would be worse still for the before
  version, while the after version stays flat. This is exactly the N+1 failure
  mode: fine with toy data, pathological in production.
- **Caching is orthogonal here.** The Redis cache added in this milestone backs
  the crop-type catalog, not the per-user farms listing (which is neither
  read-only nor shared across users), so both runs hit the database for the
  listing. The listing win comes entirely from the query strategy.

## Reproducing

```bash
docker compose up -d
./gradlew bootRun            # in another shell
./load-test/seed.sh          # seed 500 farms for the load-test user
k6 run load-test/farms-listing.js
```

To reproduce the *before* numbers, repeat the `k6 run` against a checkout of
`ff60c52` (the seeded data and schema are identical, so no reseed is needed).
