package com.example.androidpractices.domain.model

/**
 * Ошибки получения данных в терминах домена. Текст для пользователя
 * определяется на слое представления, здесь только причина.
 */
sealed class BreweryException(cause: Throwable? = null) : Exception(cause) {
    /** Нет интернета / не удалось достучаться до сервера. */
    class NoConnection(cause: Throwable? = null) : BreweryException(cause)

    /** Сервер не ответил вовремя. */
    class Timeout(cause: Throwable? = null) : BreweryException(cause)

    /** Сервер ответил HTTP-ошибкой (4xx/5xx). */
    class Server(val code: Int, cause: Throwable? = null) : BreweryException(cause)

    /** Ответ пришёл, но не соответствует ожидаемому формату. */
    class Parsing(cause: Throwable? = null) : BreweryException(cause)

    class Unknown(cause: Throwable? = null) : BreweryException(cause)
}
