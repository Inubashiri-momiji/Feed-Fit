package miji.com.feedfit.utilities

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class HTMLParser {

    companion object {

        fun parseFromString(stream: String): Document { //Genera el parsing del XML mediante el uso de XMLpullParser
            return Jsoup.parse(stream)
        }

        fun parseWeb(url: String): Document {
            return Jsoup.connect(url).maxBodySize(20000).get()
        }

        fun getImageUrl(document: Document): Elements {
            return document.select("img[src~=(?i)\\.(a?png|jpe?g|giff?|tiff|bmp|)]")
        }

        fun getFaviconURL(document: Document): String {
            return document.head().select("link[href~=.*\\.(png)]")[0].attr("href")
        }

    }

}