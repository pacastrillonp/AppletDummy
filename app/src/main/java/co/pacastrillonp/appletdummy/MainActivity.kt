package co.pacastrillonp.appletdummy

import android.annotation.SuppressLint
import android.os.Bundle
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
import co.pacastrillonp.appletdummy.ui.theme.AppletDummyTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

}

@Composable
fun LocalWebPage() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val webView = rememberWebViewWithClient { webView ->
        webView.webViewClient = WebViewClient()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            webView.apply {
                loadLocalFile(context)
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

fun WebView.loadLocalFile(context: android.content.Context) {
    val storageRepository = StorageRepositoryImpl(context)
    storageRepository.mediaPath?.let { mediaPath ->
        val file = File(mediaPath, "index.html")
        if (file.exists()) {
            val path = file.absolutePath
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
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
    }
    webViewClient(webView)
    return webView
}