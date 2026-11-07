# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

AI漫剧内容管理与分发平台: a Spring Cloud microservice backend for managing and distributing AI comic-drama content. Business code, comments, and domain terms are Chinese; keep new comments in Chinese when editing existing files.

Stack: Java 11, Spring Boot 2.7.2, Spring Cloud 2021.0.3, Spring Cloud Alibaba (Nacos discovery/config), OpenFeign, MyBatis-Plus 3.4.3, MySQL, Redis/Redisson, RabbitMQ, Elasticsearch 7.12, Seata, XXL-Job, Knife4j/Swagger, Tencent COS/VOD. There is no CI, lint/format config, or Maven wrapper.

Package/class/service/table names are comic-domain, but some persisted Java fields still use older column names (`courseId`, `cataId`, `subjectId`, `teacherId`, `lessonId`, `sectionId`) so the existing schema stays compatible. Do not change those persistence names without a coordinated schema migration.

## Build and run

The POM targets Java 11, but `mvn` on this machine defaults to JDK 8. Set `JAVA_HOME` to a JDK 11+ installation before any Maven command, for example `export JAVA_HOME="D:/Development-Tools/jdk-11"` in Git Bash.

- Full build: `mvn -DskipTests clean install`
- Test compile: `mvn -DskipTests test-compile`
- One service with its dependencies: `mvn -pl comic -am -DskipTests install`
- Run a service: `mvn -pl comic spring-boot:run`, or `java -jar comic/target/ac-comic.jar`
- All tests: `mvn test`; one class: `mvn -pl unlock test -Dtest=CodeUtilTest`; one method: `mvn -pl unlock test -Dtest=CodeUtilTest#method`
- Local profile: `--spring.profiles.active=local` (changes the registered discovery IP; `dev` is the default)

Runtime config comes from Nacos at `192.168.150.101:8848` (namespace `f923fb34-cb0a-4c06-8fca-ad61ea61a3f0`). Only `bootstrap.yml` and `bootstrap-{dev,local}.yml` are in the repo; MySQL/Redis/RabbitMQ/ES/XXL-Job/Seata settings and the `shared-*.yaml` files live in Nacos. There is no `application.yml`. Running services or `@SpringBootTest` requires this external infrastructure.

## Service map

| directory | application name | port | DB (`ac.jdbc.database`) | gateway prefix | responsibility |
|---|---|---|---|---|---|
| `gateway` | gateway-service | 10010 | — | — | routing, JWT auth, request ID |
| `auth/ac-auth-service` | auth-service | 8081 | aicomic_auth | `/as` | login/JWT, roles, menus, privileges |
| `user` | user-service | 8082 | aicomic_user | `/us` | users, viewers, creators, admins |
| `search` | search-service | 8083 | aicomic_search | `/ss` | Elasticsearch comic search, interests |
| `media` | media-service | 8084 | aicomic_media | `/ms` | video/image/material upload, playback signatures |
| `message/ac-message-service` | message-service | 8085 | aicomic_message | `/sms` | SMS, notices, inbox |
| `comic` | comic-service | 8086 | aicomic_comic | `/mc` | comics, episodes, categories, tags |
| `pay/ac-pay-service` | pay-service | 8087 | aicomic_pay | `/ps` | Alipay/WeChat pay/refund, payment notify |
| `order` | order-service | 8088 | aicomic_order | `/os` | cart, orders, refund applications |
| `quiz` | quiz-service | 8089 | aicomic_quiz | `/qz` | interactive questions |
| `watch` | watch-service | 8090 | aicomic_watch | `/ws` | watch progress, notes, comments, points, check-in |
| `comment` | comment-service | 8091 | aicomic_comment | `/cm` | like records |
| `unlock` | unlock-service | 8092 | aicomic_unlock | `/ul` | limited-time unlock, coupons, exchange codes |
| `data` | data-service | 8093 | aicomic_data | `/ds` | dashboard statistics |

Gateway routes (`gateway/src/main/resources/bootstrap.yml`) map a first path segment to a load-balanced service with `StripPrefix=1`. The legacy `/ts` alias route was removed; order traffic uses `/os`.

## Request, response, and auth flow

1. Every gateway request goes through `RequestIdRelayFilter`: generates `requestId`, stores it in MDC, adds the `requestId` and `x-request-from: gateway` headers (the latter is skipped for `/ps/notify` so payment callbacks are not treated as gateway traffic).
2. `AccountAuthFilter` skips configured public paths; otherwise it parses the `authorization` JWT (RS256, public key fetched from auth-service JWKS via `JwtSignerHolder`), adds a `user-info: <userId>` header when logged in, then checks `METHOD:/path` against privileges cached in the Redis hash `auth:privileges` (version polled every 20s by `AuthUtil.refreshTask`).
3. Downstream services use `UserInfoInterceptor` to load `user-info` into the `UserContext` ThreadLocal; `LoginAuthInterceptor` rejects requests lacking a user for services/paths with `ac.auth.resource.enable=true`. `FeignRelayUserInterceptor` forwards `user-info` on outgoing Feign calls, so identity propagates across services.
4. Login is handled by auth-service (`/accounts/login`), which calls user-service over Feign to check credentials, then issues a short-lived access token plus an HttpOnly refresh cookie. There is no Spring Security; authorization is the gateway privilege check only.
5. Responses use `R<T>` (`code`, `msg`, `data`, `requestId`). Controllers return plain DTO/VO/domain objects, not `R`: `WrapperResponseBodyAdvice` wraps them only for gateway-originated requests (`x-request-from: gateway`). Internal Feign calls receive the raw object. Do not wrap controller return values manually, and remember a change to a controller return type affects both the HTTP API and Feign consumers.
6. `CommonExceptionAdvice` maps business exceptions (`CommonException` subclasses such as `BadRequestException`, `ForbiddenException`) and validation/Feign failures into `R` with the appropriate status.

## Shared building blocks

- `common` (`ac-common`) — auto-configured through `META-INF/spring.factories`: Jackson (date/time), response wrapping, exception advice, `RequestIdFilter`, MyBatis-Plus (pagination max 200, create/update auto-fill), Redisson with the `@Lock` annotation (SpEL lock names), RabbitMQ helpers (`RabbitMqHelper` with delayed messages and `error.topic` republishing), Knife4j, XXL-Job. Also holds `R`, `PageQuery`, enums, exceptions, and most utilities.
- `api` (`ac-api`) — cross-service DTOs and Feign clients under `com.aicomic.api.client.*`, DTOs under `com.aicomic.api.dto.*`, Caffeine caches (`CategoryCache`, `RoleCache`), and Feign fallbacks. `RequestIdRelayConfiguration` enables Feign scanning and adds `requestId` + `x-request-from: feign` to every Feign call.
- `auth/ac-auth-common` — JWT constants and `PrivilegeRoleDTO`.
- `auth/ac-auth-gateway-sdk` — gateway-side `AuthUtil`/`JwtSignerHolder` (used by `gateway`).
- `auth/ac-auth-resource-sdk` — servlet-side interceptors plus `ac.auth.resource.*` properties (`enable`, `includeLoginPaths`, `excludeLoginPaths`). Check each service's `bootstrap.yml`: `comic` and `quiz` currently set `enable: false`, while other services enable it with per-service include/exclude paths.
- `message/ac-message-api` and `pay/ac-pay-api` — service-local API modules (each with a `*-domain`) imported directly by consumers (`user` uses message-api, `order` uses pay-api) instead of the central `ac-api`.

## Async and background processing

- RabbitMQ: exchange/routing-key constants live in `common/constants/MqConstants` (`comic.topic`, `order.topic`, `watch.topic`, `pay.topic`, `unlock.topic`, `sms.direct`, `like.record.topic`, `order.delay.topic`). Request IDs are relayed into consumer MDC. Failed messages are republished to `error.topic`.
- XXL-Job: `@XxlJob` handlers in comic, watch, message, pay, unlock, and order; admin/executor config is `ac.xxl-job` in Nacos.
- Redis/Redisson: `@Lock` for distributed locks; unlock uses Lua scripts (`unlock/src/main/resources/lua/*.lua`) for atomic coupon receipt.
- Delayed work: `RabbitMqHelper.sendDelayMessage` for order timeout/refund flows; watch uses an in-process `DelayQueue` plus Redis cache (`WatchRecordDelayTaskHandler`) to batch playback-progress writes.
- Search indexing is event-driven: `search` listens to comic/order MQ events and writes Elasticsearch through `RestHighLevelClient` (`repository/impl`).
- Seata `@GlobalTransactional` is used only in comic draft publishing (`ComicDraftServiceImpl`).
- Feature patterns: unlock uses strategy classes under `strategy/discount` and `strategy/scope`; pay abstracts channels behind `IPayService` with Ali/Wx implementations; media abstracts storage under `storage/ali` and `storage/tencent`; message abstracts SMS providers under `thirdparty/*`.

## Conventions and gotchas

- Per-service layering: `controller` → `service/I*Service` + `service/impl` → `mapper` (MyBatis-Plus `BaseMapper`) → `domain/{po,dto,vo,query}`. SQL that cannot be expressed with wrappers lives in `src/main/resources/mapper/*.xml`; `@MapperScan("com.aicomic.<service>.mapper")` is on each application class.
- Each service owns its database (one `aicomic_*` schema per service); no entities are shared across services, only `ac-api` DTOs. `ac.jdbc.database` is consumed by the Nacos `shared-mybatis.yaml`.
- Persistence compatibility: table names are comic-domain (`comic`, `comic_episode`, `watch_episode`, `unlock_activity`, ...), but several Java fields/columns keep legacy names. Do not rename `courseId`/`cataId`/`subjectId`/`teacherId`/`lessonId`/`sectionId` without also updating `@TableField`, mapper XML result maps/aliases, and the database schema.
- The `data` module entities do not use `@TableName`; confirm actual table names before migrating this module.
- Knife4j docs use `ac.swagger.*` in each `bootstrap.yml`; API docs are exposed only where `ac.swagger.enable=true`.
- Sensitive config in-repo: `pay/ac-pay-service/src/main/resources/bootstrap.yml` contains Alipay/WeChat credentials and a temporary `notifyHost` tunnel; `media` and `message` hold third-party settings (some via environment variables). Treat these values as secrets.
- The auth keystore is `auth/ac-auth-service/src/main/resources/aicomic.jks`, alias `aicomic`, password `AcComic2026`. Changing it invalidates existing tokens.

## Tests

Ten test classes live under `<module>/src/test/java`, mostly ad-hoc `@SpringBootTest` or third-party SDK tests (Alipay, Elasticsearch) that require the external Nacos/MySQL/Redis/ES environment or real credentials. They are not a reliable offline signal. Genuine unit-style examples: `unlock/src/test/java/com/aicomic/unlock/utils/CodeUtilTest.java` and `watch/src/test/java/com/aicomic/watch/utils/DelayTaskTest.java`. `mvn -DskipTests test-compile` is the quick check that test sources still compile.

## Runtime configuration checklist

Before starting services, ensure Nacos contains the `ac.*` keys (`ac.jdbc.database`, `ac.auth.resource.*`, `ac.swagger.*`, `ac.xxl-job.*`, `ac.mq.*`) and MySQL contains the `aicomic_*` databases. Service-specific Nacos data-ids must match the `spring.application.name` values in the table above. RabbitMQ exchanges/routing keys are defined in `MqConstants` and must match the broker topology. After changing controller paths, update the auth `privilege` table and clear the Redis `auth:privileges` cache.
