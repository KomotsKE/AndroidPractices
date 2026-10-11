package com.example.androidpractices.ui.common

import com.example.androidpractices.domain.model.BreweryException

/** Текст ошибки для пользователя. */
fun Throwable.toUserMessage(): String = when (this) {
    is BreweryException.NoConnection -> "Нет подключения к интернету. Проверьте сеть и повторите попытку"
    is BreweryException.Timeout -> "Сервер не ответил вовремя. Повторите попытку"
    is BreweryException.Server -> "Ошибка сервера (код $code). Попробуйте позже"
    is BreweryException.Parsing -> "Не удалось разобрать ответ сервера"
    else -> "Не удалось загрузить данные"
}
