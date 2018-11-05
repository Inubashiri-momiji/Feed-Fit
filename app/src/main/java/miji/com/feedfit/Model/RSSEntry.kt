package miji.com.feedfit.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class RSSEntry() : Parcelable, RealmObject() {
    @SerializedName("title")
    @Expose
    var title: String? = null // Especifica el titulo de la entrada, si es que tiene
    @SerializedName("published")
    @Expose
    var published: String? = null // Espeficica la hora de publicación del feed, si es que tiene
    @SerializedName("author")
    @Expose
    var author: String? = null //especifica el autor, si es que tiene
    @SerializedName("content")
    @Expose
    var content: String? = null //Especifica el contenido directo del feed, si es que tiene, puede ser texto plano, HTML u otro XML
    @SerializedName("link")
    @Expose
    var link: String? = null // especifica el enlace del la entrada del feed
    @SerializedName("summary")
    @Expose
    var summary: String? = null  //especifica el resumen de la entrada, si es que tiene
    @SerializedName("category")
    @Expose
    var category: String? = null  //especifica la categoria a la que pertenece la entrada, si es que tiene
    @SerializedName("enclosure")
    @Expose
    var enclosure: String? = null  //especifica si hay medios adjuntos a la que pertenece la entrada, si es que tiene
    @SerializedName("parentLink")
    @Expose
    var parentLink: String? = null //Especifica el enlace del canal padre.

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        published = parcel.readString()
        author = parcel.readString()
        content = parcel.readString()
        link = parcel.readString()
        summary = parcel.readString()
        category = parcel.readString()
        enclosure = parcel.readString()
        parentLink = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(published)
        parcel.writeString(author)
        parcel.writeString(content)
        parcel.writeString(link)
        parcel.writeString(summary)
        parcel.writeString(category)
        parcel.writeString(enclosure)
        parcel.writeString(parentLink)
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