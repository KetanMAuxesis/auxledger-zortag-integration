package com.auxesisgroup.auxsecure

data class Item(
        var client: Int = 0,
        var code: String = "",
        var name: String = "",
        var message: String = "",
        var link: Link = Link(),
        var details: List<Detail> = listOf()
)

data class Detail(
        var heading: String = "",
        var content: String = ""
)

data class Link(
        var url: String = "",
        var text: String = ""
)


data class ItemUpdate(
        var name: String = "",
        var message: String = "",
        var link: Link = Link(),
        var details: List<Detail> = listOf()
)


data class Items(
        val client: Int = 0,
        val items: List<Item> = listOf()
)

data class ItemActive(
        val code: String = "",
        val name: String = ""
)


data class Error(
        val error: String = ""
)