package miji.com.feedfit.utilities

import android.util.Log
import android.util.Xml
import miji.com.feedfit.model.RSS
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class XMLParser {  // Para más información: @link:https://developer.android.com/training/basics/network-ops/xml#kotlin

    private var regexPattern: Pattern = Pattern.compile("/(\\d{6,10})/")
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(stream: String): ArrayList<RSS> { //Genera el parsing del XML mediante el uso de XMLpullParser
        var items: ArrayList<RSS>? = null
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
            if (items == null) items = ArrayList() //Si al terminar el método, el resultado fue nulo, generamos un elemento vacio
        }
        return items
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): ArrayList<RSS> { //Se encarga de la lectura de cada entrada del feed y filtra las entradas no necesarias
        val items = ArrayList<RSS>()
        var item = RSS(RSS.ARTICLE_TYPE)
        var text: String? = null
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> if (tagName.equals("item", ignoreCase = true)) item = RSS(RSS.ARTICLE_TYPE)
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG ->
                        when (tagName) { //Al llegar al final del documento, se genera el objeto resultado del parsing
                            "item" -> items.add(item)
                            "link" -> item.link = text
                            "title" -> item.title = text
                            "pubdate" -> try {
                                val cal = Calendar.getInstance()
                                cal.time = dateFormat.parse(text)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                item.postDate = cal.timeInMillis
                            } catch (e: ParseException) {
                                Log.d("XMLParser", "cannot parse date: " + text!!)
                            }

                            "guid" -> {
                                val matcher = regexPattern.matcher(text)
                                if (matcher.find())
                                    item.id = Integer.valueOf(matcher.group(1))
                                else
                                    item.id = 0
                            }
                            "enclosure" -> {
                                text = parser.getAttributeValue(null, "url")
                                item.summary = text
                            }
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("XMLParser", "The stream cannot be parsed, error: ${e.message} ")
        }
        return items
    }

}