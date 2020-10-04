package com.hsicen.fanlayoutmanager.model

import com.hsicen.fanlayoutmanager.R
import java.util.*

object SportCardsUtils {
  fun generateSportCards(): Collection<SportCardModel> {

    val sportCardModels: MutableList<SportCardModel> = ArrayList(20)
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_1))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_2))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_3))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_4))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_5))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_1))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_2))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_3))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_4))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_5))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_1))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_2))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_3))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_4))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_5))
    sportCardModels.add(SportCardModel(imageResId = R.drawable.pic_card_1))
    return sportCardModels
  }
}