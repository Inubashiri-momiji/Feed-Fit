package miji.com.feedfit.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_rss_home_list.*
import miji.com.feedfit.adapter.RSSHomeFeedsRecyclerViewAdapter
import miji.com.feedfit.adapter.RSSHomeRecyclerViewAdapter
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry


class RSSHomeFragment : Fragment() {
    private lateinit var prevAdapter: RSSHomeRecyclerViewAdapter
    private var isHtmlOpen: Boolean = false
    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    /*private val testURLs: ArrayList<String> = ArrayList(Arrays.asList(
            "https://www.reddit.com/r/Granblue_en.rss",
            "https://www.technologyreview.es/feed.xml",
            "https://www.reddit.com/r/aww/.rss",
            "https://www.crhoy.com/feed/"))*/
    private var rss: RealmList<RSS> = RealmList()
    private lateinit var realm: Realm

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
            //testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
        }

        loadContent()


        //testURLs.forEach { element -> getFeeds(element, WebController.REQUEST_FAVORITES) }
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


    private fun loadContent() {
        realm = Realm.getDefaultInstance()
        val data = realm.where<RSS>().findAll()
        val test: RealmList<RSS> = RealmList()
        test.addAll(data.subList(0, data.size))
        //feedItems.forEach { _, item -> data.add(item) }
        val adapter = RSSHomeRecyclerViewAdapter(test, listener)
        recyclerView.adapter = adapter
        scanProgressBar.isIndeterminate = false
        scanProgressBar.visibility = View.GONE

    }


    /*
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
*/

    fun swapAdapter(item: RealmList<RSSEntry>) {
        if (recyclerView.adapter is RSSHomeRecyclerViewAdapter) {
            prevAdapter = recyclerView.adapter as RSSHomeRecyclerViewAdapter
        }
        val adapter = RSSHomeFeedsRecyclerViewAdapter(item, listener)
        recyclerView.adapter = adapter
    }

    fun onBackPress(): Boolean {
        if (recyclerView.adapter != null && recyclerView.adapter !is RSSHomeRecyclerViewAdapter && !isHtmlOpen) {
            recyclerView.adapter = prevAdapter
            return true
        } else {
            isHtmlOpen = false
            return false
        }
    }

    fun showHTML(html: String) {
        isHtmlOpen = true
        val trans = fragmentManager!!.beginTransaction()
        trans.replace(R.id.home_constraint_layout, WebViewFragment.newInstance(html))
        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        trans.addToBackStack(null)
        trans.commit()
    }
}
