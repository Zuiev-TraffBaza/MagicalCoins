package magic.game.coinss.models

class UserInfo(
    val url: String = "",
    val uuid: String = "",
    val advertisingId: String = "",
    val isGoodUser: Boolean = false,
    val appsflyerDevKey: String = "",
    val oneSignalAppId: String = "",
    val userCheckLink: String,
    val onPushClickLink: String
)
