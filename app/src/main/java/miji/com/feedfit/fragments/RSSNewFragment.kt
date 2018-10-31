package miji.com.feedfit.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_home_list.*
import miji.com.feedfit.R
import miji.com.feedfit.adapter.RSSNewFeedsRecyclerViewAdapter
import miji.com.feedfit.adapter.RSSNewRecyclerViewAdapter
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.WebController
import java.util.*
import java.util.Random


class RSSNewFragment : Fragment() {

    private var columnCount = 1
    private var listenerNew: OnListFragmentInteractionListener? = null
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
    private var i = 0
    private val random = Random()
    private var position = 0
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
                adapter = RSSNewRecyclerViewAdapter(rss, listenerNew)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = homeRecyclerView
        refreshLayout = swipeRefreshLayout
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        refreshLayout.setOnRefreshListener {
            val progressBar = scanProgressBar
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
        }
        testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listenerNew = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listenerNew = null
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
            feedItems.forEach {i,item -> currentList.add(item) }
           currentList.forEach {
                rss ->
               rssEntry = rss.entries[randomPosition(rss.entries.size)]
               feedItemsLinks.put(rssEntry!!.link!!,rss.link!!)
               currentListRssEntries += rssEntry
            }

            val adapter = RSSNewFeedsRecyclerViewAdapter(currentListRssEntries,feedItemsLinks,listenerNew)

            recyclerView.adapter = adapter

            scanProgressBar.isIndeterminate = false
            scanProgressBar.visibility = View.GONE
            if (snackbar != null) {
                snackbar!!.dismiss()
            }

        } else if (resultCode == WebController.FETCH_TIMEOUT) {
            snackbar = Snackbar.make(view!!.findViewById(R.id.main_content), getString(R.string.error_timeout), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.no_connection_retry)) {
                        scanProgressBar.isIndeterminate = false
                        scanProgressBar.visibility = View.GONE
                    }
            snackbar!!.show()
            scanProgressBar.isIndeterminate = false
            scanProgressBar.visibility = View.GONE
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
        val adapter = RSSNewFeedsRecyclerViewAdapter(item, feedItemsLinks, listenerNew)
        recyclerView.adapter = adapter
    }

    fun onBackPress(): Boolean {
        TODO("Implementar la función de atrás")
        //return false
    }

    fun randomPosition(max: Int): Int {
        if (used?.size != max) {
            var num: Int;
            var repe: Boolean = false;
            do {
                num = random.nextInt(max)+0
                repe = wasUsed(num)
            } while (repe != false)
            used?.add(num)
            return num
        } else {
            return 0
        }
    }

    fun wasUsed(num: Number): Boolean {
        var repe: Boolean = false
        used?.forEach { i ->
            if (num == used[i]) {
                repe = true;
            }
        }
        return repe;
    }

}
