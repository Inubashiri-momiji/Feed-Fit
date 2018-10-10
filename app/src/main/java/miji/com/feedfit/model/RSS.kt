package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable

class RSS() : Parcelable {

    var title: String? = null // Especifica el titulo del feed, si es que tiene
    var subtitle: String? = null //Especifica un subtitulo, si es que tiene
    var category: String? = null  //especifica la categoria a la que pertenece el feed, si es que tiene
    var icon: String? = null //Contiene la url del icono origen del feed, si es que tiene
    var logo: String? = null //Contiene la url del logo origen del feed, si es que tiene
    var link: String? = null // especifica el enlace del feed
    var author: String? = null //especifica el autor, si es que tiene
    var entries: ArrayList<RSSEntry> = ArrayList()//Especifica las entradas del feed, si es que tiene

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        subtitle = parcel.readString()
        category = parcel.readString()
        icon = parcel.readString()
        logo = parcel.readString()
        link = parcel.readString()
        author = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(category)
        parcel.writeString(icon)
        parcel.writeString(logo)
        parcel.writeString(link)
        parcel.writeString(author)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RSS> {
        override fun createFromParcel(parcel: Parcel): RSS {
            return RSS(parcel)
        }

        override fun newArray(size: Int): Array<RSS?> {
            return arrayOfNulls(size)
        }
    }

}