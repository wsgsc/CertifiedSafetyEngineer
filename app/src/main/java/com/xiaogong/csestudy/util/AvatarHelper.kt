package com.xiaogong.csestudy.util

import android.content.Context
import android.net.Uri
import java.io.File

object AvatarHelper {
    private const val AVATAR_FILENAME = "user_avatar.jpg"

    fun copyToInternal(context: Context, uri: Uri): String {
        val dest = File(context.filesDir, AVATAR_FILENAME)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest.absolutePath
    }

    fun getAvatarFile(context: Context): File = File(context.filesDir, AVATAR_FILENAME)
}
