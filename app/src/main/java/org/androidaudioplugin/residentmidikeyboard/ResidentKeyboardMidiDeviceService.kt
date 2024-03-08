package org.androidaudioplugin.residentmidikeyboard

import android.media.midi.MidiDeviceService
import android.media.midi.MidiReceiver
import org.androidaudioplugin.composeaudiocontrols.midi.MidiEventSender
import org.androidaudioplugin.composeaudiocontrols.midi.MidiKeyboardInputDispatcher

class ResidentKeyboardMidiDeviceService : MidiDeviceService() {
    private val sender: MidiEventSender = { mevent, offset, length, timestampInNanoSeconds ->
        // unlike MidiUmpDeviceService implementation, we send the bytes as is here,
        // it is what this app indeed intends, at least as of this version.
        // AAP expects UMP inputs on Android API Level <35 devices.
        outputPortReceivers.forEach { it.send(mevent, offset, length, timestampInNanoSeconds) }
    }

    override fun onCreate() {
        super.onCreate()
        MidiKeyboardInputDispatcher.senders.add(sender)
    }

    override fun onClose() {
        MidiKeyboardInputDispatcher.senders.remove(sender)
        super.onClose()
    }

    private val replyHandler = ResidentKeyboardMidiReceiver()
    private val inputPortReceivers = arrayOf<MidiReceiver>(replyHandler)
    override fun onGetInputPortReceivers() = inputPortReceivers
}

// So, this input channel exists because MIDI 2.0 clients that conform to
// the June 2023 Updates specification would send UMP Stream Configuration Notification
// back to this "device". Here we behave as if we followed the specification and processed
// them, while we actually totally ignore them :/
class ResidentKeyboardMidiReceiver : MidiReceiver() {
    override fun onSend(p0: ByteArray?, p1: Int, p2: Int, p3: Long) {
        // A valid MIDI 2.0 devices that conform to June 2023 Updates would reply to
        // out UMP Stream Configuration Request (replying Stream Configuration Notification).
        // We are brutal, simply discard them all (so far).
    }
}
