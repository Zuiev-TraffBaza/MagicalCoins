package magic.game.coinss.models

import com.google.gson.annotations.SerializedName

data class CheckUserResponse(
    @SerializedName("url")
    val url: String = "",
    @SerializedName("visitor_id")
    val visitorId: String = "",
    @SerializedName("is_first_engagement")
    val isBadUser: Boolean = false,
)