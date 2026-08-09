package com.xiaogong.csestudy.util

import android.content.Context
import com.xiaogong.csestudy.data.model.PdfDoc
import java.io.File

/**
 * 读取 assets/pdf 下的内置学习资料。
 * 放新资料只需把 PDF 丢进 app/src/main/assets/pdf/，无需改代码。
 * 文件名可用 "01_安全生产法.pdf" 形式控制排序，序号不会显示。
 */
object PdfAssets {

    private const val ASSET_DIR = "pdf"

    fun list(context: Context): List<PdfDoc> {
        val names = runCatching { context.assets.list(ASSET_DIR) }.getOrNull() ?: return emptyList()
        return names
            .filter { it.endsWith(".pdf", ignoreCase = true) }
            .sorted()
            .map { name ->
                PdfDoc(
                    assetName = name,
                    title = titleOf(name),
                    sizeBytes = assetLength(context, name)
                )
            }
    }

    fun titleOf(assetName: String): String =
        assetName.substringBeforeLast('.')
            .replace(Regex("^\\d+[_\\-.\\s]*"), "")
            .ifBlank { assetName }

    /**
     * PDFView 只能从真实文件按需 seek，assets 里的条目做不到，
     * 所以首次打开时拷到 filesDir，后续直接复用。
     */
    fun ensureLocalFile(context: Context, assetName: String): File {
        val dir = File(context.filesDir, ASSET_DIR).apply { mkdirs() }
        val target = File(dir, assetName)
        val expected = assetLength(context, assetName)

        if (target.isFile && expected > 0 && target.length() == expected) return target

        val temp = File(dir, "$assetName.tmp")
        context.assets.open("$ASSET_DIR/$assetName").use { input ->
            temp.outputStream().use { output -> input.copyTo(output) }
        }
        if (target.exists()) target.delete()
        check(temp.renameTo(target)) { "无法写入 ${target.name}" }
        return target
    }

    private fun assetLength(context: Context, assetName: String): Long =
        runCatching {
            context.assets.openFd("$ASSET_DIR/$assetName").use { it.length }
        }.getOrDefault(-1L)
}
