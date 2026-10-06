package com.lagradost.cloudstream3.actions.temp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.lagradost.api.Log
import com.lagradost.cloudstream3.actions.OpenInAppAction
import com.lagradost.cloudstream3.actions.updateDurationAndPosition
import com.lagradost.cloudstream3.ui.result.LinkLoadingResult
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.utils.DataStoreHelper.getViewPos
import com.lagradost.cloudstream3.utils.txt

class MpvRexPreviewPackage : MpvRexPackage(
    appName = "REX Player Preview",
    packageName = "xyz.mpv.rex.preview",
)

/**
 * REX Player external player support
 * https://github.com/estiaksoyeb/mpvRex
 */
open class MpvRexPackage(
    appName: String = "REX Player",
    packageName: String = "xyz.mpv.rex",
) : OpenInAppAction(
    appName = txt(appName),
    packageName = packageName,
    intentClass = "xyz.mpv.rex.ui.player.PlayerActivity"
) {
    override val oneSource = true

    override suspend fun putExtra(
        context: Context,
        intent: Intent,
        video: ResultEpisode,
        result: LinkLoadingResult,
        index: Int?
    ) {
        val link = result.links.getOrNull(index ?: 0) ?: return

        intent.apply {
            putExtra("title", video.name)
            setDataAndType(link.url.toUri(), "video/*")

            val headers = link.headers
            if (headers.isNotEmpty()) {
                val flat = headers.entries.flatMap { listOf(it.key, it.value) }.toTypedArray()
                putExtra("headers", flat)
            }

            val subs = result.subs
            if (subs.isNotEmpty()) {
                putExtra("subs", subs.map { it.url.toUri() }.toTypedArray())
                val selected = subs.firstOrNull { it.matchesLanguageCode("en") }?.url?.toUri()
                if (selected != null) {
                    putExtra("subs.enable", arrayOf(selected))
                }
            }

            val position = getViewPos(video.id)?.position
            if (position != null) {
                putExtra("position", position.toInt())
            }

            putExtra("secure_uri", true)
        }
    }

    override fun onResult(activity: Activity, intent: Intent?) {
        val position = intent?.getIntExtra("position", -1) ?: -1
        val duration = intent?.getIntExtra("duration", -1) ?: -1
        Log.d("MPV", "Position: $position, Duration: $duration")
        updateDurationAndPosition(position.toLong(), duration.toLong())
    }
}
