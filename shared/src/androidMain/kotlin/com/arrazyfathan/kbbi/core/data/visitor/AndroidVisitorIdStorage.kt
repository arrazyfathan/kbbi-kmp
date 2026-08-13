package com.arrazyfathan.kbbi.core.data.visitor

import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext

class AndroidVisitorIdStorage : VisitorIdStorage {
    private val preferences by lazy {
        appContext.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
    }

    override fun getVisitorId(): String? = preferences.getString(VISITOR_ID_KEY, null)

    override fun saveVisitorId(visitorId: String) {
        preferences.edit().putString(VISITOR_ID_KEY, visitorId).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "kbbi_visitor"
        const val VISITOR_ID_KEY = "visitor_id"
    }
}
