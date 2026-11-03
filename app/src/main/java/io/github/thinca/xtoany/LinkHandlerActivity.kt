package io.github.thinca.xtoany

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle

const val X_PACKAGE = "com.twitter.android"

class LinkHandlerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let(::handle)
        finish()
    }

    private fun handle(uri: Uri) {
        val text = PostLink.extractText(uri.scheme, uri.host, uri.path) { uri.getQueryParameter(it) }
        if (text == null) {
            openElsewhere(uri)
        } else {
            share(text)
        }
    }

    private fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        val target = ShareTargetStore(this).target
        if (target != null && tryStart(Intent(send).setComponent(target))) return
        tryStart(Intent.createChooser(send, null))
    }

    private fun openElsewhere(uri: Uri) {
        val view = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
        if (tryStart(Intent(view).setPackage(X_PACKAGE))) return
        // Resolving the URL implicitly would route it back to this app, so ask for a browser explicitly.
        val browser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        tryStart(Intent(view).apply { selector = browser })
    }

    private fun tryStart(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
}
