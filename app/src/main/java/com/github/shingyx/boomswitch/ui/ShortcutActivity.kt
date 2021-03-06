package com.github.shingyx.boomswitch.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.shingyx.boomswitch.R
import com.github.shingyx.boomswitch.data.BoomClient
import com.github.shingyx.boomswitch.data.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

private const val ACTION_SWITCH = "com.github.shingyx.boomswitch.SWITCH"

private var toast: Toast? = null

class ShortcutActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_CREATE_SHORTCUT -> createShortcut()
            ACTION_SWITCH -> launch { switchBoom() }
            else -> Timber.w("Unknown intent action ${intent.action}")
        }

        finish()
    }

    private fun createShortcut() {
        val shortcutIntent = Intent(ACTION_SWITCH, null, this, javaClass)
        val iconResource = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
        @Suppress("DEPRECATION") // Use deprecated approach for no icon badge
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name))
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        }
        setResult(Activity.RESULT_OK, intent)
    }

    private suspend fun switchBoom() {
        val deviceInfo = Preferences.bluetoothDeviceInfo
        if (deviceInfo == null) {
            Toast.makeText(this, R.string.select_speaker, Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            return
        }

        BoomClient.switchPower(this, deviceInfo) { progressMessage ->
            runOnUiThread {
                toast?.cancel()
                toast = Toast.makeText(this, progressMessage, Toast.LENGTH_LONG).also {
                    it.show()
                }
            }
        }
    }
}
