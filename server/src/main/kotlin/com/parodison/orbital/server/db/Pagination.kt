package com.parodison.orbital.server.db

import com.parodison.shared.dto.Page
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Query

fun <T> Query.paginate(page: Int = 1, pageSize: Int = 100, transform: (ResultRow) -> T): Page<T> {
    require(page >= 1) { "page debe ser >= 1" }
    require(pageSize >= 1) { "pageSize debe ser >= 1" }

    val totalItems = this.copy().count()

    val content = this
        .limit(pageSize)
        .offset((page - 1).toLong() * pageSize)
        .map(transform)

    val totalPages = if (totalItems == 0L) 0
        else ((totalItems - 1) / pageSize + 1).toInt()

    return Page(content, page, pageSize, totalItems, totalPages)
}