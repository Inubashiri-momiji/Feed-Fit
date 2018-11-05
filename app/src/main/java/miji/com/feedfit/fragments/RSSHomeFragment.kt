package miji.com.feedfit.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_rss_home_list.*
import miji.com.feedfit.R
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
        if (realm.isClosed)
            realm = Realm.getDefaultInstance()
        recyclerView = homeRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        loadContent()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
        realm = Realm.getDefaultInstance()
    }

    override fun onDetach() {
        realm.close()
        listener = null
        super.onDetach()
    }


    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: RSS?)
        fun onListFragmentInteraction(item: RSSEntry?, index: Int)
    }


    private fun loadContent() {

        val data = realm.copyFromRealm(realm.where<RSS>().findAll())
        val test: RealmList<RSS> = RealmList()
        test.addAll(data.subList(0, data.size))
        val adapter = RSSHomeRecyclerViewAdapter(test, listener)
        recyclerView.adapter = adapter

    }

    fun swapAdapter(item: RealmList<RSSEntry>) {
        if (recyclerView.adapter is RSSHomeRecyclerViewAdapter) {
            prevAdapter = recyclerView.adapter as RSSHomeRecyclerViewAdapter
        }
        val adapter = RSSHomeFeedsRecyclerViewAdapter(item, listener)
        recyclerView.adapter = adapter
    }

    fun onBackPress(): Int {
        return if (recyclerView.adapter!! !is RSSHomeRecyclerViewAdapter) {
            if (isHtmlOpen) {
                isHtmlOpen = false
                CLOSE_HTML
            } else {
                recyclerView.adapter = prevAdapter
                ADAPTER_CHANGE
            }
        } else {
            RETURN_FIRST_SCREEN
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

    companion object {
        const val FRAGMENTID = 1
        const val CLOSE_HTML = 1
        const val ADAPTER_CHANGE = 0
        const val RETURN_FIRST_SCREEN = 2

    }
}
