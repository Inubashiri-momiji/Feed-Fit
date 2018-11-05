package miji.com.feedfit.utilities

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class HTMLParser {

    companion object {

        fun parse(stream: String): Document { //Genera el parsing del XML mediante el uso de XMLpullParser
            return Jsoup.parse(stream)
        }

        fun getImageUrl(document: Document): Elements {
            return document.select("img[src~=(?i)\\.(a?png|jpe?g|giff?|tiff|bmp|)]")
        }

        fun getDocumentURLS(document: Document): Elements {
            return document.select("a[href]")
        }
    }

}