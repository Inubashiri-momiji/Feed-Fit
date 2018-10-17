package miji.com.feedfit.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_rss_home.view.*
import miji.com.feedfit.R
import miji.com.feedfit.fragments.RSSHomeFragment.OnListFragmentInteractionListener
import miji.com.feedfit.model.RSS


class RSSHomeRecyclerViewAdapter(
        private val mValues: RealmList<RSS>? = null,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<RSSHomeRecyclerViewAdapter.ViewHolder>() {
    private val mOnClickListener: View.OnClickListener


    init {
        mOnClickListener = View.OnClickListener { v ->
            // Notify the active callbacks interface (the activity, if the fragment is attached to  one) that an item has been selected.
            val item = v.tag as RSS/*
            val intent = Intent(this@MainActivity, ArticleActivity::class.java)
            val extras = Bundle()

            extras.putString("RSS_LINK", rssItem.link)
            extras.putString("RSS_", rssItem.en)

            val rssImage = view.findViewById(R.id.rss_image)
            intent.putExtras(extras)

            startActivity(intent)*/
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_rss_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        holder.summaryFeed.text = item?.title
        holder.imageFeed.setImageBitmap(null)
        Picasso.get().load(item?.logo).into(holder.imageFeed)
        holder.itemView.tag = item
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues!!.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val imageFeed: ImageView = mView.rss_image
        val summaryFeed: TextView = mView.rss_title

    }
}
