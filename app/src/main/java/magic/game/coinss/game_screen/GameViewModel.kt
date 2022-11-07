package magic.game.coinss.game_screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import magic.game.coinss.use_cases.allItemsVisible
import magic.game.coinss.use_cases.getItemsList
import magic.game.coinss.use_cases.hideAllItems
import magic.game.coinss.use_cases.makeRandomVisible
import kotlin.random.Random

class Item(res: Int, isVisible: Boolean = false) {
    val id: Int = Random.nextInt()
    var res by mutableStateOf(res)
    var isVisible by mutableStateOf(isVisible)

    override fun equals(other: Any?): Boolean {
        if (other !is Item) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        return id
    }
}

class GameViewModel : ViewModel() {
    private val looperDelay = 300L
    private val nItems = 15
    var isLooping = false
    val items: List<Item> = getItemsList(nItems)
    var score: Int by mutableStateOf(0)

    fun onItemClick(item: Item) {
        if(!isLooping || !item.isVisible) return
        items.find { item.id == it.id }?.isVisible = false
        score++
    }

    init {
        startLooper()
    }

    fun refresh(){
        Log.d("SlvkLog", "Refresh")
        stopLooper()
        startLooper()
        score = 0
        items.hideAllItems()
    }

    private fun onLoop() {
        items.makeRandomVisible()
        if (items.allItemsVisible()) {
            onLose()
        }
    }

    private fun onLose() {
        stopLooper()
    }

    private fun startLooper() = viewModelScope.launch {
        isLooping = true
        while (isLooping) {
            onLoop()
            delay(looperDelay)
        }
    }

    private fun stopLooper() {
        isLooping = false
    }

}