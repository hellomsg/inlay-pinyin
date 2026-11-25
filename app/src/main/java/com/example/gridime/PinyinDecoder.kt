package com.example.gridime

import android.content.Context
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

internal data class PinyinCandidate(val text: String, val consumed: Int)

/** Rime sees only the active spelling. Its dictionaries and worker are shared across input windows. */
internal object PinyinDecoder {
    private val worker = Executors.newSingleThreadExecutor()
    private var initialized: Boolean? = null

    fun warm(context: Context) {
        val app = context.applicationContext
        worker.execute { prepare(app) }
    }

    fun request(context: Context, spelling: String, isCurrent: () -> Boolean, deliver: (List<PinyinCandidate>) -> Unit) {
        val app = context.applicationContext
        worker.execute {
            if (!isCurrent()) return@execute
            val result = if (prepare(app) && isCurrent()) {
                RimeNative.candidates(spelling).mapNotNull { record ->
                    val parts = record.toString(Charsets.UTF_8).split('\t', limit = 2)
                    val consumed = parts.firstOrNull()?.toIntOrNull()
                    val text = parts.getOrNull(1)
                    if (consumed == null || consumed !in 1..spelling.length || text.isNullOrEmpty()) null
                    else PinyinCandidate(text, consumed)
                }.distinctBy { it.text }
            } else emptyList()
            if (isCurrent()) deliver(result)
        }
    }

    private fun prepare(context: Context): Boolean {
        initialized?.let { return it }
        val ready = try {
            System.loadLibrary("inlay_rime")
            val assets = context.assets
            val version = assets.open("rime/bundle-id.txt").bufferedReader().use { it.readText().trim() }
            if (!version.matches(Regex("[a-f0-9]{64}"))) throw IOException("Invalid Rime bundle identity")
            val base = File(context.noBackupFilesDir, "rime/$version")
            val shared = File(base, "shared")
            val user = File(base, "user")
            copyAssets(context, "rime", shared)
            if (!user.isDirectory && !user.mkdirs()) throw IOException("Cannot create Rime data directory")
            RimeNative.initialize(shared.path, user.path).also {
                if (!it) android.util.Log.e("InlayPinyin", "Rime could not open its Pinyin schema")
            }
        } catch (error: IOException) {
            android.util.Log.e("InlayPinyin", "Cannot prepare offline dictionary", error)
            false
        } catch (error: LinkageError) {
            android.util.Log.e("InlayPinyin", "Cannot load the Rime library", error)
            false
        }
        initialized = ready
        return ready
    }

    private fun copyAssets(context: Context, path: String, target: File) {
        val children = context.assets.list(path).orEmpty()
        if (children.isNotEmpty()) {
            if (!target.isDirectory && !target.mkdirs()) throw IOException("Cannot create dictionary directory")
            children.forEach { copyAssets(context, "$path/$it", File(target, it)) }
        } else if (!target.isFile) {
            val temporary = File(target.parentFile, "${target.name}.tmp")
            context.assets.open(path).use { input -> temporary.outputStream().use { input.copyTo(it) } }
            if (!temporary.renameTo(target)) throw IOException("Cannot install Rime asset ${target.name}")
        }
    }
}

internal object RimeNative {
    external fun initialize(shared: String, user: String): Boolean
    external fun candidates(spelling: String): Array<ByteArray>
}
