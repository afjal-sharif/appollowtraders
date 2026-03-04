package appollowtraders.afjal

import android.annotation.SuppressLint
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView
    lateinit var swipeRefresh: SwipeRefreshLayout

    val websiteURL = "https://appollowtraders.cdndirect.workers.dev/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {

                if (!isOnline()) {

                    webView.loadUrl("file:///android_asset/offline.html")
                }
            }
        }

        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        if (isOnline()) {

            webView.loadUrl(websiteURL)

        } else {

            webView.loadUrl("file:///android_asset/offline.html")
        }
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }

    private fun isOnline(): Boolean {

        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false

        val capabilities = cm.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun copyToClipboard(text: String) {

        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("copied_text", text)

        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun shareText(text: String) {

        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)

        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}
