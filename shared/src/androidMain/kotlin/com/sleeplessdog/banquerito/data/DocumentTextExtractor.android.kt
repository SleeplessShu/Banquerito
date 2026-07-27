package com.sleeplessdog.banquerito.data

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

actual object DocumentTextExtractor {

    actual fun extractText(bytes: ByteArray, mimeType: String, fileName: String): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return try {
            when {
                mimeType.startsWith("text/") || ext in listOf("txt", "csv") ->
                    bytes.toString(Charsets.UTF_8)

                mimeType == "application/rtf" || ext == "rtf" ->
                    extractRtf(bytes.toString(Charsets.UTF_8))

                ext == "docx" || mimeType.contains("wordprocessingml") ->
                    extractDocx(bytes)

                ext == "xlsx" || mimeType.contains("spreadsheetml") ->
                    extractXlsx(bytes)

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- RTF: убираем управляющие последовательности \xxx и группы {}
    private fun extractRtf(raw: String): String {
        val noGroups = raw.replace(Regex("\\{\\\\[^{}]*}"), "")
        val noControls = noGroups.replace(Regex("\\\\[a-zA-Z]+-?\\d* ?"), "")
        val noBraces = noControls.replace(Regex("[{}]"), "")
        return noBraces.replace(Regex("\\s+"), " ").trim()
    }

    // --- DOCX: word/document.xml, вытаскиваем текст из <w:t> тегов
    private fun extractDocx(bytes: ByteArray): String {
        val xml = readZipEntry(bytes, "word/document.xml") ?: return ""
        return extractTagText(xml, "w:t")
    }

    // --- XLSX: читаем все sheet*.xml + sharedStrings.xml (если есть)
    private fun extractXlsx(bytes: ByteArray): String {
        val sb = StringBuilder()
        val zip = ZipInputStream(ByteArrayInputStream(bytes))
        val sheetXmls = mutableListOf<String>()
        var sharedStrings: List<String> = emptyList()

        var entry = zip.nextEntry
        val entries = mutableMapOf<String, String>()
        while (entry != null) {
            if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            } else if (entry.name == "xl/sharedStrings.xml") {
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
            entry = zip.nextEntry
        }
        zip.close()

        entries["xl/sharedStrings.xml"]?.let { xml ->
            sharedStrings = extractTagTextList(xml, "t")
        }

        entries.entries
            .filter { it.key.startsWith("xl/worksheets/sheet") }
            .sortedBy { it.key }
            .forEach { (_, xml) ->
                // значения из <v> внутри <c>, если тип строки — берём из sharedStrings по индексу
                val cellRegex = Regex("<c[^>]*r=\"[A-Z]+\\d+\"[^>]*?(?:t=\"(\\w+)\")?[^>]*>(?:<f[^>]*>.*?</f>)?(?:<v>(.*?)</v>)?</c>")
                cellRegex.findAll(xml).forEach { match ->
                    val type = match.groupValues[1]
                    val value = match.groupValues[2]
                    if (value.isNotBlank()) {
                        val text = if (type == "s") {
                            value.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: value
                        } else {
                            value
                        }
                        sb.append(text).append(" | ")
                    }
                }
                sb.append("\n")
            }

        return sb.toString().trim()
    }

    private fun readZipEntry(bytes: ByteArray, entryName: String): String? {
        val zip = ZipInputStream(ByteArrayInputStream(bytes))
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == entryName) {
                return zip.readBytes().toString(Charsets.UTF_8)
            }
            entry = zip.nextEntry
        }
        return null
    }

    private fun extractTagText(xml: String, tag: String): String {
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(xml)
            .joinToString(" ") { it.groupValues[1] }
            .let { unescapeXml(it) }
    }

    private fun extractTagTextList(xml: String, tag: String): List<String> {
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(xml)
            .map { unescapeXml(it.groupValues[1]) }
            .toList()
    }

    private fun unescapeXml(s: String): String =
        s.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
}