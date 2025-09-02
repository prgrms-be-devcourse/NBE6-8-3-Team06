package com.back.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore


data class RsData<T>(
    val resultCode: String,
    val msg: String,
    val data: T? = null
) {
    @JsonIgnore
    val statusCode: Int = resultCode
        .split("-", limit = 2)[0].toInt()

    constructor(resultCode: String, statusCode: Int, msg: String, data: T?) : this(
        resultCode = resultCode,
        msg = msg,
        data = data
    )
}