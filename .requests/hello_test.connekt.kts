import org.assertj.core.api.Assertions
import java.util.UUID
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val baseUrl: String by env
val path = "greeting"
var savedUserId: String? = null
val mapper = jacksonObjectMapper()

// Тест 1 должен вернуть приветствие
GET("$baseUrl/$path") {
    accept("application/json")
} then {
    Assertions.assertThat(code).isEqualTo(200)

    val responseText = body?.string() ?: throw AssertionError("Response body is null")
    val response = mapper.readValue<Map<String, String>>(responseText)
    Assertions.assertThat(response).isEqualTo(mapOf("text" to "Hello World"))
}

// Тест 2 создание пользователя - ИСПРАВЛЕНО!
POST("$baseUrl/$path") {
    accept("application/json")
    contentType("application/json")
    // Преобразуем Map в JSON строку
    body("""{"name":"Ivan","surname":"Ivanov"}""")
} then {
    Assertions.assertThat(code).isEqualTo(200)

    val responseText = body?.string() ?: throw AssertionError("Response body is null")
    val response = mapper.readValue<Map<String, Any>>(responseText)

    Assertions.assertThat(response).containsKey("id")
    Assertions.assertThat(response).containsKey("text")
    Assertions.assertThat(response["text"]).isEqualTo("Hello, Ivanov Ivan")

    savedUserId = response["id"] as String
    println("Created user with ID: $savedUserId")
}

// Тест 3 поиск созданного пользователя
if (savedUserId != null) {
    GET("$baseUrl/$path?id=$savedUserId") {
        accept("application/json")
    } then {
        Assertions.assertThat(code).isEqualTo(200)

        val responseText = body?.string() ?: throw AssertionError("Response body is null")
        val response = mapper.readValue<Map<String, String>>(responseText)

        Assertions.assertThat(response["name"]).isEqualTo("Ivan")
        Assertions.assertThat(response["surname"]).isEqualTo("Ivanov")
    }
}

// Тест 4 greeting/{id} через path variable
if (savedUserId != null) {
    GET("$baseUrl/$path/$savedUserId") {
        accept("application/json")
    } then {
        Assertions.assertThat(code).isEqualTo(200)

        val responseText = body?.string() ?: throw AssertionError("Response body is null")
        val response = mapper.readValue<Map<String, String>>(responseText)

        Assertions.assertThat(response["name"]).isEqualTo("Ivan")
        Assertions.assertThat(response["surname"]).isEqualTo("Ivanov")
    }
}

// Тест 5 должен вернуть 404
GET("$baseUrl/$path?id=${UUID.randomUUID()}") {
    accept("application/json")
} then {
    Assertions.assertThat(code).isEqualTo(404)
}