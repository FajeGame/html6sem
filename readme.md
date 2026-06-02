# Лабораторная работа №6

**Тема:** тестирование Spring Boot-приложения (unit-тесты, интеграционные тесты, Testcontainers)

---

## Описание проекта

Основные сущности:

| Сущность    | Назначение                                      |
|:------------|:------------------------------------------------|
| User        | Пользователь системы                            |
| Restaurant  | Ресторан                                        |
| Dish        | Блюдо, привязанное к ресторану                  |
| Order       | Заказ пользователя, содержащий список блюд      |

API доступен по префиксу `/api/v1/`. Спецификация эндпоинтов — в файле `spec.yaml`.

---


## Структура проекта

```
lab6/
├── src/main/kotlin/com/example/lab3/
│   ├── api/                    # REST-контроллеры, DTO, обработчик ошибок
│   ├── application/            # Сервисный слой (бизнес-логика)
│   ├── domain/                 # Доменные модели и порты репозиториев
│   └── infrastructure/
│       ├── jpa/                # JPA-сущности, репозитории, адаптеры
│       ├── mock/               # In-memory реализации (режим mock)
│       └── DataProviderConfig.kt
├── src/main/resources/
│   ├── application.yml         # Конфигурация приложения
│   ├── logback-spring.xml      # Настройка логирования
│   └── db/migration/           # SQL-миграции Flyway (V1–V5)
├── src/test/kotlin/com/example/lab3/
│   ├── application/            # Unit-тесты сервисов
│   └── api/                    # Интеграционные тесты контроллеров
├── src/test/resources/
│   ├── application.yaml        # Общие настройки для тестов
│   └── application-test.yaml   # Профиль test
├── .test/run-test.sh           # Скрипт ручной проверки API (из ЛР-5)
├── .github/workflows/ci.yaml   # CI: запуск тестов при PR
├── docker-compose.yaml         # PostgreSQL для локальной разработки
├── spec.yaml                   # OpenAPI-спецификация
└── pom.xml
```

### Слои приложения

Запрос проходит цепочку:

```
Controller → Service → RepositoryPort → JpaAdapter → PostgreSQL
```

Обработка ошибок централизована в `ApiExceptionHandler` (`@RestControllerAdvice`). Кастомные исключения: `NotFoundException` (404), `AlreadyExistsException` (409), `InvalidOrderStateException` (400).

---

## Реализованное в ЛР-6

### 1. Зависимости для тестирования

**Файл:** `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

`spring-boot-starter-test` включает JUnit 5 и Mockito. Testcontainers подключается отдельными модулями для интеграционных тестов с PostgreSQL.

---

### 2. Тестовый профиль

**Файл:** `src/test/resources/application.yaml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true

logging:
  level:
    root: WARN
    com.example.lab3: DEBUG
```

- `ddl-auto: validate` — Hibernate не меняет схему, только проверяет
- Flyway включён — миграции накатываются при старте тестового контекста
- Уровень логирования `root: WARN`, для пакета приложения — `DEBUG`

Интеграционные тесты дополнительно помечены `@ActiveProfiles("test")`, профиль описан в `application-test.yaml`.

```properties
api.version=1.44
```

Без этой настройки Testcontainers 1.21 по умолчанию обращается к Docker API 1.32 и падает с ошибкой `Could not find a valid Docker environment`.

---

### 3. Unit-тесты сервисного слоя

Для каждого сервиса создан отдельный тестовый класс. Зависимости заменены моками через Mockito, Spring-контекст не поднимается (`@ExtendWith(MockitoExtension::class)`, без `@SpringBootTest`).

| Класс                  | Тестов | Что проверяется                                              |
|:-----------------------|:-------|:-------------------------------------------------------------|
| RestaurantServiceTest  | 6      | Создание, дубликат имени, поиск, удаление                    |
| OrderServiceTest       | 7      | Создание заказа, валидация, смена статуса, недопустимый переход |
| UserServiceTest        | 4      | Создание/получение существующего, поиск, удаление            |
| DishServiceTest        | 4      | CRUD-операции через репозиторий                              |

Структура каждого теста: **Arrange → Act → Assert**.

#### Позитивный сценарий

**Файл:** `src/test/kotlin/com/example/lab3/application/RestaurantServiceTest.kt`

```kotlin
@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {

    @Mock
    lateinit var restaurantRepositoryPort: RestaurantRepositoryPort

    @InjectMocks
    lateinit var restaurantService: RestaurantService

    @Test
    fun `create сохраняет ресторан если имя свободно`() {
        // Arrange
        val restaurant = Restaurant(id = 0, name = "Pizza Place", address = "ул. Ленина, 1")
        val saved = restaurant.copy(id = 1)
        `when`(restaurantRepositoryPort.findByName("Pizza Place")).thenReturn(null)
        `when`(restaurantRepositoryPort.create(restaurant)).thenReturn(saved)

        // Act
        val result = restaurantService.create(restaurant)

        // Assert
        assertEquals(saved, result)
        verify(restaurantRepositoryPort).create(restaurant)
    }
}
```

#### Негативный сценарий

**Файл:** `src/test/kotlin/com/example/lab3/application/RestaurantServiceTest.kt`

```kotlin
@Test
fun `create бросает AlreadyExistsException при дублировании имени`() {
    val restaurant = Restaurant(id = 0, name = "Pizza Place", address = "ул. Мира, 5")
    `when`(restaurantRepositoryPort.findByName("Pizza Place"))
        .thenReturn(Restaurant(id = 2, name = "Pizza Place", address = "другой адрес"))

    assertThrows<AlreadyExistsException> {
        restaurantService.create(restaurant)
    }

    verify(restaurantRepositoryPort, never()).create(restaurant)
}
```

#### Проверка бизнес-правил (смена статуса заказа)

**Файл:** `src/test/kotlin/com/example/lab3/application/OrderServiceTest.kt`

```kotlin
@Test
fun `updateStatus бросает InvalidOrderStateException при недопустимом переходе`() {
    val existing = Order(
        id = 1, userId = 1, status = OrderStatus.DELIVERED,
        createdAt = LocalDateTime.now(), dishIds = listOf(10L)
    )
    whenever(orderRepositoryPort.findById(1)).thenReturn(existing)

    assertThrows<InvalidOrderStateException> {
        orderService.updateStatus(1, OrderStatus.PENDING)
    }
}
```

---

### 4. Интеграционные тесты API

Класс `RestaurantIntegrationTest` поднимает полный Spring-контекст и отправляет HTTP-запросы через MockMvc. База данных — PostgreSQL в Docker-контейнере (Testcontainers).

#### Настройка контейнера и контекста

**Файл:** `src/test/kotlin/com/example/lab3/api/RestaurantIntegrationTest.kt`

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class RestaurantIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("integration-tests-db")
            withUsername("test")
            withPassword("test")
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc
}
```

`@ServiceConnection` автоматически прокидывает URL и credentials контейнера в Spring DataSource. `@Transactional` откатывает изменения в БД после каждого теста.

#### Позитивный сценарий — создание ресторана

**Файл:** `src/test/kotlin/com/example/lab3/api/RestaurantIntegrationTest.kt`

```kotlin
@Test
fun `POST restaurant возвращает 201 и создаёт запись`() {
    mockMvc.post("/api/v1/restaurants") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name": "Integration Place", "address": "ул. Тестовая, 1"}"""
    }.andExpect {
        status { isCreated() }
        jsonPath("$.id") { exists() }
        jsonPath("$.name") { value("Integration Place") }
        jsonPath("$.address") { value("ул. Тестовая, 1") }
    }
}
```

#### Негативные сценарии — валидация и конфликт

**Файл:** `src/test/kotlin/com/example/lab3/api/RestaurantIntegrationTest.kt`

```kotlin
@Test
fun `GET несуществующий ресторан возвращает 404`() {
    mockMvc.get("/api/v1/restaurants/999999")
        .andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(404) }
            jsonPath("$.message") { exists() }
        }
}

@Test
fun `POST restaurant с пустым именем возвращает 400 и errors`() {
    mockMvc.post("/api/v1/restaurants") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"name": "", "address": "ул. Тестовая, 1"}"""
    }.andExpect {
        status { isBadRequest() }
        jsonPath("$.status") { value(400) }
        jsonPath("$.errors.name") { exists() }
    }
}
```

Покрыты эндпоинты `RestaurantController`:

| Сценарий                              | Ожидаемый результат |
|:--------------------------------------|:--------------------|
| POST `/api/v1/restaurants`            | 201, тело с id, name, address |
| GET `/api/v1/restaurants/{id}`        | 200, корректные данные |
| GET `/api/v1/restaurants/999999`     | 404, `{ status, message }` |
| POST с пустым name                  | 400, `{ errors.name }` |
| POST с дублирующимся именем         | 409 |
| PUT несуществующего ресторана       | 404 |

---

### 5. CI

**Файл:** `.github/workflows/ci.yaml`

```yaml
name: CI

on:
  pull_request:
    branches: [main, master]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"
          cache: maven
      - name: Run tests
        run: ./mvnw test
```

При создании pull request выполняется `./mvnw test`. GitHub Actions предоставляет Docker, поэтому интеграционные тесты проходят в CI без дополнительной настройки.

---

## Запуск

### База данных

```bash
docker compose up -d
```

Параметры подключения (из `application.yml`): `localhost:5432`, БД `postgres`, пользователь/пароль `postgres`.

### Приложение

```bash
./mvnw spring-boot:run
```

Сервер стартует на `http://localhost:8080`.

### Автотесты


```bash
# все тесты
./mvnw test

# только unit-тесты
./mvnw test -Dtest="*ServiceTest"

# только интеграционные
./mvnw test -Dtest="*IntegrationTest"
```

```bash
bash .test/run-test.sh
```


## Эндпоинты

| Метод  | Путь                              | Описание                    |
|:-------|:----------------------------------|:----------------------------|
| GET    | `/api/v1/users`                   | Список пользователей        |
| POST   | `/api/v1/users`                   | Создать / вернуть по email  |
| GET    | `/api/v1/restaurants`             | Список ресторанов           |
| POST   | `/api/v1/restaurants`             | Создать ресторан            |
| GET    | `/api/v1/restaurants/{id}/dishes` | Меню ресторана              |
| POST   | `/api/v1/restaurants/{id}/dishes` | Добавить блюдо              |
| GET    | `/api/v1/dishes`                  | Список блюд (фильтр namePart) |
| POST   | `/api/v1/orders`                  | Создать заказ               |
| PATCH  | `/api/v1/orders/{id}/status`      | Сменить статус заказа       |

Полное описание — в `spec.yaml`.
