package com.hsicen.fanlayoutmanager.model

import android.os.Parcel
import android.os.Parcelable

data class SportCardModel(
  val sportTitle: String? = "", val sportSubtitle: String? = "", val sportRound: String? = "",
  val imageResId: Int, val time: String? = "", val dayPart: String? = "", val backgroundColorResId: Int = 0
) : Parcelable {

  constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readInt(),
    parcel.readString(),
    parcel.readString(),
    parcel.readInt()
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(sportTitle)
    parcel.writeString(sportSubtitle)
    parcel.writeString(sportRound)
    parcel.writeInt(imageResId)
    parcel.writeString(time)
    parcel.writeString(dayPart)
    parcel.writeInt(backgroundColorResId)
  }

  override fun describeContents(): Int = 0

  companion object CREATOR : Parcelable.Creator<SportCardModel> {

    override fun createFromParcel(parcel: Parcel): SportCardModel = SportCardModel(parcel)
    override fun newArray(size: Int): Array<SportCardModel?> = arrayOfNulls(size)
  }
}