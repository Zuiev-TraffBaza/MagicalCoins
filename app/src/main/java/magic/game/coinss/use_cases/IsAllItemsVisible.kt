package magic.game.coinss.use_cases

import magic.game.coinss.game_screen.Item

fun List<Item>.allItemsVisible(): Boolean {
    for (item in this) {
        if (!item.isVisible) return false
    }
    return true
}