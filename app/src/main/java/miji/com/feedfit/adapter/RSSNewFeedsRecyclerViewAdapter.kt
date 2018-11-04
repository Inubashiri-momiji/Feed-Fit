package miji.com.feedfit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_new_feed.view.*
import miji.com.feedfit.R
import miji.com.feedfit.fragments.RSSNewFragment
import miji.com.feedfit.model.RSSEntry

class RSSNewFeedsRecyclerViewAdapter(
        private val mValues: RealmList<RSSEntry>? = null,
        private val mListener: RSSNewFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSNewFeedsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_new_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.feedTitle.text = item?.title
        holder.feedSummary.text = item?.content
        holder.btnAdd.setOnClickListener { v ->
            val entryTag = v.tag as RSSEntry
            mListener?.onListFragmentInteraction(entryTag.parentLink)
        }

    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val feedTitle: TextView = mView.feed_title_news
        val feedSummary: TextView = mView.feed_summary_news
        val btnAdd: Button = mView.btn_add_favorite
    }
}
