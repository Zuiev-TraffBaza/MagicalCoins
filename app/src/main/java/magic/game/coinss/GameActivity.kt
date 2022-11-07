package magic.game.coinss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import magic.game.coinss.game_screen.GameScreenContent
import magic.game.coinss.main_menu_screen.MainMenuScreenContent
import magic.game.coinss.theme.CoreTheme

class GameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isGmameOpened by mutableStateOf(false)
        setContent {
            BackHandler(isGmameOpened) {
                isGmameOpened = false
            }
            CoreTheme {
                if (isGmameOpened) {
                    GameScreenContent()
                } else {
                    MainMenuScreenContent { isGmameOpened = true }
                }
            }
        }
    }
}