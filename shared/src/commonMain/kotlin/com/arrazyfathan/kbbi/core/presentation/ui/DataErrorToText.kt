package com.arrazyfathan.kbbi.core.presentation.ui

import com.arrazyfathan.kbbi.core.domain.model.DataError
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.error_bad_request
import kbbi_kmp.shared.generated.resources.error_conflict
import kbbi_kmp.shared.generated.resources.error_empty_body
import kbbi_kmp.shared.generated.resources.error_empty_query
import kbbi_kmp.shared.generated.resources.error_forbidden
import kbbi_kmp.shared.generated.resources.error_no_internet
import kbbi_kmp.shared.generated.resources.error_not_found
import kbbi_kmp.shared.generated.resources.error_payload_too_large
import kbbi_kmp.shared.generated.resources.error_request_timeout
import kbbi_kmp.shared.generated.resources.error_serialization
import kbbi_kmp.shared.generated.resources.error_server_error
import kbbi_kmp.shared.generated.resources.error_service_unavailable
import kbbi_kmp.shared.generated.resources.error_too_many_requests
import kbbi_kmp.shared.generated.resources.error_unauthorized
import kbbi_kmp.shared.generated.resources.error_unknown

fun DataError.asUiText(): UiText =
    when (this) {
        DataError.EmptyQuery -> UiText.StringResource(Res.string.error_empty_query)
        DataError.NoInternet -> UiText.StringResource(Res.string.error_no_internet)
        DataError.BadRequest -> UiText.StringResource(Res.string.error_bad_request)
        DataError.RequestTimeout -> UiText.StringResource(Res.string.error_request_timeout)
        DataError.Unauthorized -> UiText.StringResource(Res.string.error_unauthorized)
        DataError.Forbidden -> UiText.StringResource(Res.string.error_forbidden)
        DataError.NotFound -> UiText.StringResource(Res.string.error_not_found)
        DataError.Conflict -> UiText.StringResource(Res.string.error_conflict)
        DataError.TooManyRequests -> UiText.StringResource(Res.string.error_too_many_requests)
        DataError.PayloadTooLarge -> UiText.StringResource(Res.string.error_payload_too_large)
        DataError.ServerError -> UiText.StringResource(Res.string.error_server_error)
        DataError.ServiceUnavailable -> UiText.StringResource(Res.string.error_service_unavailable)
        DataError.EmptyBody -> UiText.StringResource(Res.string.error_empty_body)
        DataError.Serialization -> UiText.StringResource(Res.string.error_serialization)
        is DataError.Remote -> UiText.DynamicString(message)
        DataError.Unknown -> UiText.StringResource(Res.string.error_unknown)
    }
