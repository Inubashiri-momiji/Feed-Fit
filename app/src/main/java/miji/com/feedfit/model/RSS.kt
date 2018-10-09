package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable

class RSS() : Parcelable {

    var link: String? = null
    var title: String? = null
    var summary: String? = null
    var id: Int? = null
    var type: Int? = null
    var postDate: Long? = null

    constructor(parcel: Parcel) : this() {
        link = parcel.readString()
        title = parcel.readString()
        summary = parcel.readString()
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        type = parcel.readValue(Int::class.java.classLoader) as? Int
        postDate = parcel.readValue(Long::class.java.classLoader) as? Long
    }


    constructor(type: Int) : this() {
        this.type = type
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(link)
        parcel.writeString(title)
        parcel.writeString(summary)
        parcel.writeValue(id)
        parcel.writeValue(type)
        parcel.writeValue(postDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RSS> {
        const val ARTICLE_TYPE = 0
        override fun createFromParcel(parcel: Parcel): RSS {
            return RSS(parcel)
        }

        override fun newArray(size: Int): Array<RSS?> {
            return arrayOfNulls(size)
        }
    }


}