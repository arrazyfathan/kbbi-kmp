package com.arrazyfathan.kbbi.feature.campaigns

import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignSubscriptionReconciler
import platform.Foundation.NSNotificationCenter

object IosCampaignSubscriptionReconciler : CampaignSubscriptionReconciler {
    override fun requestReconcile() {
        NSNotificationCenter.defaultCenter.postNotificationName(IOS_CAMPAIGN_RECONCILE_NOTIFICATION, null)
    }

    private const val IOS_CAMPAIGN_RECONCILE_NOTIFICATION = "com.arrazyfathan.kbbi.campaigns.reconcile"
}
