package io.github.thinca.xtoany

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi

class SettingsActivity : Activity() {
    private lateinit var store: ShareTargetStore
    private lateinit var targets: RadioGroup
    private lateinit var targetFilter: EditText
    private lateinit var linkStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        store = ShareTargetStore(this)
        targets = findViewById(R.id.targets)
        targetFilter = findViewById(R.id.target_filter)
        linkStatus = findViewById(R.id.link_status)

        targetFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = applyFilter()
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<Button>(R.id.open_own_link_settings).setOnClickListener { openLinkSettings(packageName) }
            findViewById<Button>(R.id.open_x_link_settings).setOnClickListener { openLinkSettings(X_PACKAGE) }
        } else {
            findViewById<View>(R.id.link_settings_section).visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        populateTargets()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            linkStatus.text = describeLinkStatus()
        }
    }

    private fun populateTargets() {
        targets.setOnCheckedChangeListener(null)
        targets.removeAllViews()

        val selected = store.target
        val askEveryTime = RadioButton(this).apply {
            id = View.generateViewId()
            text = getString(R.string.target_ask_every_time)
        }
        targets.addView(askEveryTime)

        val components = mutableMapOf<Int, ComponentName>()
        for (entry in shareTargets()) {
            val button = RadioButton(this).apply {
                id = View.generateViewId()
                text = entry.label
                tag = entry
                val iconSize = resources.getDimensionPixelSize(R.dimen.target_icon_size)
                entry.icon.setBounds(0, 0, iconSize, iconSize)
                setCompoundDrawablesRelative(entry.icon, null, null, null)
                compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.target_icon_padding)
            }
            targets.addView(button)
            components[button.id] = entry.component
        }

        val checkedId = components.entries.firstOrNull { it.value == selected }?.key ?: askEveryTime.id
        targets.check(checkedId)
        targets.setOnCheckedChangeListener { _, id ->
            store.target = components[id]
            applyFilter()
        }
        applyFilter()
    }

    private fun applyFilter() {
        val query = targetFilter.text.toString().trim()
        for (i in 0 until targets.childCount) {
            val button = targets.getChildAt(i) as RadioButton
            val entry = button.tag as? ShareTarget
            val visible = entry == null || button.isChecked || entry.label.contains(query, ignoreCase = true)
            button.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun shareTargets(): List<ShareTarget> {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
        val excludedPackages = setOf(packageName, X_PACKAGE)
        return packageManager.queryIntentActivities(send, 0)
            .filter { it.activityInfo.packageName !in excludedPackages }
            .map { info ->
                val appLabel = info.activityInfo.applicationInfo.loadLabel(packageManager).toString()
                val activityLabel = info.loadLabel(packageManager).toString()
                ShareTarget(
                    component = ComponentName(info.activityInfo.packageName, info.activityInfo.name),
                    label = if (activityLabel == appLabel) appLabel else "$appLabel - $activityLabel",
                    icon = info.loadIcon(packageManager),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun describeLinkStatus(): String {
        val manager = getSystemService(DomainVerificationManager::class.java)
        val state = try {
            manager.getDomainVerificationUserState(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } ?: return getString(R.string.link_status_unknown)

        if (!state.isLinkHandlingAllowed) return getString(R.string.link_handling_disallowed)

        // hostToStateMap is an ArrayMap whose entry set does not support toArray(), which sorting requires.
        val disabledHosts = state.hostToStateMap.toSortedMap()
            .filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }
            .keys
        return if (disabledHosts.isEmpty()) {
            getString(R.string.link_status_all_enabled)
        } else {
            getString(R.string.link_status_some_disabled, disabledHosts.joinToString(", "))
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun openLinkSettings(targetPackage: String) {
        val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:$targetPackage"))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$targetPackage")))
        }
    }

    private class ShareTarget(
        val component: ComponentName,
        val label: String,
        val icon: Drawable,
    )
}
