package com.rnfanbotwebapp.service.redis

import android.os.Parcel
import android.os.Parcelable

class RedisBucket() : Parcelable {

    var key: String? = null

    var path: String? = null

    var value: String? = null

    var topic: String? = null

    var msg: String? = null

    var isStringValue: Boolean? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        path = parcel.readString()
        value = parcel.readString()
        topic = parcel.readString()
        msg = parcel.readString()
        isStringValue = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    constructor(
        key: String,
        value: String,
        isStringValue: Boolean
    ) : this() {
        this.key = key
        this.value = value
        this.isStringValue = isStringValue
    }

    constructor(
        key: String,
        path: String,
        value: String,
        isStringValue: Boolean
    ) : this() {
        this.key = key
        this.path = path
        this.value = value
        this.isStringValue = isStringValue
    }

    constructor(topic: String, msg: String) : this() {
        this.topic = topic
        this.msg = msg
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(path)
        parcel.writeString(value)
        parcel.writeString(topic)
        parcel.writeString(msg)
        parcel.writeValue(isStringValue)
    }

    fun readFromParcel(dest: Parcel) {
        key = dest.readString()
        path = dest.readString()
        value = dest.readString()
        topic = dest.readString()
        msg = dest.readString()
        isStringValue = dest.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<RedisBucket> {
        override fun createFromParcel(parcel: Parcel): RedisBucket {
            return RedisBucket(parcel)
        }

        override fun newArray(size: Int): Array<RedisBucket?> {
            return arrayOfNulls(size)
        }
    }

}