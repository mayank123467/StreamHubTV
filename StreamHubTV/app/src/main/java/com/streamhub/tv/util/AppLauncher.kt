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
            for (pkg in listOf(platform.packageName, platform.mobilePackageName)) {
                try {
                    val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage(pkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(ytIntent)
                    return
                } catch (_: Exception) { }
            }
        }

        val launchIntent = pm.getLeanbackLaunchIntentForPackage(platform.packageName)
            ?: pm.getLaunchIntentForPackage(platform.mobilePackageName)
            ?: pm.getLaunchIntentForPackage(platform.packageName)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return
        }

        try {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url?.takeIf { it.isNotBlank() } ?: platform.fallbackWebUrl)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "${platform.displayName} is not installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
