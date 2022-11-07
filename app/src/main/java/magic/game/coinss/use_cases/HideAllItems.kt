package magic.game.coinss.use_cases

import magic.game.coinss.game_screen.Item

fun List<Item>.hideAllItems() {
    forEach { it.isVisible = false }
}