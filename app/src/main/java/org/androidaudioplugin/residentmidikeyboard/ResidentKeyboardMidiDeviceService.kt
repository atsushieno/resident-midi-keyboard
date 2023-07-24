package org.androidaudioplugin.residentmidikeyboard

import android.media.midi.MidiDeviceService
import android.media.midi.MidiReceiver
import org.androidaudioplugin.composeaudiocontrols.midi.MidiEventSender
import org.androidaudioplugin.composeaudiocontrols.midi.MidiKeyboardInputDispatcher

class ResidentKeyboardMidiDeviceService : MidiDeviceService() {

    private val sender: MidiEventSender = { mevent, offset, length, timestampInNanoSeconds ->
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

    override fun onGetInputPortReceivers() = arrayOf<MidiReceiver>() // no "input" port
}