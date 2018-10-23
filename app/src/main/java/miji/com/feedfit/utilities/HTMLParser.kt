package miji.com.feedfit.utilities

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HTMLParser {

    companion object {
        fun parse(stream: String) { //Genera el parsing del XML mediante el uso de XMLpullParser
            Jsoup.parse(stream)
        }

        fun getImageUrl(document: Document) {
            document.select("img[src~=(?i)\\.(a?png|jpe?g|giff?|tiff|bmp|)]")
        }

        fun getDocumentURLS(document: Document) {
            document.select("a[href]")
        }
    }

}