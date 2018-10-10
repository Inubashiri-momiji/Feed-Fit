package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable

class RSSEntry() : Parcelable {

    var title: String? = null // Especifica el titulo de la entrada, si es que tiene
    var published: String? = null // Espeficica la hora de publicación del feed, si es que tiene
    var author: String? = null //especifica el autor, si es que tiene
    var content: String? = null //Especifica el contenido directo del feed, si es que tiene, puede ser texto plano, HTML u otro XML
    var link: String? = null // especifica el enlace del la entrada del feed
    var summary: String? = null  //especifica el resumen de la entrada, si es que tiene
    var category: String? = null  //especifica la categoria a la que pertenece la entrada, si es que tiene

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        published = parcel.readString()
        author = parcel.readString()
        content = parcel.readString()
        link = parcel.readString()
        summary = parcel.readString()
        category = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(published)
        parcel.writeString(author)
        parcel.writeString(content)
        parcel.writeString(link)
        parcel.writeString(summary)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RSSEntry> {
        override fun createFromParcel(parcel: Parcel): RSSEntry {
            return RSSEntry(parcel)
        }

        override fun newArray(size: Int): Array<RSSEntry?> {
            return arrayOfNulls(size)
        }
    }

}