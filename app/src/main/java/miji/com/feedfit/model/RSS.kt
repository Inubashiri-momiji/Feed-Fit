package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
open class RSS(//especifica el autor, si es que tiene
        @SerializedName("pudate")
        @Expose
        var pubdate: String? = null,
        @SerializedName("author")
        @Expose
        var author: String? = null,//Especifica las entradas del feed, si es que tiene
        @SerializedName("entries")
        @Expose
        var entries: RealmList<RSSEntry> = RealmList(),// especifica el enlace del feed
        @SerializedName("link")
        @Expose
        @PrimaryKey
        var link: String? = null,//Contiene la url del logo origen del feed, si es que tiene
        @SerializedName("logo")
        @Expose
        var logo: String? = null,//Contiene la url del icono origen del feed, si es que tiene
        @SerializedName("icon")
        @Expose
        var icon: String? = null,//especifica la categoria a la que pertenece el feed, si es que tiene
        @SerializedName("category")
        @Expose
        var category: String? = null,//Especifica un subtitulo, si es que tiene
        @SerializedName("subtitle")
        @Expose
        var subtitle: String? = null,// Especifica el titulo del feed, si es que tiene
        @SerializedName("title")
        @Expose
        var title: String? = null) : Parcelable, RealmObject() {

    constructor(parcel: Parcel) : this() {
        pubdate = parcel.readString()
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
        parcel.writeString(pubdate)
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