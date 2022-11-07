package com.big.win.casino.games.main

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.big.win.casino.games.R
import com.big.win.casino.games.databinding.FragmentMainBinding
import com.big.win.casino.games.util.logEvent
import com.big.win.casino.games.util.logFragment
import com.google.firebase.analytics.FirebaseAnalytics

import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val filepathResultCode = 1
    private lateinit var viewModel : MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        val sharedPrefs = requireActivity().getSharedPreferences("save", 0)
        val loadUrl = sharedPrefs.getString("savedUrl", "") ?: ""

        if (loadUrl.isNotEmpty()) {
             loadSite(loadUrl)
        } else {
            viewModel.siteUrl.observe(viewLifecycleOwner){
                it.data?.let { it1 -> loadSite(it1)
                    sharedPrefs.edit().putString("savedUrl", it1).apply()
                }
            }
        }

    }

    private fun loadSite(url : String){
        binding.webView.visibility = View.VISIBLE
        setupWebView()
        implementOnBackPressed()
        binding.webView.loadUrl(url)
        logEvent("open_web", requireContext().applicationContext)
        KeyboardUtil(requireActivity(), binding.webView).enable()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(binding.webView) {
            webViewClient = CustomView(requireContext())
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    uploadMessage = filePathCallback
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = "image/*"
                    startActivityForResult(Intent.createChooser(i, "Image Chooser"),
                        filepathResultCode)
                    return true
                }
            }
            with(settings) {
                userAgentString = userAgentString.replaceAfter(")", "")
                allowContentAccess = true
                CookieManager.getInstance()
                    .setAcceptThirdPartyCookies(binding.webView, true)
                setSupportZoom(false)
                domStorageEnabled = true
                javaScriptEnabled = true
                savePassword = true
                saveFormData = true
                databaseEnabled = true
                CookieManager.getInstance().setAcceptCookie(true)
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = 0
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                useWideViewPort = true
                loadWithOverviewMode = true
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
            }

            isFocusable = true
            isFocusableInTouchMode = true
            isSaveEnabled = true
            setDownloadListener { url: String?, _: String?, _: String?, _: String?, _: Long ->
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                context.startActivity(i)
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == filepathResultCode) {
            uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            uploadMessage = null
        }
    }


    override fun onStart() {
        super.onStart()
        FirebaseAnalytics.getInstance(requireContext()).logFragment(this)
    }


    private fun implementOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) binding.webView.goBack()
                }
            })
    }
}