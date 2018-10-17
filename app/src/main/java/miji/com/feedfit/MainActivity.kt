package miji.com.feedfit

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import miji.com.feedfit.fragments.PlaceholderFragment
import miji.com.feedfit.fragments.RSSHomeFragment
import miji.com.feedfit.model.RSS

class MainActivity : AppCompatActivity(), RSSHomeFragment.OnListFragmentInteractionListener {



    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var realm: Realm
    private var linearLayout: LinearLayout? = null


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

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> RSSHomeFragment()
                else -> PlaceholderFragment.newInstance(position + 1)
            }

        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    override fun onListFragmentInteraction(item: RSS?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val tag: String = data!!.getStringExtra("TAG")
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        fragment?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
