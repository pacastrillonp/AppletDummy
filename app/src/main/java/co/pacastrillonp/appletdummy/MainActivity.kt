package co.pacastrillonp.appletdummy

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import co.pacastrillonp.appletdummy.repository.StorageRepositoryImpl
import co.pacastrillonp.appletdummy.repository.module
import co.pacastrillonp.appletdummy.ui.theme.AppletDummyTheme
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.file
import io.ktor.http.content.static
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var server: NettyApplicationEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storageRepository = StorageRepositoryImpl(this)
        storageRepository.mediaPath?.let { mediaPath ->
            val file = File(mediaPath, "index.html")
            if (file.exists()) {
                startServer()
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            AppletDummyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocalWebPage()
                }
            }
        }

    }

    override fun onDestroy() {
        server.stop(0, 0)
        super.onDestroy()
    }

    private fun startServer(mediaPath: String) {
        server = embeddedServer(Netty, port = 8080) {
            module()
            static("/") {
                file("index.html", mediaPath)
            }
        }
        server.start()
    }

}

@Composable
fun LocalWebPage() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val webView = rememberWebViewWithClient { webView ->
        webView.webViewClient = WebViewClient()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            webView.apply {
                loadUrl("http://localhost:8080/api/data")
                lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            webView.destroy()
                        }
                    }
                })
            }
        }
    )
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
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }
    webViewClient(webView)
    return webView
}