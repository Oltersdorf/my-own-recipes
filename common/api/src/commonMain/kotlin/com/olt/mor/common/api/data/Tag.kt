package com.olt.mor.common.api.data

sealed interface Tag {
    data class New(val name: String) : Tag

    data class Existing(val id: Long, val name: String) : Tag
}