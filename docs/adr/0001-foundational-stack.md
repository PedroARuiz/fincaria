# 0001. Foundational stack: Kotlin, Spring Boot and Gradle

## Status
Accepted

## Context

The author has 9 years of professional experience as an Android developer using Kotlin and wants to transition their career toward backend development. Kotlin + Spring Boot covers a large part of the current backend job market, which makes it a pragmatic stack to target for this transition.

To build real, hands-on backend engineering skills, every foundational decision in this project — stack, architecture, tooling — is made and understood by the author themselves, using AI strictly as a mentor/reviewer rather than an implementer.

A concrete technology stack needs to be chosen before any code is written.

## Decision

- **Kotlin** as the programming language, building on 9 years of existing Kotlin experience from Android development.
- **Spring Boot** as the backend framework, given how widely it is used across the current Kotlin backend job market.
- **Gradle**, using the Kotlin DSL (`build.gradle.kts`), as the build tool — reusing existing Gradle knowledge from Android development and keeping build scripts in the same language as the rest of the codebase.

## Alternatives considered

- **Language**: Kotlin was the obvious choice given the author's existing background; no serious alternative was considered.
- **Framework**: Ktor was considered, but discarded because the backend job market predominantly demands Spring Boot experience.
- **Build tool**: Maven was not considered, since the author already had prior working knowledge of Gradle.

## Consequences

- Existing Kotlin and Gradle knowledge is directly reusable, speeding up the learning curve on those parts.
- Spring Boot maximizes employability, being the most in-demand framework in this niche.
- Spring Boot has its own, fairly steep learning curve (IoC, auto-configuration, a broad ecosystem) that doesn't carry over from Android experience. A lighter framework like Ktor would have been easier to pick up, at the cost of weaker job-market alignment.
