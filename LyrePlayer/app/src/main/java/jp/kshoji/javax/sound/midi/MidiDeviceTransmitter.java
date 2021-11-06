package jp.kshoji.javax.sound.midi;

import androidx.annotation.NonNull;

/**
 * Interface for {@link MidiDevice} transmitter.
 *
 * @author K.Shoji
 */
public interface MidiDeviceTransmitter extends Transmitter {

    /**
     * Get the {@link jp.kshoji.javax.sound.midi.MidiDevice} associated with this instance.
     *
     * @return the {@link jp.kshoji.javax.sound.midi.MidiDevice} associated with this instance.
     */
    @NonNull
    MidiDevice getMidiDevice();
}
