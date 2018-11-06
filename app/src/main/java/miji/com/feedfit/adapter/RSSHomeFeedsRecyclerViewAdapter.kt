package miji.com.feedfit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_home_feed.view.*
import miji.com.feedfit.R
import miji.com.feedfit.fragments.RSSHomeFragment
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.HTMLParser
import org.jsoup.select.Elements

class RSSHomeFeedsRecyclerViewAdapter(
        private val mValues: RealmList<RSSEntry>? = null,
        private val mListener: RSSHomeFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSHomeFeedsRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RSSEntry
            mListener?.onListFragmentInteraction(item, RSSHomeFragment.FRAGMENTID)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_home_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.feedTitle.text = item?.title
        holder.feedAuthor.text = item?.author
        val test: Elements = HTMLParser.getImageUrl(HTMLParser.parseFromString(item?.content!!))
        if (!test.isEmpty()) {
            Picasso.get().load(test[0].attr("src")).into(holder.feedImage)
        } else {
            Picasso.get().load("https://image.flaticon.com/icons/png/128/9/9550.png").into(holder.feedImage)
        }


        /*holder.feedImage.setImageBitmap(null)
        when {
            !item!!.logo.isNullOrBlank() && item.logo!!.matches(Regex(".*\\.(a?png|jpe?g|giff?|tiff|bmp|)"))  -> Picasso.get().load(item.logo).error(R.drawable.noimage).into(holder.imageFeed)
            !item.icon.isNullOrBlank()  -> Picasso.get().load(item.icon).error(R.drawable.noimage).into(holder.imageFeed)
            else ->   Picasso.get().load(item.icon).error(R.drawable.noimage).into(holder.imageFeed)
        }*/


        holder.itemView.tag = item
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val feedTitle: TextView = mView.feed_title
        val feedAuthor: TextView = mView.feed_author
        val feedImage: ImageView = mView.feed_image
    }
}
