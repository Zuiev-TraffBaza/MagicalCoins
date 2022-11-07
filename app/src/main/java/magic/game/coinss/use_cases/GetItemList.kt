package magic.game.coinss.use_cases

import magic.game.coinss.R
import magic.game.coinss.game_screen.Item


private val itemsRes = listOf(
    R.drawable.sym0_imperialriches,
    R.drawable.sym1_imperialriches,
    R.drawable.sym3_imperialriches,
    R.drawable.sym4_imperialriches,
    R.drawable.sym5_imperialriches,
    R.drawable.sym6_imperialriches,
    R.drawable.sym7_imperialriches,
    R.drawable.sym8_imperialriches,
    R.drawable.sym9_imperialriches,
)

fun getItemsList(size: Int): List<Item>{
    return List(size){ Item(res = itemsRes.random()) }
}