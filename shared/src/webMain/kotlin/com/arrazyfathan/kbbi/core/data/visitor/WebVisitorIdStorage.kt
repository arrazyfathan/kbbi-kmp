package com.arrazyfathan.kbbi.core.data.visitor

import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import web.storage.localStorage

class WebVisitorIdStorage : VisitorIdStorage {
    override fun getVisitorId(): String? = localStorage.getItem(VISITOR_ID_KEY)

    override fun saveVisitorId(visitorId: String) {
        localStorage.setItem(VISITOR_ID_KEY, visitorId)
    }

    private companion object {
        const val VISITOR_ID_KEY = "kbbi.visitor.id"
    }
}
