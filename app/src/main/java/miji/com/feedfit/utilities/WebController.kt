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
import miji.com.feedfit.model.RSS
import java.io.UnsupportedEncodingException

class WebController : IntentService("WebController") {

    private val parser = XMLParser()

    override fun onHandleIntent(intent: Intent?) {
        if (REQUEST_QUEUE == null) {
            REQUEST_QUEUE = Volley.newRequestQueue(this)
        }
        if (intent != null) {
            config[PENDING_RESULT] = intent.getParcelableExtra(PENDING_RESULT)
            config[FRAGMENT_TAG] = intent.getStringExtra(FRAGMENT_TAG)
            config[REQUEST_TYPE] = intent.getStringExtra(REQUEST_TYPE)
            REQUEST_QUEUE!!.add(StringRequest(Request.Method.GET, intent.getStringExtra(URL),
                    onFeedReceived, onErrorResponse)) //método, url, callback, error
        }
    }

    private val onFeedReceived = Response.Listener<String> { response ->
        try {
            val reply = config[PENDING_RESULT] as PendingIntent
            val rssItems: RSS = parser.parse(String(response.toByteArray(charset("UTF-8"))))
            val result = Intent()
            val tag = "" + config[FRAGMENT_TAG] as String
            val type = "" + config[REQUEST_TYPE] as String
            result.putExtra(FRAGMENT_TAG, tag)
            result.putExtra(PARCELABLE_EXTRAS, rssItems)
            result.putExtra(REQUEST_TYPE, type)
            reply.send(applicationContext, FETCH_SUCCESS, result)
        } catch (e: PendingIntent.CanceledException) {
            Log.e("webController", "onHandleIntent pending intent error", e)
        } catch (e: UnsupportedEncodingException) {
            Log.e("webController", "onHandleIntent encoding error", e)
        }
    }

    private val onErrorResponse = Response.ErrorListener { error ->
        if (error is TimeoutError) {
            try {
                val reply = config[PENDING_RESULT] as PendingIntent
                reply.send(FETCH_TIMEOUT)
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
        private val config: HashMap<String, Any> = HashMap()

        //const val CATEGORY = "CATEGORY"
        const val URL = "URL"
        const val PENDING_RESULT = "RSS_PENDING"
        const val REQUEST_FAVORITES = "RSS_FAVORITE"
        const val REQUEST_NEW_CONTENT = "RSS_NEW_CONTENT"
        const val REQUEST_TYPE = "REQUEST_TYPE"
        const val PARCELABLE_EXTRAS = "PARCELABLE_EXTRAS"
        const val FRAGMENT_TAG = "TAG"
    }


}