package miji.com.feedfit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_favorites.view.*
import miji.com.feedfit.R
import miji.com.feedfit.fragments.RSSFavoritesFragment.OnListFragmentInteractionListener
import miji.com.feedfit.model.RSS

class RSSFavoritesRecyclerViewAdapter(
        private val mValues: RealmList<RSS>? = null,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSFavoritesRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener


    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RSS
            mListener?.onListFragmentInteraction(item)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_favorites, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.feedTitle.text = item?.title
        holder.feedSummary.text = item?.subtitle
        holder.imageFeed.setImageBitmap(null)
        when {
            !item!!.logo.isNullOrBlank() && item.logo!!.matches(Regex(".*\\.(a?png|jpe?g|giff?|tiff|bmp|)")) -> Picasso.get().load(item.logo).error(R.drawable.noimage).into(holder.imageFeed)
            !item.icon.isNullOrBlank() -> Picasso.get().load(item.icon).error(R.drawable.noimage).into(holder.imageFeed)
            else -> Picasso.get().load(item.icon).error(R.drawable.noimage).into(holder.imageFeed)
        }

        holder.itemView.tag = item
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val imageFeed: ImageView = mView.rss_image
        val feedTitle: TextView = mView.rss_title
        val feedSummary: TextView = mView.rss_summary
    }
}
