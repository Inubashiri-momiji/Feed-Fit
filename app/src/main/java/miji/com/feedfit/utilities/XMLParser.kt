package miji.com.feedfit.utilities

import android.util.Log
import android.util.Xml
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class XMLParser {  // Para más información: @link:https://developer.android.com/training/basics/network-ops/xml#kotlin

    private var regexPattern: Pattern = Pattern.compile("/(\\d{6,10})/")
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(stream: String): RSS { //Genera el parsing del XML mediante el uso de XMLpullParser
        var items: RSS? = null
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            val s = ByteArrayInputStream(stream.toByteArray(charset("ISO-8859-1")))
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
                    XmlPullParser.START_TAG -> if (tagName.equals("entry", ignoreCase = true)) entry = RSSEntry()
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG ->
                        when (tagName) { //Al llegar al final del documento, se genera el objeto resultado del parsing

                            //Feed
                            "entry" -> rss.entries.add(entry!!)
                            "icon" -> rss.icon = text
                            "logo" -> rss.logo = text
                            "subtitle" -> rss.subtitle = text
                            //Entradas
                            "published" -> entry?.published = text
                            "content" -> entry?.content = text
                            "summary" -> entry?.summary = text
                            //Compartidas
                            "title" -> if (entry != null) entry.title = text
                            else rss.title = text
                            "category" -> if (entry != null) entry.category = text
                            else rss.category = text
                            "link" -> if (entry != null) entry.link = text
                            else rss.link = text
                            "author" -> if (entry != null) entry.author = text
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