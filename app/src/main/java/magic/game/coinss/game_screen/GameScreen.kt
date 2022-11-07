package magic.game.coinss.game_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import magic.game.coinss.R
import magic.game.coinss.theme.BalooFontFamily


@Composable
fun GameScreenContent() {
    val viewModel: GameViewModel = viewModel()

    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.back),
        contentDescription = null,
        contentScale = ContentScale.FillBounds
    )

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(top = 16.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.balance_panel),
                    contentDescription = null
                )
                Text(
                    text = viewModel.score.toString(),
                    fontFamily = BalooFontFamily,
                    color = Color.White,
                    fontSize = 25.sp
                )
            }
            Box {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.slot),
                    contentDescription = null
                )
                GameField(
                    items = viewModel.items, nItems = 5,
                    onItemClick = viewModel::onItemClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 24.dp, end = 24.dp, top = 30.dp)
                )
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.spin),
                contentDescription = null,
                modifier = Modifier.clickable { viewModel.refresh() })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameField(
    modifier: Modifier = Modifier,
    items: List<Item>,
    nItems: Int,
    onItemClick: (Item) -> Unit
) {

    LazyVerticalGrid(
        modifier = modifier,
        cells = GridCells.Fixed(nItems)
    ) {
        items(items.size) {
            FieldItem(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                item = items[it],
                onClick = onItemClick
            )
        }
    }
}

@Composable
fun FieldItem(modifier: Modifier, item: Item, onClick: (Item) -> Unit) {
    val alpha = if (item.isVisible) 1f else 0f
    Image(
        painter = painterResource(id = item.res),
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .clickable { onClick(item) }
    )
}