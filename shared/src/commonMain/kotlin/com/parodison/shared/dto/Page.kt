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