package com.romadmin.app.util

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "%.1f KB".format(bytes / 1024.0)
    if (bytes < 1024 * 1024 * 1024) return "%.1f MB".format(bytes / (1024.0 * 1024.0))
    return "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}

fun formatFileSizeFromString(sizeStr: String): String {
    val bytes = sizeStr.toLongOrNull() ?: return sizeStr
    return formatFileSize(bytes)
}
