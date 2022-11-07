package magic.game.coinss.models

import com.google.gson.annotations.SerializedName


data class CheckUserParameters(
    @SerializedName("bundle_id")
    val bundleId: String,
    @SerializedName("appsflyer_device_id")
    val appsflyerDeviceId: String,
    @SerializedName("advertising_id")
    val advertisingId: String,
    @SerializedName("campaign")
    val campaign: String,
    @SerializedName("utm_medium")
    val utmMedium: String,
    @SerializedName("utm_source")
    val utmSource: String
)

fun CheckUserParameters.toQueryParams(): Map<String, String>{
    return mapOf(
        Pair("bundle_id", bundleId),
        Pair("advertising_id", advertisingId),
        Pair("appsflyer_device_id", appsflyerDeviceId),
        Pair("campaign", campaign),
        Pair("utm_source", utmSource),
        Pair("utm_medium", utmMedium),
    )
}

