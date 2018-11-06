package miji.com.feedfit.utilities

import android.os.StrictMode
import android.util.Log
import android.util.Xml
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException


class XMLParser {  //  more information at @link:https://developer.android.com/training/basics/network-ops/xml#kotlin

    //private var regexPattern: Pattern = Pattern.compile("/(\\d{6,10})/")
    //private var dateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(stream: String): RSS { //Genera el parsing del XML mediante el uso de XMLpullParser
        var items: RSS? = null
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            val s = ByteArrayInputStream(stream.toByteArray(charset("UTF-8")))
            parser.setInput(s, "UTF-8")
            parser.nextTag()
            items = readFeed(parser)
        } catch (e: Exception) {
            Log.w(e.message, e)
        } finally {
            if (items == null) items = RSS() //Si al terminar el método, el resultado fue nulo, generamos un elemento vacio
        }
        return items
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): RSS { //Se encarga de la lectura de cada entrada del feed y filtra las entradas no necesarias
        val rss = RSS()
        var entry: RSSEntry? = null
        var text: String? = null
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.toLowerCase()
                when (parser.eventType) { //Genera una captura de las variables que encuentra en cada entrada para construir el objeto posteriormente
                    XmlPullParser.START_TAG -> if (tagName.equals("entry", ignoreCase = true) ||
                            tagName.equals("item", ignoreCase = true))
                        entry = RSSEntry()
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG ->
                        when (tagName) { //Al llegar al final del documento, se genera el objeto resultado del parsing

                            //Feed
                            "item", "entry" -> {
                                entry?.parentLink = rss.link
                                if (rss.icon.isNullOrBlank() && rss.logo.isNullOrBlank()) {
                                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                                    StrictMode.setThreadPolicy(policy)
                                    val faviconURL: String = HTMLParser.getFaviconURL(HTMLParser.parseWeb(rss.link!!))
                                    rss.icon = (faviconURL).trim()
                                }
                                rss.entries.add(entry!!)
                            }//RSS 2.0

                            "icon" -> {
                                rss.icon = text?.trim()               //Atom 1.0 favicon}
                                if (rss.icon.isNullOrBlank())
                                    rss.icon = null
                            }
                            "logo", "url", "imagen" -> {
                                rss.logo = text?.trim() //RSS 2.0
                                if (rss.logo.isNullOrBlank())
                                    rss.logo = null
                            }
                            "subtitle" -> rss.subtitle = text?.trim() //Atom 1.0
                            "pubDate" -> rss.pubdate = text?.trim() //RSS2.0
                            //Entradas -- items
                            "published" -> entry?.published = text?.trim() //Atom 1.0
                            "content" -> entry?.content = text?.trim() //Atom 1.0
                            "summary" -> entry?.summary = text?.trim() //Atom 1.0
                            "enclosure" -> entry?.enclosure = text?.trim() //RSS2.0
                            //Compartidas entradas e items
                            "description" -> if (entry != null) entry.content = text //Atom 1.0 - RSS 2.0
                            else rss.subtitle = text?.trim()
                            "title" -> if (entry != null) entry.title = text //Atom 1.0 - RSS 2.0
                            else rss.title = text?.trim()
                            "category" -> if (entry != null) entry.category = text //Atom 1.0 - RSS 2.0
                            else rss.category = text?.trim()
                            "link" -> if (entry != null) entry.link = text //Atom 1.0 - RSS 2.0
                            else { //caso del Canal
                                if (text.isNullOrBlank() && parser.attributeCount > 0) {
                                    rss.link = parser.getAttributeValue(null, "href")
                                } else {
                                    rss.link = text
                                }
                            }
                            "author" -> if (entry != null) entry.author = text //Atom 1.0 - RSS 2.0
                            else rss.author = text
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("XMLParser", "The stream cannot be parsed, error: ${e.message} ")
        }
        return rss
    }

}