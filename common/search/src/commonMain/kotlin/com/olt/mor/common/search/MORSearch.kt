package com.olt.mor.common.search

import com.arkivanov.decompose.value.Value

interface MORSearch {

    val models: Value<Model>

    fun onSearchTermChanged(newText: String)

    data class Model(
        val searchTerm: String
    )
}