package co.pacastrillonp.appletdummy.repository

import android.content.Context
import androidx.core.content.ContextCompat

interface StorageRepository {
    val mediaPath: String?
}

class StorageRepositoryImpl(private val context: Context) : StorageRepository {
    override val mediaPath: String?
        get() = ContextCompat.getExternalFilesDirs(context, null)
            .filterNotNull()
            .firstOrNull { it.canRead() && it.canWrite() }?.absolutePath
}