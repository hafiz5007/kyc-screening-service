# Architecture notes

## Why this shape

A KYC screen has three moving parts: a **coarse filter** (regex over an indexed field) that narrows a multi-million-row list to a handful of candidates, an **explainable scorer** that turns each candidate into a number + reasons, and a **decision rule** that maps the scored set to CLEAR / REVIEW / HIT. Splitting them out means each can be swapped or tuned independently — swap Mongo for OpenSearch, swap Jaro-Winkler for a bespoke phonetic algorithm, tighten the HIT threshold from 0.90 to 0.95 — without disturbing the rest.

## Data flow

1. Request lands in `ScreeningController`, gets a correlation id from `RequestLoggingFilter`, and is validated by Jakarta Bean Validation.
2. `ScreeningService.screen` normalises the subject name (`NameNormalizer`) and builds a **coarse regex filter** joined from every token >= 3 chars.
3. Two indexed Mongo queries fetch candidate sanction and PEP rows against `normalisedName`.
4. Each candidate is passed to `MatchScorer`, which returns `{score, matchedName, reasons}`.
5. Candidates below `request.minScore` are dropped. The rest are grouped by source, capped, sorted.
6. `decide()` picks CLEAR / REVIEW / HIT based on the top scores.
7. Result is returned; a structured log line captures the decision with the correlation id.

## Scoring

The primary signal is **Jaro-Winkler** similarity over normalised tokens, calculated twice — once in the original order and once after sorting tokens alphabetically. The maximum is taken. This lets "John Smith" match "Smith, John" without penalising exact-order matches. Aliases are considered alongside the primary name; the best-scoring name wins.

DOB and country of residence give small additive adjustments (+0.10 for exact DOB, +0.05 for matching country, -0.10 for DOB > 5 years apart). These are deliberately small — a strong name match should not be rejected because DOB is missing, and a weak name match should not be promoted purely on DOB.

Every non-zero component pushes a human-readable string into `reasons`, so a compliance analyst can look at a HIT and know **why**.

## Ingestion

`IngestJob` is the shared interface. Each source has its own implementation. Job runs are **idempotent**: they upsert by `sourceId` so re-running against the same feed produces no duplicates. In production the ingesters are triggered by:

- A daily scheduled task (via `@Scheduled` or an external scheduler)
- The dev-only `/api/v1/dev/ingest/*` endpoints for manual smoke tests

Failures per row don't stop the job. Each failure is captured with source file, line number, and raw payload so a re-run can pick up only the failed rows.

## Security posture

- All `/api/v1/**` endpoints require a valid JWT via `spring-boot-starter-oauth2-resource-server`. The issuer URI is env-driven.
- `dev` profile disables the resource server so you can hit the API with `curl` without an IdP.
- MongoDB URI is env-driven; never checked in.
- Container runs as non-root (`USER app` in the Dockerfile).
- No PII is persisted beyond what's already public in the reference lists.

## Observability

- **Logs** — SLF4J + Logback to stdout with `%X{cid}` MDC pattern → correlation id shows up in every line of a request.
- **Metrics** — Micrometer + Prometheus registry, `/actuator/prometheus`. Per-endpoint HTTP timers come for free.
- **Health** — Actuator `/actuator/health/liveness` and `/actuator/health/readiness` are the K8s probes.

## Trade-offs worth calling out

- **Regex `$options: 'i'` is not the fastest coarse filter.** For a demo on a laptop it's fine. In production you'd swap it for an OpenSearch or Mongo `$text` index and use the score as an additional weighting signal. The service interface doesn't change.
- **Jaro-Winkler is Latin-alphabet-friendly, less so for CJK.** A production system would layer language-specific normalisation (romanisation, phonetic keys) before scoring.
- **PEP data changes frequently and has short-lived positions.** In production you'd store a snapshot with `positionStart` / `positionEnd` and mask the record from queries once positions expire beyond a configurable window.
