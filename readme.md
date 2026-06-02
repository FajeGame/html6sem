# Лабораторная работа №7

**Тема:** аутентификация и авторизация (JWT, Spring Security, роли USER / ADMIN)

---

## Описание проекта

REST API сервиса доставки еды,  модернизация 4, а потом 5 и 6 лабы: CRUD-сущности, валидация, логирование, автотесты. В текущей работе API закрыт Spring Security: регистрация и логин через JWT, доступ к эндпоинтам по ролям.

Основные сущности:

| Сущность    | Назначение                                      |
|:------------|:------------------------------------------------|
| User        | Пользователь (email, пароль BCrypt, роль)       |
| Restaurant  | Ресторан                                        |
| Dish        | Блюдо, привязанное к ресторану                  |
| Order       | Заказ пользователя, содержащий список блюд      |

Публичные эндпоинты: `POST /auth/register`, `POST /auth/login`, просмотр ресторанов и меню (`GET`). Остальное — только с JWT в заголовке `Authorization: Bearer <token>`.

---

## Структура проекта

```
lab7/
├── src/main/kotlin/com/example/lab3/
│   ├── api/                    # REST-контроллеры, DTO, AuthController
│   ├── application/            # Сервисный слой (AuthService, OrderService, …)
│   ├── domain/                 # Доменные модели, Role, порты репозиториев
│   ├── security/               # JWT, SecurityConfig, фильтры
│   └── infrastructure/jpa/     # JPA-сущности, адаптеры
├── src/main/resources/
│   ├── application.yml         # Конфигурация + jwt.secret
│   └── db/migration/           # Flyway V1–V6
├── src/test/kotlin/.../
│   ├── application/            # Unit-тесты сервисов
│   └── api/                    # AuthIntegrationTest, RestaurantIntegrationTest
├── .github/workflows/ci.yaml
├── docker-compose.yaml
└── pom.xml
```

### Цепочка запроса с JWT

```
HTTP Request + Bearer token
    → JwtAuthenticationFilter
    → SecurityFilterChain (авторизация по URL/роли)
    → Controller → Service → Repository → PostgreSQL
```

---

## Реализованное в ЛР-7

### 1. Расширение User и миграция

**Файл:** `src/main/resources/db/migration/V6__add_auth_fields_to_users.sql`

```sql
ALTER TABLE users ADD COLUMN password VARCHAR(255);
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';
UPDATE users SET password = '...bcrypt...' WHERE password IS NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
```

**Файл:** `src/main/kotlin/com/example/lab3/infrastructure/jpa/UserEntity.kt`

```kotlin
@Enumerated(EnumType.STRING)
@Column(nullable = false)
var role: Role = Role.USER

@Column(nullable = false)
var password: String = ""
```

Роли: `USER` (заказы, просмотр), `ADMIN` (управление ресторанами и блюдами).

---

### 2. Регистрация и логин

**Файл:** `src/main/kotlin/com/example/lab3/api/AuthController.kt`

```kotlin
@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse>

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse>
}
```

Пароль хешируется через `BCryptPasswordEncoder`. Ответ: `{ token, email, role }`.

---

### 3. JwtService и фильтр

**Файл:** `src/main/kotlin/com/example/lab3/security/JwtService.kt`

```kotlin
fun generateToken(email: String, role: String): String =
    Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + expirationMs))
        .signWith(key)
        .compact()
```

**Файл:** `src/main/resources/application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:local-dev-jwt-secret-key-min-32-chars!!}
  expiration-ms: ${JWT_EXPIRATION:86400000}
```

Секрет задаётся через переменную окружения `JWT_SECRET` (файл `.env` в `.gitignore`).

**Файл:** `src/main/kotlin/com/example/lab3/security/JwtAuthenticationFilter.kt` — извлекает `Bearer`-токен, валидирует и устанавливает `SecurityContext`.

---

### 4. SecurityFilterChain

**Файл:** `src/main/kotlin/com/example/lab3/security/SecurityConfig.kt`

```kotlin
http
    .csrf { it.disable() }
    .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    .authorizeHttpRequests {
        it.requestMatchers("/auth/**").permitAll()
        it.requestMatchers(GET, "/api/v1/restaurants/**").permitAll()
        it.requestMatchers(GET, "/api/v1/dishes/**").permitAll()
        it.anyRequest().authenticated()
    }
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
```

---

### 5. Авторизация по ролям

**Файл:** `src/main/kotlin/com/example/lab3/api/RestaurantController.kt`

```kotlin
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
fun create(@Valid @RequestBody body: RestaurantRequest): ResponseEntity<RestaurantResponse>
```

**Матрица доступа:**

| Эндпоинт                               | USER | ADMIN | Без токена |
|:---------------------------------------|:----:|:-----:|:----------:|
| `POST /auth/register`, `/auth/login`   |  —   |   —   |     +      |
| `GET /api/v1/restaurants/**`           |  +   |   +   |     +      |
| `POST/PUT/DELETE` ресторанов и блюд    |  —   |   +   |     —      |
| `POST /api/v1/orders`                  |  +   |   +   |     —      |
| `GET /api/v1/orders/{id}`              | свой |   +   |     —      |
| `PATCH /api/v1/orders/{id}/status`     |  —   |   +   |     —      |

Заказ создаётся от имени текущего пользователя (`@AuthenticationPrincipal`), `userId` из тела запроса не принимается.

---

### 6. Обработка ошибок безопасности

**Файл:** `src/main/kotlin/com/example/lab3/api/ApiExceptionHandler.kt`

```kotlin
@ExceptionHandler(BadCredentialsException::class)
fun handleBadCredentials(): ResponseEntity<ErrorResponse> =
    ResponseEntity.status(UNAUTHORIZED)
        .body(ErrorResponse(401, "Invalid email or password"))

@ExceptionHandler(AccessDeniedException::class)
fun handleAccessDenied(): ResponseEntity<ErrorResponse> =
    ResponseEntity.status(FORBIDDEN)
        .body(ErrorResponse(403, "Access denied"))
```

---

### 7. Тесты

**Файл:** `src/test/kotlin/com/example/lab3/api/AuthIntegrationTest.kt`

```kotlin
@Test
fun `POST restaurant без токена возвращает 401`() { ... }

@Test
@WithMockUser(roles = ["USER"])
fun `POST restaurant от USER возвращает 403`() { ... }

@Test
@WithMockUser(roles = ["ADMIN"])
fun `POST restaurant от ADMIN возвращает 201`() { ... }
```

Всего **34 теста** (unit + integration), включая тесты из ЛР-6 с учётом `@WithMockUser`.

--- 

## Запуск

### База данных

```bash
docker compose up -d
```

### Переменные окружения

```bash
# .env
JWT_SECRET=local-dev-jwt-secret-key-min-32-chars!!
```

### Приложение

```bash
./mvnw spring-boot:run
```

### Пример работы с API

```bash
# Регистрация
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123","name":"Ivan"}'

# Запрос с токеном
curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <token>"
```

### Автотесты

```bash
./mvnw test
```

---

## CI

`.github/workflows/ci.yaml` — `./mvnw test` при PR. Переменная `JWT_SECRET` задана в workflow.

---

## Эндпоинты (кратко)

| Метод  | Путь                    | Доступ              |
|:-------|:------------------------|:--------------------|
| POST   | `/auth/register`        | публичный           |
| POST   | `/auth/login`           | публичный           |
| GET    | `/api/v1/restaurants`   | публичный           |
| POST   | `/api/v1/restaurants`   | ADMIN               |
| POST   | `/api/v1/orders`        | USER / ADMIN + JWT  |
| PATCH  | `/api/v1/orders/{id}/status` | ADMIN + JWT    |

Полное описание CRUD — в `spec.yaml` (часть эндпоинтов дополнена auth-ограничениями).
