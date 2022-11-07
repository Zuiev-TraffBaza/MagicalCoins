package magic.game.coinss.ads_view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import magic.game.coinss.MainActivity


class MainAdsView(context: Context) : WebView(context) {
    private var mOfferViewListener: OfferViewListener? = null
    private var mWebViewCustomClient: WebViewCustomClient? = null

    init {
        initSetting()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context) {
        initSetting()
    }

    interface OfferViewListener {
        fun removeLoadingView(progress: Int)
        fun onNewUrlLoaded(url: String)
        fun onStartAnotherIntent(url: String)
        fun logAdsCategories(adsCategories: String?, value: String?)
        fun onInternetError(codeError: Int)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSetting() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = true
        settings.databaseEnabled = true
//        settings.javaScriptCanOpenWindowsAutomatically = true
//        settings.setSupportMultipleWindows(true)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.userAgentString = settings.userAgentString.replace("; wv", "")
        setSoftKeyBoard()
        mWebViewCustomClient = WebViewCustomClient()
        webChromeClient = ChromeCustomClient()
    }

    fun setOfferViewListener(offerViewListener: OfferViewListener?) {
        mOfferViewListener = offerViewListener
        mWebViewCustomClient?.setOfferViewListener(offerViewListener)
        webViewClient = mWebViewCustomClient!!
    }


    private fun getWebLayout(): OnGlobalLayoutListener {
        return object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rootView.getWindowVisibleDisplayFrame(rectangle)
                val displayMetrics = context.resources.displayMetrics
                val height = displayMetrics.heightPixels
                val botHeight = 0
                val diff = height - rectangle.bottom
                if (rootView.bottom - rectangle.bottom > 100 * displayMetrics.density) {
                    if (rootView.paddingBottom != diff) {
                        rootView.setPadding(0, 0, 0, (diff + botHeight))
                    }
                } else {
                    if (rootView.paddingBottom != 0) {
                        rootView.setPadding(0, 0, 0, 0)
                    }
                }
            }

            private val rectangle = Rect()
        }
    }

    private fun setSoftKeyBoard() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            viewTreeObserver.addOnGlobalLayoutListener(getWebLayout())
        }
    }


    inner class ChromeCustomClient : WebChromeClient() {
        private var viewCustom: View? = null

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            mOfferViewListener?.removeLoadingView(newProgress)
            super.onProgressChanged(view, newProgress)
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            super.onShowCustomView(view, callback)
            Log.d("CORE_SDK", "On show custom view!")
            if (viewCustom != null) {
                callback.onCustomViewHidden()
                return
            }

            viewCustom = view
            this@MainAdsView.visibility = GONE
            (parent as FrameLayout).visibility = VISIBLE
            (parent as FrameLayout).addView(viewCustom)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            clearFocus()
            if (viewCustom != null) {
                (parent as FrameLayout).removeView(viewCustom)
            } else {
                (parent as FrameLayout).removeAllViews()
                (parent as FrameLayout).addView(this@MainAdsView)
            }
            viewCustom = null
            this@MainAdsView.visibility = VISIBLE
        }

        override fun onShowFileChooser(
            webView: WebView,
            uploadMsg: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            MainActivity.callback = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            (context as Activity).startActivityForResult(
                Intent.createChooser(
                    i,
                    "File Chooser"
                ), 1
            )
            return true
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            Log.d("CORE_SDK", "On create window. URL: ${view?.url}, msg: ${resultMsg}")
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }
    }

    open inner class WebViewCustomClient : WebViewClient() {
        private var mOfferViewListener: OfferViewListener? = null
        fun setOfferViewListener(offerViewListener: OfferViewListener?) {
            mOfferViewListener = offerViewListener
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            mOfferViewListener?.onNewUrlLoaded(url)
            Log.d("CORE_SDK", "New url loading: $url")
            if (URLUtil.isNetworkUrl(url)) {
                val uri = Uri.parse(url)
                try {
                    mOfferViewListener?.logAdsCategories("ad", uri.getQueryParameter("ad"))
                    mOfferViewListener?.logAdsCategories("vert", uri.getQueryParameter("vert"))
                } catch (e: Exception) {
                }
                return false
            }
            mOfferViewListener?.onStartAnotherIntent(url)
            return true
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            if (error.errorCode == ERROR_HOST_LOOKUP) {
                mOfferViewListener?.onInternetError(error.errorCode)
            }
            super.onReceivedError(view, request, error)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }

    }
}