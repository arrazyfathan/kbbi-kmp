package com.arrazyfathan.kbbi.feature.campaigns.domain

/** Requests an asynchronous refresh of remote subscriptions after user intent changes. */
fun interface CampaignSubscriptionReconciler {
    fun requestReconcile()
}

object NoOpCampaignSubscriptionReconciler : CampaignSubscriptionReconciler {
    override fun requestReconcile() = Unit
}
