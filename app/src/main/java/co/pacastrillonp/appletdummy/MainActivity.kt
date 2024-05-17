package co.pacastrillonp.appletdummy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import co.pacastrillonp.appletdummy.repository.StorageRepositoryImpl
import co.pacastrillonp.appletdummy.ui.theme.AppletDummyTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppletDummyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocalWebPage()
                }
            }
        }
    }
}

@Composable
fun LocalWebPage() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val webView = rememberWebViewWithClient { webView ->
        webView.webViewClient = object : WebViewClient() {

            val okHttpClient = OkHttpClient()

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url.toString()
                Log.d("WebView", "Intercepted URL: $url")

                return try {
                    val okHttpRequest = Request.Builder()
                        .url(url)
                        .addHeader("Origin", "https://example.com")
                        .addHeader("Access-Control-Request-Method", "GET")
                        .addHeader("Access-Control-Request-Headers", "X-Requested-With")
                        .build()

                    val okHttpResponse = okHttpClient.newCall(okHttpRequest).execute()

                    val responseBody = okHttpResponse.body
                    if (responseBody != null) {
                        val inputStream = responseBody.byteStream()
                        val contentType = responseBody.contentType()?.toString() ?: "text/html"
                        val encoding = responseBody.contentType()?.charset()?.name() ?: "UTF-8"

                        WebResourceResponse(contentType, encoding, inputStream)
                    } else {
                        super.shouldInterceptRequest(view, request)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    super.shouldInterceptRequest(view, request)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                webView.destroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            webView.apply {
                loadLocalFile(context)
            }
        }
    )
}

fun WebView.loadLocalFile(context: Context) {
    val storageRepository = StorageRepositoryImpl(context)
    storageRepository.mediaPath?.let { mediaPath ->
        val file = File(mediaPath, "index.html")
        if (file.exists()) {
            val path = file.absolutePath
            Log.d("WebView", "Loading local file: $path")
            loadUrl("file://$path")
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberWebViewWithClient(
    webViewClient: (WebView) -> Unit
): WebView {
    val webView = WebView(LocalContext.current)
    webView.apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(
                    "WebView",
                    "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}"
                )
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }
    webViewClient(webView)
    return webView
}