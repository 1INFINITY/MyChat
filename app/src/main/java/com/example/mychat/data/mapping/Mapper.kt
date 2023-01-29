package com.example.mychat.data.mapping

interface Mapper<SRC, DST> {
    fun transform(data: SRC): DST
}