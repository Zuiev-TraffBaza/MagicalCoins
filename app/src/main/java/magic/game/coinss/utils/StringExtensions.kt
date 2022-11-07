package magic.game.coinss.utils

fun String.getUtmSource(): String {
    return try {
        substring(indexOf("=") + 1, indexOf("&"))
    } catch (e: Exception){
        ""
    }
}

fun String.getUtmMedium(): String {
    return try {
        substring(lastIndexOf("=") + 1)
    } catch (e: Exception){
        ""
    }
}