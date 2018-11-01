package miji.com.feedfit.adapter

//import kotlinx.android.synthetic.main.fragment_rss_home_feed.view.*
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
import java.util.*

class RSSNewFeedsRecyclerViewAdapter(
        private val mValues: RealmList<RSSEntry>? = null,
        private val mLinkCompare: HashMap<String, String>? = null,
        private val mListener: RSSNewFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSNewFeedsRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener
    private val favoriteRSSEntry: ArrayList<String>? = null

    init {
        mOnClickListener = View.OnClickListener { v ->
            // Notify the active callbacks interface (the activity, if the fragment is attached to  one) that an item has been selected.
            val item = v.tag as RSSEntry
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_new_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.feedTitle.text = mValues.size.toString()
        holder.feedSummary.text = item?.content
        holder.btnAdd.setOnClickListener {
            holder.btnAdd.setBackgroundResource(R.drawable.ic_favorite_feed_selected)
            if(mLinkCompare?.get(item?.link) != null){
                favoriteRSSEntry?.add(item?.link!!)
            }
        }

    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val feedTitle: TextView = mView.feed_title_news
        val feedSummary: TextView = mView.feed_summary_news
        val btnAdd: Button =  mView.btn_add_favorite
    }
}
