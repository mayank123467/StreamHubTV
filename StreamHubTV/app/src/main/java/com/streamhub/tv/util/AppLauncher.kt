package com.streamhub.tv.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.streamhub.tv.model.OttPlatform

object AppLauncher {
    fun openPlatform(context: Context, platform: OttPlatform, url: String? = null) {
        val pm = context.packageManager
        if (!url.isNullOrBlank() && platform == OttPlatform.YOUTUBE) {
            try {
                val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage(platform.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(ytIntent)
                return
            } catch (_: Exception) { }
        }

        val leanback = pm.getLeanbackLaunchIntentForPackage(platform.packageName)
            ?: pm.getLaunchIntentForPackage(platform.packageName)

        if (leanback != null) {
            leanback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(leanback)
        } else {
            Toast.makeText(
                context,
                "${platform.displayName} is not installed on this TV.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
