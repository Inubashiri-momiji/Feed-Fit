package miji.com.feedfit.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_new_list.*
import miji.com.feedfit.R
import miji.com.feedfit.adapter.RSSNewFeedsRecyclerViewAdapter
import miji.com.feedfit.adapter.RSSNewRecyclerViewAdapter
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.WebController
import java.util.*

class RSSNewFragment : Fragment() {

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val testURLs: ArrayList<String> = ArrayList(Arrays.asList( //NEWS
            "http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml",
            "http://www.washingtonpost.com/rss/",
            "https://www.yahoo.com/news/rss/world",
            "http://rssfeeds.usatoday.com/usatoday-NewsTopStories",
            "http://feeds.reuters.com/reuters/topNews",
            "https://www.crhoy.com/feed/"))
    private var rss: RealmList<RSS> = RealmList()
    private val feedItems: HashMap<String, RSS> = HashMap()
    private val feedItemsLinks: HashMap<String, String> = HashMap()
    private var snackbar: Snackbar? = null
    private val random = Random()
    private var rssEntry: RSSEntry? = null
    private val currentListRssEntries: RealmList<RSSEntry> = RealmList()
    private val used: ArrayList<Int>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_new_list, container, false)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = RSSNewRecyclerViewAdapter(rss, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = NewsRecyclerView
        refreshLayout = swipeRefreshLayoutNews
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        refreshLayout.setOnRefreshListener {
            val progressBar = scanProgressBarNews
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
        }
        testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: RSS?)
        fun onListFragmentInteraction(item: RSSEntry?)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == WebController.FETCH_SUCCESS && data != null) {
            val rss: RSS = data.getParcelableExtra(WebController.PARCELABLE_EXTRAS)
            val currentList: RealmList<RSS> = RealmList()
            feedItems[rss.link!!] = rss
            feedItems.forEach { _, item -> currentList.add(item) }
           currentList.forEach { item ->
               rssEntry = item.entries[randomPosition(item.entries.size)]
               feedItemsLinks[rssEntry!!.link!!] = item.link!!
               currentListRssEntries += rssEntry
            }

            val adapter = RSSNewFeedsRecyclerViewAdapter(currentListRssEntries, feedItemsLinks, listener)

            recyclerView.adapter = adapter

            scanProgressBarNews.isIndeterminate = false
            scanProgressBarNews.visibility = View.GONE
            if (snackbar != null) {
                snackbar!!.dismiss()
            }

        } else if (resultCode == WebController.FETCH_TIMEOUT) {
            snackbar = Snackbar.make(view!!.findViewById(R.id.main_content), getString(R.string.error_timeout), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.no_connection_retry)) {
                        scanProgressBarNews.isIndeterminate = false
                        scanProgressBarNews.visibility = View.GONE
                    }
            snackbar!!.show()
            scanProgressBarNews.isIndeterminate = false
            scanProgressBarNews.visibility = View.GONE
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Obtiene  los feeds resultado del controlador web, requiere de una URL para su funcionamiento
     * @param link URL to fetch feeds
     * @param request request type
     * @see WebController
     */
    private fun getFeeds(link: String, request: String) {
        val connectManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectManager.activeNetworkInfo?.isConnected
        val pendingResult = activity?.createPendingResult(0, Intent(), 0)
        val intent = Intent(context, WebController::class.java)
        intent.putExtra(WebController.URL, link)
        intent.putExtra(WebController.PENDING_RESULT, pendingResult)
        intent.putExtra(WebController.REQUEST_TYPE, request)
        intent.putExtra(WebController.FRAGMENT_TAG, tag.toString())
        context?.startService(intent)
        if (!isConnected!!) {
            snackbar = Snackbar.make(view!!.findViewById(R.id.main_content), getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.no_connection_retry)) {
                        val progressBar = view!!.findViewById<ProgressBar>(R.id.scanProgressBar)
                        progressBar.isIndeterminate = true
                        progressBar.visibility = View.VISIBLE
                        getFeeds(link, request)
                    }
            snackbar!!.show()
        }
        refreshLayout.isRefreshing = false
    }

    fun swapAdapter(item: RealmList<RSSEntry>) {
        val adapter = RSSNewFeedsRecyclerViewAdapter(item, feedItemsLinks, listener)
        recyclerView.adapter = adapter
    }

    fun onBackPress(): Boolean {
        return false
    }

    private fun randomPosition(max: Int): Int {
        return if (used?.size != max) {
            var num: Int
            var repe: Boolean
            do {
                num = random.nextInt(max)+0
                repe = wasUsed(num)
            } while (repe)
            used?.add(num)
            num
        } else {
            0
        }
    }

    private fun wasUsed(num: Number): Boolean {
        var repe = false
        used?.forEach { i ->
            if (num == used[i]) {
                repe = true
            }
        }
        return repe
    }

}
