package miji.com.feedfit

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import miji.com.feedfit.fragments.RSSFavoritesFragment
import miji.com.feedfit.fragments.RSSNewFragment
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry
import miji.com.feedfit.utilities.WebController


class MainActivity : AppCompatActivity(), RSSFavoritesFragment.OnListFragmentInteractionListener, RSSNewFragment.OnListFragmentInteractionListener {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var realm: Realm
    private var tagFragment: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appName = "<font color=#EEEEEE>Feed</font> <font color=#D4E157>Fit</font>"
        toolbar.title = Html.fromHtml(appName, 0)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mainViewContainer.adapter = mSectionsPagerAdapter
        mainViewContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mainViewContainer))


        Realm.init(this)
        val config: RealmConfiguration = RealmConfiguration.Builder()
                .name("favorites.realm")
                .schemaVersion(42)
                .build()
        Realm.setDefaultConfiguration(config)
        realm = Realm.getDefaultInstance()


        //realm.executeTransaction { realm ->  realm.deleteAll()   }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onResume() {
        super.onResume()
        if (realm.isClosed)
            realm = Realm.getDefaultInstance()

    }

    override fun onStop() {
        realm.close()
        super.onStop()
    }

    override fun onPause() {
        realm.close()
        super.onPause()
    }

    override fun onBackPressed() {
        val fragment = mSectionsPagerAdapter?.getCurrentFragment()
        when (fragment) {
            is RSSNewFragment -> {
                if (fragment.isHtmlOpen) {
                    fragment.isHtmlOpen = false
                }
                super.onBackPressed()
            }
            is RSSFavoritesFragment -> when ((fragment as? RSSFavoritesFragment)?.onBackPress()!!) {
                RSSFavoritesFragment.CLOSE_HTML -> super.onBackPressed()
                RSSFavoritesFragment.RETURN_FIRST_SCREEN -> mainViewContainer.currentItem = RSSNewFragment.FRAGMENTID
            }
        }
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val mPageReferenceMap: HashMap<Int, Fragment> = HashMap()
        private var mCurrentFragment: Fragment? = null
        override fun getItem(position: Int): Fragment {
            return when (position) {
                RSSFavoritesFragment.FRAGMENTID -> {
                    val fragment: Fragment = RSSFavoritesFragment()
                    mPageReferenceMap[position] = fragment
                    fragment
                }
                else -> {
                    val fragment: Fragment = RSSNewFragment()
                    mPageReferenceMap[position] = fragment
                    fragment
                }

            }
        }

        fun getFragment(key: Int): Fragment? {
            return mPageReferenceMap[key]
        }

        fun getCurrentFragment(): Fragment? {
            return mCurrentFragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
            mPageReferenceMap.remove(position)
        }

        override fun getCount(): Int {
            return 2
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (getCurrentFragment() !== `object`) {
                mCurrentFragment = `object` as Fragment
            }
            super.setPrimaryItem(container, position, `object`)
        }
    }

    override fun onListFragmentInteraction(item: RSS?) {
        val fragment: Fragment = mSectionsPagerAdapter!!.getFragment(RSSFavoritesFragment.FRAGMENTID)!!
        if (fragment is RSSFavoritesFragment) {
            fragment.swapAdapter(item!!.entries)
        }
    }

    override fun onListFragmentInteraction(item: RSSEntry?, index: Int) {
        val fragment: Fragment = mSectionsPagerAdapter!!.getFragment(index)!!
        when (fragment) {
            is RSSFavoritesFragment -> fragment.showHTML(item?.content!!)
            is RSSNewFragment -> fragment.showHTML(item?.content!!)
        }
    }

    override fun updateFavorites() {
        val fragment: Fragment = mSectionsPagerAdapter!!.getFragment(RSSFavoritesFragment.FRAGMENTID)!!
        if (fragment is RSSFavoritesFragment) {
            fragment.loadContent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tagFragment = data!!.getStringExtra(WebController.FRAGMENT_TAG)
        val fragment: Fragment = supportFragmentManager.findFragmentByTag(tagFragment)!!

        if (fragment is RSSNewFragment) {
            fragment.onActivityResultNew(resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
