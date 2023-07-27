package org.androidaudioplugin.residentmidikeyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.androidaudioplugin.composeaudiocontrols.midi.MidiKeyboardMain
import org.androidaudioplugin.residentmidikeyboard.ui.theme.ComposeAudioControlsTheme
import kotlin.system.exitProcess


private fun isNotificationPermissionRequired(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    } else {
        false
    }

private fun startForeground(context: Context) {
    with(context) {
        val serviceIntent = Intent(this, MidiKeyboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeAudioControlsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold {
                        title = "ResidentMIDIKeyboard"
                        MidiKeyboardManagerMain(Modifier.padding(it))
                    }
                }
            }
            var lastBackPressed by remember { mutableStateOf(System.currentTimeMillis()) }
            BackHandler {
                if (System.currentTimeMillis() - lastBackPressed < 2000) {
                    finish()
                    exitProcess(0)
                } else
                    Toast.makeText(this, "Tap once more to quit", Toast.LENGTH_SHORT).show()
                lastBackPressed = System.currentTimeMillis()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MidiKeyboardManagerMainPreview() {
    ComposeAudioControlsTheme {
        MidiKeyboardManagerMain()
    }
}

@Composable
private fun Markdown(markdown: String) {
    MarkdownText(markdown = markdown, color = LocalContentColor.current, fontSize = 16.sp,
        modifier = Modifier.padding (20.dp, 10.dp))
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MidiKeyboardManagerMain(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (!Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "Overlay permission is not enabled.", Toast.LENGTH_LONG).show()
    }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Markdown("""
Resident MIDI Keyboard (RMK) is primarily designed to run as an overlay window.
Start over the notification dot (you will have to give the Notification permission first).
""")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isNotificationPermissionRequired(context)) {
            val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            if (!permissionState.status.isGranted) {
                Snackbar(contentColor = MaterialTheme.colorScheme.secondary, containerColor = MaterialTheme.colorScheme.secondaryContainer, action = {
                    TextButton(onClick = {
                        permissionState.launchPermissionRequest()
                    }) {
                        Text("Approve")
                    }
                }) {
                    Text("We need your approval for notification; it contains all the controllers.")
                }
            } else
                startForeground(context)
        }
        else
            startForeground(context)

        if (!Settings.canDrawOverlays(context)) {
            Snackbar(contentColor = MaterialTheme.colorScheme.secondary, containerColor = MaterialTheme.colorScheme.secondaryContainer, action = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(intent)
                }) {
                    Text("Show Settings")
                }
            }) {
                Text("Overlay permission is not approved")
            }
        }

        Markdown("""
----

This main activity demonstrates the keyboard part too (but makes less sense to use it!)

You can connect to a MIDI output, or have your DAW connect to this keyboard, if it supports Android MIDI API.
Note that some DAWs do not actually support this API whereas it is the standard way (their issues, not ours).
""")

        val knobImage = ImageBitmap.imageResource(R.drawable.chromed_knob)

        MidiKeyboardMain(AndroidMidiAccess(context), knobImage)

        Markdown("""
The knob controllers are for various non-note MIDI messages such as CCs, NRPNs, Per-Note Assignable and Registered Controllers.
Drag a knob vertically to change the value. They are typically ranged between 0 and 127.
It supports "fine" mode: hold 1 second on the knob to switch to it. Releasing the knob makes it back to normal mode.

----

Below is an example use case for developers to demonstrate RMK SurfaceView and SurfaceControlViewHost.
Note that you will have to ensure that you allocate necessary space for expanded DropDownMenu e.g. by making container scrollable.
""")
        val surfaceControlClient by remember { mutableStateOf(MidiKeyboardSurfaceControlClient(context)) }
            AndroidView(factory = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    GlobalScope.launch {
                        surfaceControlClient.connectUI(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
                surfaceControlClient.surfaceView
            }, Modifier.size(800.dp, 700.dp))

        Markdown("""
That's all! If you found any bugs please report at: https://github.com/atsushieno/resident-midi-keyboard/issues
and if you have any questions feel free to post at: https://github.com/atsushieno/resident-midi-keyboard/discussions
""")
    }
}
