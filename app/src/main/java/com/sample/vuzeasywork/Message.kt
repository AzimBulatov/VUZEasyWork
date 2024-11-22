package com.sample.vuzeasywork

import android.os.Parcel
import android.os.Parcelable

data class Message(
    val text: String = "",
    val isUser: Boolean = false, // true — пользователь, false — собеседник
    val style: String = "default" // Сохраняет стиль сообщения
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "default"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeByte(if (isUser) 1 else 0)
        parcel.writeString(style)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message = Message(parcel)
        override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)

        // Преобразование из Map (Firebase-структура) в объект Message
        fun fromMap(data: Map<String, Any?>): Message {
            return Message(
                text = data["text"] as? String ?: "",
                isUser = data["user"] as? Boolean ?: false,
                style = data["style"] as? String ?: "default"
            )
        }
    }
}
