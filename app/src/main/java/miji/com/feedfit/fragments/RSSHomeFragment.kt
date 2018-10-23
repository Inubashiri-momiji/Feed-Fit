package miji.com.feedfit.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_home_list.*
import miji.com.feedfit.R
import miji.com.feedfit.adapter.RSSHomeRecyclerViewAdapter
import miji.com.feedfit.model.RSS
import miji.com.feedfit.utilities.WebController


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [RSSHomeFragment.OnListFragmentInteractionListener] interface.
 */
class RSSHomeFragment : Fragment() {

    companion object {

        const val WIFI = "Wi-Fi"
        const val ANY = "Any"
        const val SO_URL = "https://www.reddit.com/r/Granblue_en.rss"
        private var wifiConnected = false
        private var mobileConnected = false
        var refreshDisplay = true
        var sPref: String? = null
        var snackbar: Snackbar? = null
        val feedItems: HashMap<String, RSS> = HashMap()
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
                RSSHomeFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val testURL = "https://www.reddit.com/r/Granblue_en.rss"
    private var rss: RealmList<RSS> = RealmList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rss_home_list, container, false)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = RSSHomeRecyclerViewAdapter(rss, listener)
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
            getFeeds(testURL)
        }
        val pendingResult = activity?.createPendingResult(0, Intent(), 0)
        val intent = Intent(context, WebController::class.java)
        intent.putExtra(WebController.PENDING_RESULT, pendingResult)
        intent.putExtra("TAG", tag.toString())
        context?.startService(intent)

        val pendingResulttest1 = activity?.createPendingResult(0, Intent(), 0)
        val intenttest1 = Intent(context, WebController::class.java)
        intenttest1.putExtra("URL", "https://www.technologyreview.es/feed.xml")
        intenttest1.putExtra(WebController.PENDING_RESULT, pendingResulttest1)
        intenttest1.putExtra("TAG", tag.toString())
        context?.startService(intenttest1)

        val pendingResulttest2 = activity?.createPendingResult(0, Intent(), 0)
        val intenttest2 = Intent(context, WebController::class.java)
        intenttest2.putExtra("URL", "http://rss.cnn.com/rss/cnn_topstories.rss")
        intenttest2.putExtra(WebController.PENDING_RESULT, pendingResulttest2)
        intenttest2.putExtra("TAG", tag.toString())
        context?.startService(intenttest2)

        val pendingResulttest3 = activity?.createPendingResult(0, Intent(), 0)
        val intenttest3 = Intent(context, WebController::class.java)
        intenttest3.putExtra("URL", "https://www.crhoy.com/feed/")
        intenttest3.putExtra(WebController.PENDING_RESULT, pendingResulttest3)
        intenttest3.putExtra("TAG", tag.toString())
        context?.startService(intenttest3)
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == WebController.FETCH_SUCCESS && data != null) {
            val rss: RSS = data.getParcelableExtra(WebController.PARCELABLE_EXTRAS)
            val adapter = RSSHomeRecyclerViewAdapter(RealmList(rss), listener)

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
     * @param category URL to fetch feeds
     * @see WebController
     */
    private fun getFeeds(category: String) {
        val connectManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectManager.activeNetworkInfo?.isConnected
        val pendingResult = activity?.createPendingResult(0, Intent(), 0)
        val intent = Intent(context!!.applicationContext, WebController::class.java)
        intent.putExtra(WebController.url, category)
        intent.putExtra(WebController.PENDING_RESULT, pendingResult)
        activity?.startService(intent)
        if (!isConnected!!) {
            snackbar = Snackbar.make(view!!.findViewById(R.id.main_content), getString(R.string.no_connection), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.no_connection_retry)) {
                        val progressBar = view!!.findViewById<ProgressBar>(R.id.scanProgressBar)
                        progressBar.isIndeterminate = true
                        progressBar.visibility = View.VISIBLE
                        getFeeds(category)
                    }
            snackbar!!.show()
        }
        refreshLayout.isRefreshing = false
    }

}
