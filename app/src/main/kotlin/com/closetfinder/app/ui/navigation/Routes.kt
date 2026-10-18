package com.closetfinder.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val ADD_ITEM = "add_item"
    const val ITEM_DETAIL = "item_detail/{itemId}"

    fun itemDetail(itemId: Long) = "item_detail/$itemId"
}
