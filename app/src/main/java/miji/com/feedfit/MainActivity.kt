package miji.com.feedfit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_rss_new_list.*
import miji.com.feedfit.adapter.RSSHomeFeedsRecyclerViewAdapter
import miji.com.feedfit.fragments.PlaceholderFragment
import miji.com.feedfit.fragments.RSSHomeFragment
import miji.com.feedfit.fragments.RSSNewFragment
import miji.com.feedfit.model.RSS
import miji.com.feedfit.model.RSSEntry


class MainActivity : AppCompatActivity(), RSSHomeFragment.OnListFragmentInteractionListener, RSSNewFragment.OnListFragmentInteractionListener {



    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var realm: Realm
    private var tagFragmentHome: String = ""
    private var tagFragmentNewContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mainViewContainer.adapter = mSectionsPagerAdapter
        mainViewContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mainViewContainer))


        Realm.init(this)
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
        val callResult: Boolean
        callResult = (fragment as? RSSHomeFragment)?.onBackPress()!!
        if (!callResult)
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
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
                /*0 -> {
                    val fragment: Fragment = RSSHomeFragment()
                    mPageReferenceMap[position] = fragment
                    fragment
                }*/
                1 -> {
                    val fragment: Fragment = RSSNewFragment()
                    mPageReferenceMap[position] = fragment
                    fragment
                }
                else -> {
                    val fragment: Fragment = PlaceholderFragment.newInstance(position + 1)
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
            // Show 3 total pages.
            return 3
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (getCurrentFragment() !== `object`) {
                mCurrentFragment = `object` as Fragment
            }
            super.setPrimaryItem(container, position, `object`)
        }
    }

    override fun onListFragmentInteraction(item: RSS?) {
       /* val fragment: RSSHomeFragment = supportFragmentManager.findFragmentByTag(tagFragmentHome) as RSSHomeFragment
        fragment.swapAdapter(item!!.entries)*/
        val fragment: RSSNewFragment = supportFragmentManager.findFragmentByTag(tagFragmentHome) as RSSNewFragment
       // fragment.swapAdapter(item!!.entries)
    }

    override fun onListFragmentInteraction(item: RSSEntry?) {
        val fragment: RSSNewFragment = supportFragmentManager.findFragmentByTag(tagFragmentHome) as RSSNewFragment
    //    fragment.showHTML(item?.content!!)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tagFragmentHome = data!!.getStringExtra("TAG")
        val fragment = supportFragmentManager.findFragmentByTag(tagFragmentHome)
        fragment?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
