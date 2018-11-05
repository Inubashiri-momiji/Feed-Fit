package miji.com.feedfit.utilities

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HTMLParser(private val document: Document) {

    companion object {
        fun parse(stream: String): Document { //Genera el parsing del XML mediante el uso de XMLpullParser
            return Jsoup.parse(stream)
        }

        fun getImageUrl(document: Document) {
            document.select("img[src~=(?i)\\.(a?png|jpe?g|giff?|tiff|bmp|)]")
        }

        fun getDocumentURLS(document: Document) {
            document.select("a[href]")
        }
    }

}