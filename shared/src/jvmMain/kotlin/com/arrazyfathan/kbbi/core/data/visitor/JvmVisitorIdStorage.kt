package com.arrazyfathan.kbbi.core.data.visitor

import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import java.util.prefs.Preferences

class JvmVisitorIdStorage : VisitorIdStorage {
    private val preferences = Preferences.userRoot().node(PREFERENCES_NODE)

    override fun getVisitorId(): String? = preferences.get(VISITOR_ID_KEY, null)

    override fun saveVisitorId(visitorId: String) {
        preferences.put(VISITOR_ID_KEY, visitorId)
    }

    private companion object {
        const val PREFERENCES_NODE = "com/arrazyfathan/kbbi/visitor"
        const val VISITOR_ID_KEY = "visitor_id"
    }
}
