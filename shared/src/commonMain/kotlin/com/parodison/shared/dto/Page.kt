package com.parodison.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val content: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
) {
    val hasNext: Boolean get() = currentPage < totalPages
    val hasPrevious: Boolean get() = currentPage > 1
}

fun <T, R> Page<T>.map(transform: (T) -> R): Page<R> = Page(
    content = content.map(transform),
    currentPage = currentPage,
    pageSize = pageSize,
    totalItems = totalItems,
    totalPages = totalPages,
)