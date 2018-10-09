package miji.com.feedfit.utilities


import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.UnsupportedEncodingException

class WebController : IntentService("WebController") {

    private val parser = XMLParser()
    private var reply: PendingIntent? = null
    private val url = "https://www.reddit.com/r/Granblue_en.rss"


    override fun onHandleIntent(intent: Intent?) {
        if (REQUEST_QUEUE == null) {
            REQUEST_QUEUE = Volley.newRequestQueue(this)
        }
        if (intent != null) {
            reply = intent.getParcelableExtra(PENDING_RESULT)
            REQUEST_QUEUE!!.add(StringRequest(Request.Method.GET, url, onFeedReceived, onErrorResponse)) //método, url, callback, error
        }
    }

    private val onFeedReceived = Response.Listener<String> { response ->
        try {
            val rssItems = parser.parse(String(response.toByteArray(charset("UTF-8"))))
            val result = Intent()
            result.putParcelableArrayListExtra(PARCELABLE_EXTRAS, rssItems)
            reply?.send(this@WebController, FETCH_SUCCESS, result)
        } catch (e: PendingIntent.CanceledException) {
            Log.e("webController", "onHandleIntent pending intent error", e)
        } catch (e: UnsupportedEncodingException) {
            Log.e("webController", "onHandleIntent encoding error", e)
        }
    }

    private val onErrorResponse = Response.ErrorListener { error ->
        if (error is TimeoutError) {
            try {
                reply?.send(FETCH_TIMEOUT)
            } catch (e: PendingIntent.CanceledException) {
                Log.e("webController", "onHandleIntent error", e)
            }

        } else {
            Log.e("webController", error.toString())
        }
    }

    companion object {
        private var REQUEST_QUEUE: RequestQueue? = null
        const val FETCH_SUCCESS = 0
        const val FETCH_TIMEOUT = 1
        //const val CATEGORY = "CATEGORY"
        val PENDING_RESULT = "RSS_SERVICE_PENDING_RESULT"
        const val PARCELABLE_EXTRAS = "PARCELABLE_EXTRAS"
    }


}