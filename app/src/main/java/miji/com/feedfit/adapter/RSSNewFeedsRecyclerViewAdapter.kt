package miji.com.feedfit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_new_feed.view.*
import miji.com.feedfit.R
import miji.com.feedfit.fragments.RSSNewFragment
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.HTMLParser
import java.net.URI


class RSSNewFeedsRecyclerViewAdapter(
        private val mValues: RealmList<RSSEntry>? = null,
        private val channels: HashMap<String, RSS> = HashMap(),
        private val mListener: RSSNewFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSNewFeedsRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RSSEntry
            mListener?.onListFragmentInteraction(item, RSSNewFragment.FRAGMENTID)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_new_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.feedTitle.text = item?.title
        if (item?.content?.matches(Regex(".*</?.*>.*"))!!) {
            holder.feedSummary.text = (HTMLParser.parseFromString(item.content!!)).body().text()
        } else
            holder.feedSummary.text = item.content

        holder.btnAdd.setOnClickListener {
            val channel: RSS = channels[item.parentLink]!!
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.insertOrUpdate(channel)
            realm.commitTransaction()
            mListener?.updateFavorites()
        }
        val itemDomain = URI(item.parentLink)
        if (itemDomain.host.isNullOrBlank()) {
            holder.feedDomain.text = URI(item.author).host
        } else {
            holder.feedDomain.text = itemDomain.host
        }
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val feedTitle: TextView = mView.feed_title_news
        val feedSummary: TextView = mView.feed_summary_news
        val btnAdd: Button = mView.btn_add_favorite
        val feedDomain: TextView = mView.feed_domain
    }
}
