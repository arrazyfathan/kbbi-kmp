package com.arrazyfathan.kbbi.core.data.visitor

import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import platform.Foundation.NSUserDefaults

class IosVisitorIdStorage : VisitorIdStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getVisitorId(): String? = userDefaults.stringForKey(VISITOR_ID_KEY)

    override fun saveVisitorId(visitorId: String) {
        userDefaults.setObject(visitorId, forKey = VISITOR_ID_KEY)
    }

    private companion object {
        const val VISITOR_ID_KEY = "kbbi.visitor.id"
    }
}
