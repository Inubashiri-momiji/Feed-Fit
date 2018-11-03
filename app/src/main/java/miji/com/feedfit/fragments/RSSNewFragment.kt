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
import kotlinx.android.synthetic.*
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
        selectCategory()
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
        categoriesChannels["World"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/World.xml","http://rss.cnn.com/rss/edition_world.rss", "http://feeds.reuters.com/Reuters/worldNews","https://www.hoy.es/rss/2.0/?section=internacional", "http://www.diarioextra.com/Rss/list/2/internacionales", "https://www.20minutos.es/rss/internacional/"))
        categoriesChannels["Entertainment"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Television.xml","http://rss.cnn.com/rss/edition_entertainment.rss","http://ep00.epimg.net/rss/cultura/television.xml","http://feeds.reuters.com/reuters/entertainment","https://www.hoy.es/rss/2.0/?section=culturas/musica", "https://www.20minutos.es/rss/gente-television/"))
        categoriesChannels["Science"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Science.xml", "http://rss.cnn.com/rss/edition_space.rss", "http://ep00.epimg.net/rss/elpais/ciencia.xml", "http://feeds.reuters.com/reuters/scienceNews","https://www.hoy.es/rss/2.0/?section=sociedad/ciencia", "https://www.20minutos.es/rss/ciencia/"))
        categoriesChannels["Sports"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Sports.xml", "http://www.espn.com/espn/rss/news", "http://rss.cnn.com/rss/edition_sport.rss", "https://news.co.cr/sports/feed/","http://feeds.reuters.com/reuters/sportsNews", "https://www.20minutos.es/rss/deportes/"))
        categoriesChannels["Technology"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Technology.xml", "http://rss.cnn.com/rss/edition_technology.rss", "http://ep00.epimg.net/rss/tecnologia/portada.xml", "https://news.co.cr/technology/feed/","http://feeds.reuters.com/reuters/technologyNews", "https://www.20minutos.es/rss/tecnologia/"))
    }

    private fun selectCategory() {
        testURLs2 = categoriesChannels["World"]
        val titleCategory : TextView = view!!.findViewById(R.id.category_title)
        val selectWorld : ImageButton = view!!.findViewById(R.id.btn_world)
        val selectEntertainment : ImageButton = view!!.findViewById(R.id.btn_entertainment)
        val selectScience : ImageButton = view!!.findViewById(R.id.btn_science)
        val selectSports : ImageButton = view!!.findViewById(R.id.btn_sports)
        val selectTechnology : ImageButton = view!!.findViewById(R.id.btn_technology)

        selectWorld.setOnClickListener{
            titleCategory.text = "World News"
            selectWorld.setBackgroundColor(Color.rgb(170,170,170))
            selectEntertainment.setBackgroundColor(Color.rgb(224,224,224))
            selectScience.setBackgroundColor(Color.rgb(224,224,224))
            selectSports.setBackgroundColor(Color.rgb(224,224,224))
            selectTechnology.setBackgroundColor(Color.rgb(224,224,224))
            testURLs2 = categoriesChannels["World"]
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectEntertainment.setOnClickListener{
            titleCategory.text = "Entertainment News"
            selectWorld.setBackgroundColor(Color.rgb(224,224,224))
            selectEntertainment.setBackgroundColor(Color.rgb(170,170,170))
            selectScience.setBackgroundColor(Color.rgb(224,224,224))
            selectSports.setBackgroundColor(Color.rgb(224,224,224))
            selectTechnology.setBackgroundColor(Color.rgb(224,224,224))
            testURLs2 = categoriesChannels["Entertainment"]
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectScience.setOnClickListener{
            titleCategory.text = "Science News"
            selectWorld.setBackgroundColor(Color.rgb(224,224,224))
            selectEntertainment.setBackgroundColor(Color.rgb(224,224,224))
            selectScience.setBackgroundColor(Color.rgb(170,170,170))
            selectSports.setBackgroundColor(Color.rgb(224,224,224))
            selectTechnology.setBackgroundColor(Color.rgb(224,224,224))
            testURLs2 = categoriesChannels["Science"]
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectSports.setOnClickListener {
            titleCategory.text = "Sports News"
            selectWorld.setBackgroundColor(Color.rgb(224, 224, 224))
            selectEntertainment.setBackgroundColor(Color.rgb(224, 224, 224))
            selectScience.setBackgroundColor(Color.rgb(224, 224, 224))
            selectSports.setBackgroundColor(Color.rgb(170, 170, 170))
            selectTechnology.setBackgroundColor(Color.rgb(224, 224, 224))
            testURLs2 = categoriesChannels["Sports"]
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectTechnology.setOnClickListener{
            titleCategory.text = "Technology News"
            selectWorld.setBackgroundColor(Color.rgb(224,224,224))
            selectEntertainment.setBackgroundColor(Color.rgb(224,224,224))
            selectScience.setBackgroundColor(Color.rgb(224,224,224))
            selectSports.setBackgroundColor(Color.rgb(224,224,224))
            selectTechnology.setBackgroundColor(Color.rgb(170,170,170))
            testURLs2 = categoriesChannels["Technology"]
            testURLs2!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
    }
}
