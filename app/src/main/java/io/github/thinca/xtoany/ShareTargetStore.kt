package io.github.thinca.xtoany

import android.content.ComponentName
import android.content.Context

class ShareTargetStore(context: Context) {
    private val prefs = context.getSharedPreferences("share_target", Context.MODE_PRIVATE)

    var target: ComponentName?
        get() = prefs.getString(KEY_TARGET, null)?.let(ComponentName::unflattenFromString)
        set(value) = prefs.edit().putString(KEY_TARGET, value?.flattenToString()).apply()

    private companion object {
        const val KEY_TARGET = "target"
    }
}
