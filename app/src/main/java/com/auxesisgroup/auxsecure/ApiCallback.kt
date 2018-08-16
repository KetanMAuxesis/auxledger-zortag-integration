package com.auxesisgroup.auxsecure

interface ApiCallback {
    fun <T> onResponse(res: T)
    fun <T> onError(err: T)
}