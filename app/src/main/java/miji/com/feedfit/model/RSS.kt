package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RSS(//especifica el autor, si es que tiene
        var author: String? = null,//Especifica las entradas del feed, si es que tiene
        var entries: RealmList<RSSEntry> = RealmList(),// especifica el enlace del feed
        var link: String? = null,//Contiene la url del logo origen del feed, si es que tiene
        var logo: String? = null,//Contiene la url del icono origen del feed, si es que tiene
        var icon: String? = null,//especifica la categoria a la que pertenece el feed, si es que tiene
        var category: String? = null,//Especifica un subtitulo, si es que tiene
        var subtitle: String? = null,// Especifica el titulo del feed, si es que tiene
        var title: String? = null) : Parcelable {

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        subtitle = parcel.readString()
        category = parcel.readString()
        icon = parcel.readString()
        logo = parcel.readString()
        link = parcel.readString()
        author = parcel.readString()
        entries = RealmList()
        entries.addAll(parcel.createTypedArrayList(RSSEntry.CREATOR))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(category)
        parcel.writeString(icon)
        parcel.writeString(logo)
        parcel.writeString(link)
        parcel.writeString(author)
        parcel.writeTypedList(entries)
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