package magic.game.coinss.utils

fun String.decodeAsBase64(): String {
    val decodedBytes = android.util.Base64.decode(this, android.util.Base64.DEFAULT);
    return String(decodedBytes)
}