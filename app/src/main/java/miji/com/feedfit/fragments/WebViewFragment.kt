package miji.com.feedfit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_view.*
import miji.com.feedfit.R

class WebViewFragment : Fragment() {
    private var html: String? = null
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            html = it.getString("content")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webView = htmlWebView
        webView.loadData(html, "text/html; charset=utf-8", "UTF-8")
        webView.settings.javaScriptEnabled = false
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param html HTML string to show
         * @return A new instance of fragment WebViewFragment.
         */
        @JvmStatic
        fun newInstance(html: String) =
                WebViewFragment().apply {
                    arguments = Bundle().apply {
                        putString("content", html)
                    }
                }
    }
}
