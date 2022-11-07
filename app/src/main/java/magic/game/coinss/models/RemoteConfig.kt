package magic.game.coinss.models

import com.google.gson.annotations.SerializedName

data class RemoteConfig(
    @SerializedName("af_dev_key")
    val appsflyerDevKey: String = "",
    @SerializedName("one_sig_app_id")
    val oneSignalAppId: String = "",
    @SerializedName("user_check_url")
    val userCheckLink: String = "",
    @SerializedName("on_push_click_link")
    val onPushClickLink: String = ""
)