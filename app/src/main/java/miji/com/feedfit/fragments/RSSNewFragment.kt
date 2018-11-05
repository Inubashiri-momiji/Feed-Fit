package miji.com.feedfit.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_new_list.*
import miji.com.feedfit.R
import miji.com.feedfit.adapter.RSSNewFeedsRecyclerViewAdapter
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.WebController
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RSSNewFragment : Fragment() {


    var isHtmlOpen: Boolean = false
    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var snackbar: Snackbar? = null
    private var currentChannel: String = "World"
    private val categoriesChannels: HashMap<String, ArrayList<String>> = HashMap()
    private val channels: HashMap<String, RSS> = HashMap()
    private val feedEntriesList: RealmList<RSSEntry> = RealmList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_new_list, container, false)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = RSSNewFeedsRecyclerViewAdapter(feedEntriesList, channels, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = NewsRecyclerView
        refreshLayout = swipeRefreshLayoutNews
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        hashMapChannels()
        selectCategory()
        refreshLayout.setOnRefreshListener {
            val progressBar = scanProgressBarNews
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            categoriesChannels[currentChannel]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        categoriesChannels[currentChannel]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
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

    fun onActivityResultNew(resultCode: Int, data: Intent?) {
        if (resultCode == WebController.FETCH_SUCCESS && data != null &&
                data.getStringExtra(WebController.REQUEST_TYPE) == WebController.REQUEST_NEW_CONTENT) {
            val channel: RSS = data.getParcelableExtra(WebController.PARCELABLE_EXTRAS)
            channels[channel.link!!] = channel
            feedEntriesList.addAll(channel.entries)
            Collections.shuffle(feedEntriesList)
            recyclerView.adapter = RSSNewFeedsRecyclerViewAdapter(feedEntriesList, channels, listener)

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


    private fun selectCategory() {
        feedEntriesList.clear()
        recyclerView.adapter = RSSNewFeedsRecyclerViewAdapter(feedEntriesList, channels, listener)
        (recyclerView.adapter as RSSNewFeedsRecyclerViewAdapter).notifyDataSetChanged()
        channels.clear()
        val titleCategory: TextView = view!!.findViewById(R.id.category_title)
        val selectWorld: ImageButton = view!!.findViewById(R.id.btn_world)
        val selectEntertainment: ImageButton = view!!.findViewById(R.id.btn_entertainment)
        val selectScience: ImageButton = view!!.findViewById(R.id.btn_science)
        val selectSports: ImageButton = view!!.findViewById(R.id.btn_sports)
        val selectTechnology: ImageButton = view!!.findViewById(R.id.btn_technology)

        selectWorld.setOnClickListener {
            titleCategory.text = getString(R.string.news_world)
            selectWorld.setBackgroundColor(Color.rgb(170, 170, 170))
            selectEntertainment.setBackgroundColor(Color.rgb(224, 224, 224))
            selectScience.setBackgroundColor(Color.rgb(224, 224, 224))
            selectSports.setBackgroundColor(Color.rgb(224, 224, 224))
            selectTechnology.setBackgroundColor(Color.rgb(224, 224, 224))
            channels.clear()
            categoriesChannels["World"]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectEntertainment.setOnClickListener {
            titleCategory.text = getString(R.string.news_entertainment)
            selectWorld.setBackgroundColor(Color.rgb(224, 224, 224))
            selectEntertainment.setBackgroundColor(Color.rgb(170, 170, 170))
            selectScience.setBackgroundColor(Color.rgb(224, 224, 224))
            selectSports.setBackgroundColor(Color.rgb(224, 224, 224))
            selectTechnology.setBackgroundColor(Color.rgb(224, 224, 224))
            channels.clear()
            categoriesChannels["Entertainment"]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectScience.setOnClickListener {
            titleCategory.text = getString(R.string.news_Science)
            selectWorld.setBackgroundColor(Color.rgb(224, 224, 224))
            selectEntertainment.setBackgroundColor(Color.rgb(224, 224, 224))
            selectScience.setBackgroundColor(Color.rgb(170, 170, 170))
            selectSports.setBackgroundColor(Color.rgb(224, 224, 224))
            selectTechnology.setBackgroundColor(Color.rgb(224, 224, 224))
            channels.clear()
            categoriesChannels["Science"]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectSports.setOnClickListener {
            titleCategory.text = getString(R.string.news_Sports)
            selectWorld.setBackgroundColor(Color.rgb(224, 224, 224))
            selectEntertainment.setBackgroundColor(Color.rgb(224, 224, 224))
            selectScience.setBackgroundColor(Color.rgb(224, 224, 224))
            selectSports.setBackgroundColor(Color.rgb(170, 170, 170))
            selectTechnology.setBackgroundColor(Color.rgb(224, 224, 224))
            channels.clear()
            categoriesChannels["Sports"]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
        selectTechnology.setOnClickListener {
            titleCategory.text = getString(R.string.news_technology)
            selectWorld.setBackgroundColor(Color.rgb(224, 224, 224))
            selectEntertainment.setBackgroundColor(Color.rgb(224, 224, 224))
            selectScience.setBackgroundColor(Color.rgb(224, 224, 224))
            selectSports.setBackgroundColor(Color.rgb(224, 224, 224))
            selectTechnology.setBackgroundColor(Color.rgb(170, 170, 170))
            channels.clear()
            categoriesChannels["Technology"]!!.forEach { element -> getFeeds(element, WebController.REQUEST_NEW_CONTENT) }
        }
    }

    private fun hashMapChannels() {
        categoriesChannels["World"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/World.xml",
                "http://rss.cnn.com/rss/edition_world.rss",
                "http://feeds.reuters.com/Reuters/worldNews",
                "https://www.hoy.es/rss/2.0/?section=internacional",
                "http://www.diarioextra.com/Rss/list/2/internacionales",
                "https://www.20minutos.es/rss/internacional/",
                "http://feeds.skynews.com/feeds/rss/world.xml",
                "https://www.clarin.com/rss/mundo/",
                "https://www.economist.com/international/rss.xml",
                "https://www.cnbc.com/id/100727362/device/rss/rss.html"
        ))
        categoriesChannels["Entertainment"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Television.xml",
                "http://rss.cnn.com/rss/edition_entertainment.rss",
                "http://ep00.epimg.net/rss/cultura/television.xml",
                "http://feeds.reuters.com/reuters/entertainment",
                "https://www.hoy.es/rss/2.0/?section=culturas/musica",
                "https://www.20minutos.es/rss/gente-television/",
                "https://www.clarin.com/rss/espectaculos/cine/",
                "https://www.clarin.com/rss/eespectaculos/musica/",
                "https://www.clarin.com/rss/espectaculos/teatro/",
                "https://www.clarin.com/rss/espectaculos/","http://feeds.skynews.com/feeds/rss/entertainment.xml"
        ))
        categoriesChannels["Science"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Science.xml",
                "http://rss.cnn.com/rss/edition_space.rss",
                "http://ep00.epimg.net/rss/elpais/ciencia.xml",
                "http://feeds.reuters.com/reuters/scienceNews",
                "https://www.hoy.es/rss/2.0/?section=sociedad/ciencia",
                "https://www.20minutos.es/rss/ciencia/",
                "https://www.nasa.gov/rss/dyn/shuttle_station.rss",
                "https://www.nasa.gov/rss/dyn/solar_system.rss",
                "https://www.nasa.gov/rss/dyn/earth.rss",
                "https://www.nasa.gov/rss/dyn/breaking_news.rss"
        ))
        categoriesChannels["Sports"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Sports.xml",
                "http://www.espn.com/espn/rss/news",
                "http://rss.cnn.com/rss/edition_sport.rss",
                "https://news.co.cr/sports/feed/",
                "http://feeds.reuters.com/reuters/sportsNews",
                "https://www.20minutos.es/rss/deportes/",
                "https://www.clarin.com/rss/deportes/futbol/",
                "https://www.clarin.com/rss/deportes/futbol-internacional/"
        ))
        categoriesChannels["Technology"] = ArrayList(Arrays.asList("http://rss.nytimes.com/services/xml/rss/nyt/Technology.xml",
                "http://rss.cnn.com/rss/edition_technology.rss",
                "http://ep00.epimg.net/rss/tecnologia/portada.xml",
                "https://news.co.cr/technology/feed/",
                "http://feeds.reuters.com/reuters/technologyNews",
                "https://www.20minutos.es/rss/tecnologia/","https://www.clarin.com/rss/tecnologia/",
                "http://feeds.skynews.com/feeds/rss/technology.xml",
                "https://www.economist.com/science-and-technology/rss.xml",
                "https://www.cnbc.com/id/19854910/device/rss/rss.html"
        ))
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: RSS?)
        fun onListFragmentInteraction(item: RSSEntry?, index: Int)
        fun onListFragmentInteraction(item: String?)
    }

    fun showHTML(html: String) {
        isHtmlOpen = true
        val trans = fragmentManager!!.beginTransaction()
        trans.replace(R.id.news_constraint_layout, WebViewFragment.newInstance(html))
        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        trans.addToBackStack(null)

        trans.commit()
    }

    companion object {
        const val FRAGMENTID = 0
    }
}
