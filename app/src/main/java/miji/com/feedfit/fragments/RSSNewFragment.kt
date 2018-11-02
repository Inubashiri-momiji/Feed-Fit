package miji.com.feedfit.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_new_list.*
import miji.com.feedfit.R
import miji.com.feedfit.adapter.RSSNewFeedsRecyclerViewAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_rss_new_list.view.*
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.WebController
import miji.com.feedfit.adapter.RSSNewRecyclerViewAdapter
import java.util.*
import android.R.id.button1
import android.graphics.Color
import android.widget.Button
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RSSNewFragment : Fragment() {

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var testURLs2: ArrayList<String> ?= null
    private var rss: RealmList<RSS> = RealmList()
    private val feedItemsNF: HashMap<String, RSS> = HashMap()
    private val feedItemsLinks: HashMap<String, String> = HashMap()
    private val categoriesChannels: HashMap<String, ArrayList<String>> = HashMap()
    private var snackbar: Snackbar? = null
    private val random = Random()
    private var rssEntry: RSSEntry? = null
    private val currentListRssEntries: RealmList<RSSEntry> = RealmList()
    private val used: ArrayList<Int>? = null
    private var key : String = "World"

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
        hashmapChannels()
        testURLs2 = categoriesChannels?.get(selectCategory())
        refreshLayout.setOnRefreshListener {
            val progressBar = scanProgressBarNews
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
       testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
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
        if (resultCode == WebController.FETCH_SUCCESS && data != null ) {
            //&& data.getParcelableExtra(WebController.REQUEST_TYPE) == WebController.REQUEST_NEW_CONTENT
            val rssNF: RSS = data.getParcelableExtra(WebController.PARCELABLE_EXTRAS)
            val currentListRss: RealmList<RSS> = RealmList()
            feedItemsNF[rssNF.link!!] = rssNF
            feedItemsNF.forEach { _, item -> currentListRss.add(item) }
           currentListRss.forEach { item ->
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

    private fun hashmapChannels(){
        categoriesChannels.put("Entertainment", ArrayList(Arrays.asList("http://www.npr.org/rss/rss.php?id=1008","http://www.newyorker.com/feed/humor","http://www.npr.org/rss/rss.php?id=13","http://www.npr.org/rss/rss.php?id=1045","https://www.yahoo.com/news/rss/entertainment")))
        categoriesChannels.put("Science", ArrayList(Arrays.asList( "http://hosted.ap.org/lineups/SCIENCEHEADS-rss_2.0.xml?SITE=OHLIM&SECTION=HOMEy", "https://www.yahoo.com/news/rss/world", "http://feeds.nature.com/nature/rss/current", "http://www.nasa.gov/rss/image_of_the_day.rss","https://www.yahoo.com/news/rss/science")))
        categoriesChannels.put("Sports", ArrayList(Arrays.asList( "http://hosted.ap.org/lineups/SPORTSHEADS-rss_2.0.xml?SITE=VABRM&SECTION=HOME", "http://www.si.com/rss/si_topstories.rss", "http://feeds1.nytimes.com/nyt/rss/Sports", "http://www.nba.com/jazz/rss.xml","https://www.yahoo.com/news/rss/sports")))
        categoriesChannels.put("Technology", ArrayList(Arrays.asList("http://feeds.wired.com/wired/index", "http://feeds.nytimes.com/nyt/rss/Technology", "http://www.npr.org/rss/rss.php?id=1019", "http://www.techworld.com/news/rss","https://www.yahoo.com/news/rss/tech")))
        categoriesChannels.put("World", ArrayList(Arrays.asList( "http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml","https://www.yahoo.com/news/rss/world", "https://www.crhoy.com/feed/","https://www.yahoo.com/news/rss/world")))
    }

    private fun selectCategory(): String {
        val titleCategory : TextView = view!!.findViewById(R.id.category_title)
        val selectWorld : ImageButton = view!!.findViewById(R.id.btn_world)
        val selectEntertainment : ImageButton = view!!.findViewById(R.id.btn_entertainment)
        val selectScience : ImageButton = view!!.findViewById(R.id.btn_science)
        val selectSports : ImageButton = view!!.findViewById(R.id.btn_sports)
        val selectTechnology : ImageButton = view!!.findViewById(R.id.btn_technology)

        selectWorld.setOnClickListener{
            titleCategory.text = "World News"
            selectWorld.setBackgroundColor(Color.GRAY)
            selectEntertainment.setBackgroundColor(Color.LTGRAY)
            selectScience.setBackgroundColor(Color.LTGRAY)
            selectSports.setBackgroundColor(Color.LTGRAY)
            selectTechnology.setBackgroundColor(Color.LTGRAY)
            key = "World"
        }
        selectEntertainment.setOnClickListener{
            titleCategory.text = "Entertaiment News"
            selectWorld.setBackgroundColor(Color.LTGRAY)
            selectEntertainment.setBackgroundColor(Color.GRAY)
            selectScience.setBackgroundColor(Color.LTGRAY)
            selectSports.setBackgroundColor(Color.LTGRAY)
            selectTechnology.setBackgroundColor(Color.LTGRAY)
            key = "Entertaiment"
        }
        selectScience.setOnClickListener{
            titleCategory.text = "Science News"
            selectWorld.setBackgroundColor(Color.LTGRAY)
            selectEntertainment.setBackgroundColor(Color.LTGRAY)
            selectScience.setBackgroundColor(Color.GRAY)
            selectSports.setBackgroundColor(Color.LTGRAY)
            selectTechnology.setBackgroundColor(Color.LTGRAY)
            key = "Science"
        }
        selectSports.setOnClickListener{
            titleCategory.text = "Sports News"
            selectWorld.setBackgroundColor(Color.LTGRAY)
            selectEntertainment.setBackgroundColor(Color.LTGRAY)
            selectScience.setBackgroundColor(Color.LTGRAY)
            selectSports.setBackgroundColor(Color.GRAY)
            selectTechnology.setBackgroundColor(Color.LTGRAY)
            key = "Sports"
        }
        selectTechnology.setOnClickListener{
            titleCategory.text = "Technology News"
            selectWorld.setBackgroundColor(Color.LTGRAY)
            selectEntertainment.setBackgroundColor(Color.LTGRAY)
            selectScience.setBackgroundColor(Color.LTGRAY)
            selectSports.setBackgroundColor(Color.LTGRAY)
            selectTechnology.setBackgroundColor(Color.GRAY)
            key = "Technology"
        }
        return key
    }

}
