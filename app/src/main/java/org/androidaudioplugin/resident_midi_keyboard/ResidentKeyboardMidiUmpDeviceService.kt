package org.androidaudioplugin.resident_midi_keyboard

import android.media.midi.MidiReceiver
import android.media.midi.MidiUmpDeviceService
import androidx.annotation.RequiresApi
import dev.atsushieno.ktmidi.Midi1ToUmpTranslatorContext
import dev.atsushieno.ktmidi.MidiProtocolVersion
import dev.atsushieno.ktmidi.UmpTranslator
import dev.atsushieno.ktmidi.toPlatformNativeBytes
import org.androidaudioplugin.composeaudiocontrols.midi.MidiEventSender
import org.androidaudioplugin.composeaudiocontrols.midi.MidiKeyboardInputDispatcher

@RequiresApi(35)
class ResidentKeyboardMidiUmpDeviceService : MidiUmpDeviceService() {
    private val sender: MidiEventSender = { mevent, offset, length, timestampInNanoSeconds ->
        // If the input controller sends MIDI1, up-convert to UMP
        if (!MidiKeyboardInputDispatcher.useUmp) {
            val context = Midi1ToUmpTranslatorContext(
                midi1 = mevent.toList().drop(offset).take(length),
                group = 0,
                midiProtocol = MidiProtocolVersion.MIDI2,
                isMidi1Smf = false,
                useSysex8 = false
            )
            UmpTranslator.translateMidi1BytesToUmp(context)
            val result = context.output.map { it.toPlatformNativeBytes() }
            outputPortReceivers.forEach {
                result.forEach { ev -> it.send(ev, 0, ev.size, timestampInNanoSeconds) }
            }
        }
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
    private val inputPortReceivers = listOf<MidiReceiver>(replyHandler)
    override fun onGetInputPortReceivers() = inputPortReceivers
}
