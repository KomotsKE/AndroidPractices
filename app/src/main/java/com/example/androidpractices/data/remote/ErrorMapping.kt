package com.example.androidpractices.data.remote

import com.example.androidpractices.domain.model.BreweryException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Превращает исключения Retrofit/OkHttp/сериализации в доменные [BreweryException]. */
fun Throwable.toDomainException(): BreweryException = when (this) {
    is BreweryException -> this
    is HttpException -> BreweryException.Server(code(), this)
    is SocketTimeoutException -> BreweryException.Timeout(this)
    is IOException -> BreweryException.NoConnection(this)
    is SerializationException -> BreweryException.Parsing(this)
    else -> BreweryException.Unknown(this)
}
