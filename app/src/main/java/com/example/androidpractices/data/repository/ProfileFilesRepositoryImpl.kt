package com.example.androidpractices.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.androidpractices.data.remote.toDomainException
import com.example.androidpractices.domain.model.BreweryException
import com.example.androidpractices.domain.model.DownloadedFile
import com.example.androidpractices.domain.repository.ProfileFilesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Фото копируется в личную папку приложения, поэтому не зависит от временного
 * разрешения на URI из галереи и остаётся доступным после перезагрузки устройства.
 * Резюме скачивается в кэш и открывается другим приложением через FileProvider.
 */
class ProfileFilesRepositoryImpl(
    private val context: Context,
    private val downloadClient: OkHttpClient,
) : ProfileFilesRepository {

    private val avatarsDir: File get() = File(context.filesDir, AVATARS_DIR)
    private val cameraDir: File get() = File(context.cacheDir, CAMERA_DIR)
    private val resumeDir: File get() = File(context.cacheDir, RESUME_DIR)

    override suspend fun createCameraTarget(): String = withContext(Dispatchers.IO) {
        val file = File(cameraDir.recreated(), "photo_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        contentUriOf(file)
    }

    override suspend fun importAvatar(sourceUri: String): String = withContext(Dispatchers.IO) {
        avatarsDir.mkdirs()
        // Новое имя на каждое фото: иначе загрузчик изображений показал бы старое из кэша.
        val target = File(avatarsDir, "avatar_${System.currentTimeMillis()}")
        try {
            val input = context.contentResolver.openInputStream(sourceUri.toUri())
                ?: throw IOException("Cannot open $sourceUri")
            input.use { source -> target.outputStream().use { source.copyTo(it) } }
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        Uri.fromFile(target).toString()
    }

    override suspend fun deleteUnusedAvatars(keepUri: String?) = withContext(Dispatchers.IO) {
        val keepName = keepUri?.toUri()?.lastPathSegment
        avatarsDir.listFiles()
            ?.filter { it.name != keepName }
            ?.forEach { it.delete() }
        cameraDir.deleteRecursively()
        Unit
    }

    override suspend fun downloadResume(url: String): DownloadedFile = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            downloadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw BreweryException.Server(response.code)
                val body = response.body ?: throw BreweryException.Parsing()

                val fileName = resumeFileName(response.request.url.pathSegments.lastOrNull().orEmpty())
                val directory = resumeDir.recreated()
                // Запись во временный файл: оборванная загрузка не будет выглядеть готовым резюме.
                val partFile = File(directory, "$fileName.part")
                partFile.outputStream().use { output -> body.byteStream().copyTo(output) }
                val file = File(directory, fileName)
                if (!partFile.renameTo(file)) throw IOException("Cannot rename $partFile")

                DownloadedFile(
                    contentUri = contentUriOf(file),
                    mimeType = resolveMimeType(body.contentType(), fileName),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }

    private fun contentUriOf(file: File): String =
        FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file).toString()

    private fun File.recreated(): File = apply {
        deleteRecursively()
        mkdirs()
    }

    /** Серверы часто отдают файл как octet-stream — тогда тип определяем по расширению. */
    private fun resolveMimeType(contentType: MediaType?, fileName: String): String {
        val fromHeader = contentType?.let { "${it.type}/${it.subtype}" }
        if (fromHeader != null && fromHeader !in GENERIC_MIME_TYPES) return fromHeader
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ANY_MIME_TYPE
    }

    private companion object {
        const val AUTHORITY_SUFFIX = ".fileprovider"
        const val AVATARS_DIR = "avatars"
        const val CAMERA_DIR = "camera"
        const val RESUME_DIR = "resume"
        const val ANY_MIME_TYPE = "*/*"
        val GENERIC_MIME_TYPES = setOf("application/octet-stream", "binary/octet-stream")
    }
}
