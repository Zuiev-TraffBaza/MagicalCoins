package magic.game.coinss.main_menu_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import magic.game.coinss.R
import magic.game.coinss.theme.BalooFontFamily

@Composable
fun MainMenuScreenContent(
    navigateToGame: () -> Unit,
) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.back),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "PLAY",
            style = MaterialTheme.typography.h1,
            modifier = Modifier.clickable { navigateToGame() },
            fontFamily = BalooFontFamily,
            color = Color.White
        )
    }
}