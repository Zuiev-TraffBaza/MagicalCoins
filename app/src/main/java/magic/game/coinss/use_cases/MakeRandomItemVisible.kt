package magic.game.coinss.use_cases

import magic.game.coinss.game_screen.Item
import kotlin.random.Random

fun List<Item>.makeRandomVisible() {
    var currentIndex = Random.nextInt(0, lastIndex)
    repeat(size) {
        val currentItem = get(currentIndex % size)
        if (!currentItem.isVisible) {
            currentItem.isVisible = true
            return
        }
        currentIndex++
    }
}