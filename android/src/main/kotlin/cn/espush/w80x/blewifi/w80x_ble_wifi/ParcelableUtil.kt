package cn.espush.w80x.blewifi.w80x_ble_wifi

import android.os.Parcel

import android.os.Parcelable
import kotlin.experimental.and

object ParcelableUtil {
    fun marshall(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun unMarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0) // This is extremely important!
        return parcel
    }

    fun <T> unMarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = unMarshall(bytes)
        val result = creator.createFromParcel(parcel)
        parcel.recycle()
        return result
    }
}
