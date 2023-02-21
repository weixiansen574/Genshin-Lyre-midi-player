/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.sound.midi;

import com.sun.media.sound.StandardMidiFileReader;
import com.sun.media.sound.StandardMidiFileWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.sound.midi.spi.MidiDeviceProvider;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;


/**
 * The {@code MidiSystem} class provides access to the installed MIDI system
 * resources, including devices such as synthesizers, sequencers, and MIDI input
 * and output ports. A typical simple MIDI application might begin by invoking
 * one or more {@code MidiSystem} methods to learn what devices are installed
 * and to obtain the ones needed in that application.
 * <p>
 * The class also has methods for reading files, streams, and URLs that contain
 * standard MIDI file data or soundbanks. You can query the {@code MidiSystem}
 * for the format of a specified MIDI file.
 * <p>
 * You cannot instantiate a {@code MidiSystem}; all the methods are static.
 * <p>
 * Properties can be used to specify default MIDI devices. Both system
 * properties and a properties file are considered. The "sound.properties"
 * properties file is read from an implementation-specific location (typically
 * it is the {@code conf} directory in the Java installation directory).
 * The optional "javax.sound.config.file" system property can be used to specify
 * the properties file that will be read as the initial configuration. If a
 * property exists both as a system property and in the properties file, the
 * system property takes precedence. If none is specified, a suitable default is
 * chosen among the available devices. The syntax of the properties file is
 * specified in {@link Properties#load(InputStream) Properties.load}. The
 * following table lists the available property keys and which methods consider
 * them:
 *
 * <table class="striped">
 * <caption>MIDI System Property Keys</caption>
 * <thead>
 *   <tr>
 *     <th scope="col">Property Key
 *     <th scope="col">Interface
 *     <th scope="col">Affected Method
 * </thead>
 * <tbody>
 *   <tr>
 *     <th scope="row">{@code javax.sound.midi.Receiver}
 *     <td>{@link Receiver}
 *     <td>{@link #getReceiver}
 *   <tr>
 *     <th scope="row">{@code javax.sound.midi.Sequencer}
 *     <td>{@link Sequencer}
 *     <td>{@link #getSequencer}
 *   <tr>
 *     <th scope="row">{@code javax.sound.midi.Synthesizer}
 *     <td>{@link Synthesizer}
 *     <td>{@link #getSynthesizer}
 *   <tr>
 *     <th scope="row">{@code javax.sound.midi.Transmitter}
 *     <td>{@link Transmitter}
 *     <td>{@link #getTransmitter}
 * </tbody>
 * </table>
 *
 * The property value consists of the provider class name and the device name,
 * separated by the hash mark ("#"). The provider class name is the
 * fully-qualified name of a concrete
 * {@link MidiDeviceProvider MIDI device provider} class. The device name is
 * matched against the {@code String} returned by the {@code getName} method of
 * {@code MidiDevice.Info}. Either the class name, or the device name may be
 * omitted. If only the class name is specified, the trailing hash mark is
 * optional.
 * <p>
 * If the provider class is specified, and it can be successfully retrieved from
 * the installed providers, the list of {@code MidiDevice.Info} objects is
 * retrieved from the provider. Otherwise, or when these devices do not provide
 * a subsequent match, the list is retrieved from {@link #getMidiDeviceInfo} to
 * contain all available {@code MidiDevice.Info} objects.
 * <p>
 * If a device name is specified, the resulting list of {@code MidiDevice.Info}
 * objects is searched: the first one with a matching name, and whose
 * {@code MidiDevice} implements the respective interface, will be returned. If
 * no matching {@code MidiDevice.Info} object is found, or the device name is
 * not specified, the first suitable device from the resulting list will be
 * returned. For Sequencer and Synthesizer, a device is suitable if it
 * implements the respective interface; whereas for Receiver and Transmitter, a
 * device is suitable if it implements neither Sequencer nor Synthesizer and
 * provides at least one Receiver or Transmitter, respectively.
 * <p>
 * For example, the property {@code javax.sound.midi.Receiver} with a value
 * {@code "com.sun.media.sound.MidiProvider#SunMIDI1"} will have the following
 * consequences when {@code getReceiver} is called: if the class
 * {@code com.sun.media.sound.MidiProvider} exists in the list of installed MIDI
 * device providers, the first {@code Receiver} device with name
 * {@code "SunMIDI1"} will be returned. If it cannot be found, the first
 * {@code Receiver} from that provider will be returned, regardless of name. If
 * there is none, the first {@code Receiver} with name {@code "SunMIDI1"} in the
 * list of all devices (as returned by {@code getMidiDeviceInfo}) will be
 * returned, or, if not found, the first {@code Receiver} that can be found in
 * the list of all devices is returned. If that fails, too, a
 * {@code MidiUnavailableException} is thrown.
 *
 * @author Kara Kytle
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class MidiSystem {

    /**
     * Private no-args constructor for ensuring against instantiation.
     */
    private MidiSystem() {
    }

    /**
     * Obtains an array of information objects representing the set of all MIDI
     * devices available on the system. A returned information object can then
     * be used to obtain the corresponding device object, by invoking
     * {@link #getMidiDevice(MidiDevice.Info) getMidiDevice}.
     *
     * @return an array of {@code MidiDevice.Info} objects, one for each
     *         installed MIDI device. If no such devices are installed, an array
     *         of length 0 is returned.
     */
    /**
     * Obtains the requested MIDI device.
     *
     * @param  info a device information object representing the desired device
     * @return the requested device
     * @throws MidiUnavailableException if the requested device is not available
     *         due to resource restrictions
     * @throws IllegalArgumentException if the info object does not represent a
     *         MIDI device installed on the system
     * @throws NullPointerException if {@code info} is {@code null}
     * @see #getMidiDeviceInfo
     */
    /**
     * Obtains a MIDI receiver from an external MIDI port or other default
     * device. The returned receiver always implements the
     * {@code MidiDeviceReceiver} interface.
     * <p>
     * If the system property {@code javax.sound.midi.Receiver} is defined or it
     * is defined in the file "sound.properties", it is used to identify the
     * device that provides the default receiver. For details, refer to the
     * {@link MidiSystem class description}.
     * <p>
     * If a suitable MIDI port is not available, the Receiver is retrieved from
     * an installed synthesizer.
     * <p>
     * If a native receiver provided by the default device does not implement
     * the {@code MidiDeviceReceiver} interface, it will be wrapped in a wrapper
     * class that implements the {@code MidiDeviceReceiver} interface. The
     * corresponding {@code Receiver} method calls will be forwarded to the
     * native receiver.
     * <p>
     * If this method returns successfully, the {@link MidiDevice MidiDevice}
     * the {@code Receiver} belongs to is opened implicitly, if it is not
     * already open. It is possible to close an implicitly opened device by
     * calling {@link Receiver#close close} on the returned {@code Receiver}.
     * All open {@code Receiver} instances have to be closed in order to release
     * system resources hold by the {@code MidiDevice}. For a detailed
     * description of open/close behaviour see the class description of
     * {@link MidiDevice MidiDevice}.
     *
     * @return the default MIDI receiver
     * @throws MidiUnavailableException if the default receiver is not available
     *         due to resource restrictions, or no device providing receivers is
     *         installed in the system
     */
    /**
     * Obtains a MIDI transmitter from an external MIDI port or other default
     * source. The returned transmitter always implements the
     * {@code MidiDeviceTransmitter} interface.
     * <p>
     * If the system property {@code javax.sound.midi.Transmitter} is defined or
     * it is defined in the file "sound.properties", it is used to identify the
     * device that provides the default transmitter. For details, refer to the
     * {@link MidiSystem class description}.
     * <p>
     * If a native transmitter provided by the default device does not implement
     * the {@code MidiDeviceTransmitter} interface, it will be wrapped in a
     * wrapper class that implements the {@code MidiDeviceTransmitter}
     * interface. The corresponding {@code Transmitter} method calls will be
     * forwarded to the native transmitter.
     * <p>
     * If this method returns successfully, the {@link MidiDevice MidiDevice}
     * the {@code Transmitter} belongs to is opened implicitly, if it is not
     * already open. It is possible to close an implicitly opened device by
     * calling {@link Transmitter#close close} on the returned
     * {@code Transmitter}. All open {@code Transmitter} instances have to be
     * closed in order to release system resources hold by the
     * {@code MidiDevice}. For a detailed description of open/close behaviour
     * see the class description of {@link MidiDevice MidiDevice}.
     *
     * @return the default MIDI transmitter
     * @throws MidiUnavailableException if the default transmitter is not
     *         available due to resource restrictions, or no device providing
     *         transmitters is installed in the system
     */
    /**
     * Obtains the default synthesizer.
     * <p>
     * If the system property {@code javax.sound.midi.Synthesizer} is defined or
     * it is defined in the file "sound.properties", it is used to identify the
     * default synthesizer. For details, refer to the
     * {@link MidiSystem class description}.
     *
     * @return the default synthesizer
     * @throws MidiUnavailableException if the synthesizer is not available due
     *         to resource restrictions, or no synthesizer is installed in the
     *         system
     */
    /**
     * Obtains the default {@code Sequencer}, connected to a default device. The
     * returned {@code Sequencer} instance is connected to the default
     * {@code Synthesizer}, as returned by {@link #getSynthesizer}. If there is
     * no {@code Synthesizer} available, or the default {@code Synthesizer}
     * cannot be opened, the {@code sequencer} is connected to the default
     * {@code Receiver}, as returned by {@link #getReceiver}. The connection is
     * made by retrieving a {@code Transmitter} instance from the
     * {@code Sequencer} and setting its {@code Receiver}. Closing and
     * re-opening the sequencer will restore the connection to the default
     * device.
     * <p>
     * This method is equivalent to calling {@code getSequencer(true)}.
     * <p>
     * If the system property {@code javax.sound.midi.Sequencer} is defined or
     * it is defined in the file "sound.properties", it is used to identify the
     * default sequencer. For details, refer to the
     * {@link MidiSystem class description}.
     *
     * @return the default sequencer, connected to a default Receiver
     * @throws MidiUnavailableException if the sequencer is not available due to
     *         resource restrictions, or there is no {@code Receiver} available
     *         by any installed {@code MidiDevice}, or no sequencer is installed
     *         in the system
     * @see #getSequencer(boolean)
     * @see #getSynthesizer
     * @see #getReceiver
     */

    /**
     * Obtains the default {@code Sequencer}, optionally connected to a default
     * device.
     * <p>
     * If {@code connected} is true, the returned {@code Sequencer} instance is
     * connected to the default {@code Synthesizer}, as returned by
     * {@link #getSynthesizer}. If there is no {@code Synthesizer} available, or
     * the default {@code Synthesizer} cannot be opened, the {@code sequencer}
     * is connected to the default {@code Receiver}, as returned by
     * {@link #getReceiver}. The connection is made by retrieving a
     * {@code Transmitter} instance from the {@code Sequencer} and setting its
     * {@code Receiver}. Closing and re-opening the sequencer will restore the
     * connection to the default device.
     * <p>
     * If {@code connected} is false, the returned {@code Sequencer} instance is
     * not connected, it has no open {@code Transmitters}. In order to play the
     * sequencer on a MIDI device, or a {@code Synthesizer}, it is necessary to
     * get a {@code Transmitter} and set its {@code Receiver}.
     * <p>
     * If the system property {@code javax.sound.midi.Sequencer} is defined or
     * it is defined in the file "sound.properties", it is used to identify the
     * default sequencer. For details, refer to the
     * {@link MidiSystem class description}.
     *
     * @param  connected whether or not the returned {@code Sequencer} is
     *         connected to the default {@code Synthesizer}
     * @return the default sequencer
     * @throws MidiUnavailableException if the sequencer is not available due to
     *         resource restrictions, or no sequencer is installed in the
     *         system, or if {@code connected} is true, and there is no
     *         {@code Receiver} available by any installed {@code MidiDevice}
     * @see #getSynthesizer
     * @see #getReceiver
     * @since 1.5
     */

    /**
     * Constructs a MIDI sound bank by reading it from the specified stream. The
     * stream must point to a valid MIDI soundbank file. In general, MIDI
     * soundbank providers may need to read some data from the stream before
     * determining whether they support it. These parsers must be able to mark
     * the stream, read enough data to determine whether they support the
     * stream, and, if not, reset the stream's read pointer to its original
     * position. If the input stream does not support this, this method may fail
     * with an {@code IOException}.
     *
     * @param  stream the source of the sound bank data
     * @return the sound bank
     * @throws InvalidMidiDataException if the stream does not point to valid
     *         MIDI soundbank data recognized by the system
     * @throws IOException if an I/O error occurred when loading the soundbank
     * @throws NullPointerException if {@code stream} is {@code null}
     * @see InputStream#markSupported
     * @see InputStream#mark
     */

    /**
     * Constructs a {@code Soundbank} by reading it from the specified
     * {@code File}. The {@code File} must point to a valid MIDI soundbank file.
     *
     * @param  file the source of the sound bank data
     * @return the sound bank
     * @throws InvalidMidiDataException if the {@code File} does not point to
     *         valid MIDI soundbank data recognized by the system
     * @throws IOException if an I/O error occurred when loading the soundbank
     * @throws NullPointerException if {@code file} is {@code null}
     */

    /**
     * Obtains the MIDI file format of the data in the specified input stream.
     * The stream must point to valid MIDI file data for a file type recognized
     * by the system.
     * <p>
     * This method and/or the code it invokes may need to read some data from
     * the stream to determine whether its data format is supported. The
     * implementation may therefore need to mark the stream, read enough data to
     * determine whether it is in a supported format, and reset the stream's
     * read pointer to its original position. If the input stream does not
     * permit this set of operations, this method may fail with an
     * {@code IOException}.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while determining the file format.
     *
     * @param  stream the input stream from which file format information should
     *         be extracted
     * @return an {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the stream does not point to valid
     *         MIDI file data recognized by the system
     * @throws IOException if an I/O exception occurs while accessing the stream
     * @throws NullPointerException if {@code stream} is {@code null}
     * @see #getMidiFileFormat(URL)
     * @see #getMidiFileFormat(File)
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public static MidiFileFormat getMidiFileFormat(final InputStream stream)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(stream);

        List<MidiFileReader> providers = getMidiFileReaders();
        MidiFileFormat format = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader =  providers.get(i);
            try {
                format = reader.getMidiFileFormat( stream ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( format==null ) {
            throw new InvalidMidiDataException("input stream is not a supported file type");
        } else {
            return format;
        }
    }

    /**
     * Obtains the MIDI file format of the data in the specified URL. The URL
     * must point to valid MIDI file data for a file type recognized by the
     * system.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while determining the file format.
     *
     * @param  url the URL from which file format information should be
     *         extracted
     * @return a {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the URL does not point to valid MIDI
     *         file data recognized by the system
     * @throws IOException if an I/O exception occurs while accessing the URL
     * @throws NullPointerException if {@code url} is {@code null}
     * @see #getMidiFileFormat(InputStream)
     * @see #getMidiFileFormat(File)
     */
    public static MidiFileFormat getMidiFileFormat(final URL url)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(url);

        List<MidiFileReader> providers = getMidiFileReaders();
        MidiFileFormat format = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = providers.get(i);
            try {
                format = reader.getMidiFileFormat( url ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( format==null ) {
            throw new InvalidMidiDataException("url is not a supported file type");
        } else {
            return format;
        }
    }

    /**
     * Obtains the MIDI file format of the specified {@code File}. The
     * {@code File} must point to valid MIDI file data for a file type
     * recognized by the system.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while determining the file format.
     *
     * @param  file the {@code File} from which file format information should
     *         be extracted
     * @return a {@code MidiFileFormat} object describing the MIDI file format
     * @throws InvalidMidiDataException if the {@code File} does not point to
     *         valid MIDI file data recognized by the system
     * @throws IOException if an I/O exception occurs while accessing the file
     * @throws NullPointerException if {@code file} is {@code null}
     * @see #getMidiFileFormat(InputStream)
     * @see #getMidiFileFormat(URL)
     */
    public static MidiFileFormat getMidiFileFormat(final File file)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(file);

        List<MidiFileReader> providers = getMidiFileReaders();
        MidiFileFormat format = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = providers.get(i);
            try {
                format = reader.getMidiFileFormat( file ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( format==null ) {
            throw new InvalidMidiDataException("file is not a supported file type");
        } else {
            return format;
        }
    }

    /**
     * Obtains a MIDI sequence from the specified input stream. The stream must
     * point to valid MIDI file data for a file type recognized by the system.
     * <p>
     * This method and/or the code it invokes may need to read some data from
     * the stream to determine whether its data format is supported. The
     * implementation may therefore need to mark the stream, read enough data to
     * determine whether it is in a supported format, and reset the stream's
     * read pointer to its original position. If the input stream does not
     * permit this set of operations, this method may fail with an
     * {@code IOException}.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while constructing the {@code Sequence}
     * object from the file data.
     *
     * @param  stream the input stream from which the {@code Sequence} should be
     *         constructed
     * @return a {@code Sequence} object based on the MIDI file data contained
     *         in the input stream
     * @throws InvalidMidiDataException if the stream does not point to valid
     *         MIDI file data recognized by the system
     * @throws IOException if an I/O exception occurs while accessing the stream
     * @throws NullPointerException if {@code stream} is {@code null}
     * @see InputStream#markSupported
     * @see InputStream#mark
     */
    public static Sequence getSequence(final InputStream stream)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(stream);

        List<MidiFileReader> providers = getMidiFileReaders();
        Sequence sequence = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = providers.get(i);
            try {
                sequence = reader.getSequence( stream ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from input stream");
        } else {
            return sequence;
        }
    }

    /**
     * Obtains a MIDI sequence from the specified URL. The URL must point to
     * valid MIDI file data for a file type recognized by the system.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while constructing the {@code Sequence}
     * object from the file data.
     *
     * @param  url the URL from which the {@code Sequence} should be constructed
     * @return a {@code Sequence} object based on the MIDI file data pointed to
     *         by the URL
     * @throws InvalidMidiDataException if the URL does not point to valid MIDI
     *         file data recognized by the system
     * @throws IOException if an I/O exception occurs while accessing the URL
     * @throws NullPointerException if {@code url} is {@code null}
     */
    public static Sequence getSequence(final URL url)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(url);

        List<MidiFileReader> providers = getMidiFileReaders();
        Sequence sequence = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = providers.get(i);
            try {
                sequence = reader.getSequence( url ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from URL");
        } else {
            return sequence;
        }
    }

    /**
     * Obtains a MIDI sequence from the specified {@code File}. The {@code File}
     * must point to valid MIDI file data for a file type recognized by the
     * system.
     * <p>
     * This operation can only succeed for files of a type which can be parsed
     * by an installed file reader. It may fail with an
     * {@code InvalidMidiDataException} even for valid files if no compatible
     * file reader is installed. It will also fail with an
     * {@code InvalidMidiDataException} if a compatible file reader is
     * installed, but encounters errors while constructing the {@code Sequence}
     * object from the file data.
     *
     * @param  file the {@code File} from which the {@code Sequence} should be
     *         constructed
     * @return a {@code Sequence} object based on the MIDI file data pointed to
     *         by the File
     * @throws InvalidMidiDataException if the File does not point to valid MIDI
     *         file data recognized by the system
     * @throws IOException if an I/O exception occurs
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public static Sequence getSequence(final File file)
            throws InvalidMidiDataException, IOException {
        Objects.requireNonNull(file);

        List<MidiFileReader> providers = getMidiFileReaders();
        Sequence sequence = null;

        for(int i = 0; i < providers.size(); i++) {
            MidiFileReader reader = providers.get(i);
            try {
                sequence = reader.getSequence( file ); // throws IOException
                break;
            } catch (InvalidMidiDataException e) {
                continue;
            }
        }

        if( sequence==null ) {
            throw new InvalidMidiDataException("could not get sequence from file");
        } else {
            return sequence;
        }
    }

    /**
     * Obtains the set of MIDI file types for which file writing support is
     * provided by the system.
     *
     * @return array of unique file types. If no file types are supported, an
     *         array of length 0 is returned.
     */
    public static int[] getMidiFileTypes() {

        List<MidiFileWriter> providers = getMidiFileWriters();
        Set<Integer> allTypes = new HashSet<>();

        // gather from all the providers

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            int[] types = writer.getMidiFileTypes();
            for (int j = 0; j < types.length; j++ ) {
                allTypes.add(types[j]);
            }
        }
        int[] resultTypes = new int[allTypes.size()];
        int index = 0;
        for (Integer integer : allTypes) {
            resultTypes[index++] = integer.intValue();
        }
        return resultTypes;
    }

    /**
     * Indicates whether file writing support for the specified MIDI file type
     * is provided by the system.
     *
     * @param  fileType the file type for which write capabilities are queried
     * @return {@code true} if the file type is supported, otherwise
     *         {@code false}
     */
    public static boolean isFileTypeSupported(int fileType) {

        List<MidiFileWriter> providers = getMidiFileWriters();

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            if( writer.isFileTypeSupported(fileType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtains the set of MIDI file types that the system can write from the
     * sequence specified.
     *
     * @param  sequence the sequence for which MIDI file type support is queried
     * @return the set of unique supported file types. If no file types are
     *         supported, returns an array of length 0.
     * @throws NullPointerException if {@code sequence} is {@code null}
     */
    public static int[] getMidiFileTypes(final Sequence sequence) {
        Objects.requireNonNull(sequence);

        List<MidiFileWriter> providers = getMidiFileWriters();
        Set<Integer> allTypes = new HashSet<>();

        // gather from all the providers

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            int[] types = writer.getMidiFileTypes(sequence);
            for (int j = 0; j < types.length; j++ ) {
                allTypes.add(types[j]);
            }
        }
        int[] resultTypes = new int[allTypes.size()];
        int index = 0;
        for (Integer integer : allTypes) {
            resultTypes[index++] = integer.intValue();
        }
        return resultTypes;
    }

    /**
     * Indicates whether a MIDI file of the file type specified can be written
     * from the sequence indicated.
     *
     * @param  fileType the file type for which write capabilities are queried
     * @param  sequence the sequence for which file writing support is queried
     * @return {@code true} if the file type is supported for this sequence,
     *         otherwise {@code false}
     * @throws NullPointerException if {@code sequence} is {@code null}
     */
    public static boolean isFileTypeSupported(final int fileType,
                                              final Sequence sequence) {
        Objects.requireNonNull(sequence);

        List<MidiFileWriter> providers = getMidiFileWriters();

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            if( writer.isFileTypeSupported(fileType,sequence)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes a stream of bytes representing a file of the MIDI file type
     * indicated to the output stream provided.
     *
     * @param  in sequence containing MIDI data to be written to the file
     * @param  fileType the file type of the file to be written to the output
     *         stream
     * @param  out stream to which the file data should be written
     * @return the number of bytes written to the output stream
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if the file format is not supported by
     *         the system
     * @throws NullPointerException if {@code in} or {@code out} are
     *         {@code null}
     * @see #isFileTypeSupported(int, Sequence)
     * @see #getMidiFileTypes(Sequence)
     */
    public static int write(final Sequence in, final int fileType,
                            final OutputStream out) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(out);

        List<MidiFileWriter> providers = getMidiFileWriters();
        //$$fb 2002-04-17: Fix for 4635287: Standard MidiFileWriter cannot write empty Sequences
        int bytesWritten = -2;

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            if( writer.isFileTypeSupported( fileType, in ) ) {

                bytesWritten = writer.write(in, fileType, out);
                break;
            }
        }
        if (bytesWritten == -2) {
            throw new IllegalArgumentException("MIDI file type is not supported");
        }
        return bytesWritten;
    }

    /**
     * Writes a stream of bytes representing a file of the MIDI file type
     * indicated to the external file provided.
     *
     * @param  in sequence containing MIDI data to be written to the file
     * @param  type the file type of the file to be written to the output stream
     * @param  out external file to which the file data should be written
     * @return the number of bytes written to the file
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if the file type is not supported by the
     *         system
     * @throws NullPointerException if {@code in} or {@code out} are
     *         {@code null}
     * @see #isFileTypeSupported(int, Sequence)
     * @see #getMidiFileTypes(Sequence)
     */
    public static int write(final Sequence in, final int type, final File out)
            throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(out);

        List<MidiFileWriter> providers = getMidiFileWriters();
        //$$fb 2002-04-17: Fix for 4635287: Standard MidiFileWriter cannot write empty Sequences
        int bytesWritten = -2;

        for (int i = 0; i < providers.size(); i++ ) {
            MidiFileWriter writer = providers.get(i);
            if( writer.isFileTypeSupported( type, in ) ) {

                bytesWritten = writer.write(in, type, out);
                break;
            }
        }
        if (bytesWritten == -2) {
            throw new IllegalArgumentException("MIDI file type is not supported");
        }
        return bytesWritten;
    }

    // HELPER METHODS

    /**
     * Obtains the list of MidiDeviceProviders installed on the system.
     *
     * @return the list of MidiDeviceProviders installed on the system
     */
    /**
     * Obtains the list of SoundbankReaders installed on the system.
     *
     * @return the list of SoundbankReaders installed on the system
     */
    /**
     * Obtains the list of MidiFileWriters installed on the system.
     *
     * @return the list of MidiFileWriters installed on the system
     */
    @SuppressWarnings("unchecked")
    private static List<MidiFileWriter> getMidiFileWriters() {
        List<MidiFileWriter> list = new ArrayList<>();
        list.add(new StandardMidiFileWriter());
        return list;
    }

    /**
     * Obtains the list of MidiFileReaders installed on the system.
     *
     * @return the list of MidiFileReaders installed on the system
     */
    @SuppressWarnings("unchecked")
    private static List<MidiFileReader> getMidiFileReaders() {
        List<MidiFileReader> list = new ArrayList<>();
        list.add(new StandardMidiFileReader());
        return list;
    }

    /**
     * Attempts to locate and return a default MidiDevice of the specified type.
     * This method wraps {@link #getDefaultDevice}. It catches the
     * {@code IllegalArgumentException} thrown by {@code getDefaultDevice} and
     * instead throws a {@code MidiUnavailableException}, with the catched
     * exception chained.
     *
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return default MidiDevice of the specified type
     * @throws MidiUnavailableException on failure
     */

    /**
     * Attempts to locate and return a default MidiDevice of the specified type.
     *
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return default MidiDevice of the specified type.
     * @throws IllegalArgumentException on failure
     */
    /**
     * Return a MidiDeviceProvider of a given class from the list of
     * MidiDeviceProviders.
     *
     * @param  providerClassName The class name of the provider to be returned
     * @param  providers The list of MidiDeviceProviders that is searched
     * @return A MidiDeviceProvider of the requested class, or null if none is
     *         found
     */
    private static MidiDeviceProvider getNamedProvider(String providerClassName,
                                                       List<MidiDeviceProvider> providers) {
        for(int i = 0; i < providers.size(); i++) {
            MidiDeviceProvider provider = providers.get(i);
            if (provider.getClass().getName().equals(providerClassName)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Return a MidiDevice with a given name from a given MidiDeviceProvider.
     *
     * @param  deviceName The name of the MidiDevice to be returned
     * @param  provider The MidiDeviceProvider to check for MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return A MidiDevice matching the requirements, or null if none is found
     */

    /**
     * Return a MidiDevice with a given name from a given MidiDeviceProvider.
     *
     * @param  deviceName The name of the MidiDevice to be returned
     * @param  provider The MidiDeviceProvider to check for MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @param  allowSynthesizer if true, Synthesizers are considered
     *         appropriate. Otherwise only pure MidiDevices are considered
     *         appropriate (unless allowSequencer is true). This flag only has
     *         an effect for deviceClass Receiver and Transmitter. For other
     *         device classes (Sequencer and Synthesizer), this flag has no
     *         effect.
     * @param  allowSequencer if true, Sequencers are considered appropriate.
     *         Otherwise only pure MidiDevices are considered appropriate
     *         (unless allowSynthesizer is true). This flag only has an effect
     *         for deviceClass Receiver and Transmitter. For other device
     *         classes (Sequencer and Synthesizer), this flag has no effect.
     * @return A MidiDevice matching the requirements, or null if none is found
     */
    /**
     * Return a MidiDevice with a given name from a list of MidiDeviceProviders.
     *
     * @param  deviceName The name of the MidiDevice to be returned
     * @param  providers The List of MidiDeviceProviders to check for
     *         MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return A Mixer matching the requirements, or null if none is found
     */

    /**
     * Return a MidiDevice with a given name from a list of MidiDeviceProviders.
     *
     * @param  deviceName The name of the MidiDevice to be returned
     * @param  providers The List of MidiDeviceProviders to check for
     *         MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @param  allowSynthesizer if true, Synthesizers are considered
     *         appropriate. Otherwise only pure MidiDevices are considered
     *         appropriate (unless allowSequencer is true). This flag only has
     *         an effect for deviceClass Receiver and Transmitter. For other
     *         device classes (Sequencer and Synthesizer), this flag has no
     *         effect.
     * @param  allowSequencer if true, Sequencers are considered appropriate.
     *         Otherwise only pure MidiDevices are considered appropriate
     *         (unless allowSynthesizer is true). This flag only has an effect
     *         for deviceClass Receiver and Transmitter. For other device
     *         classes (Sequencer and Synthesizer), this flag has no effect.
     * @return A Mixer matching the requirements, or null if none is found
     */
    /**
     * From a given MidiDeviceProvider, return the first appropriate device.
     *
     * @param  provider The MidiDeviceProvider to check for MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return A MidiDevice is considered appropriate, or null if no appropriate
     *         device is found
     */

    /**
     * From a given MidiDeviceProvider, return the first appropriate device.
     *
     * @param  provider The MidiDeviceProvider to check for MidiDevices
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @param  allowSynthesizer if true, Synthesizers are considered
     *         appropriate. Otherwise only pure MidiDevices are considered
     *         appropriate (unless allowSequencer is true). This flag only has
     *         an effect for deviceClass Receiver and Transmitter. For other
     *         device classes (Sequencer and Synthesizer), this flag has no
     *         effect.
     * @param  allowSequencer if true, Sequencers are considered appropriate.
     *         Otherwise only pure MidiDevices are considered appropriate
     *         (unless allowSynthesizer is true). This flag only has an effect
     *         for deviceClass Receiver and Transmitter. For other device
     *         classes (Sequencer and Synthesizer), this flag has no effect.
     * @return A MidiDevice is considered appropriate, or null if no appropriate
     *         device is found
     */
    /**
     * From a List of MidiDeviceProviders, return the first appropriate
     * MidiDevice.
     *
     * @param  providers The List of MidiDeviceProviders to search
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @return A MidiDevice that is considered appropriate, or null if none is
     *         found
     */

    /**
     * From a List of MidiDeviceProviders, return the first appropriate
     * MidiDevice.
     *
     * @param  providers The List of MidiDeviceProviders to search
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @param  allowSynthesizer if true, Synthesizers are considered
     *         appropriate. Otherwise only pure MidiDevices are considered
     *         appropriate (unless allowSequencer is true). This flag only has
     *         an effect for deviceClass Receiver and Transmitter. For other
     *         device classes (Sequencer and Synthesizer), this flag has no
     *         effect.
     * @param  allowSequencer if true, Sequencers are considered appropriate.
     *         Otherwise only pure MidiDevices are considered appropriate
     *         (unless allowSynthesizer is true). This flag only has an effect
     *         for deviceClass Receiver and Transmitter. For other device
     *         classes (Sequencer and Synthesizer), this flag has no effect.
     * @return A MidiDevice that is considered appropriate, or null if none is
     *         found
     */

    /**
     * Checks if a MidiDevice is appropriate. If deviceClass is Synthesizer or
     * Sequencer, a device implementing the respective interface is considered
     * appropriate. If deviceClass is Receiver or Transmitter, a device is
     * considered appropriate if it implements neither Synthesizer nor
     * Transmitter, and if it can provide at least one Receiver or Transmitter,
     * respectively.
     *
     * @param  device the MidiDevice to test
     * @param  deviceClass The requested device type, one of Synthesizer.class,
     *         Sequencer.class, Receiver.class or Transmitter.class
     * @param  allowSynthesizer if true, Synthesizers are considered
     *         appropriate. Otherwise only pure MidiDevices are considered
     *         appropriate (unless allowSequencer is true). This flag only has
     *         an effect for deviceClass Receiver and Transmitter. For other
     *         device classes (Sequencer and Synthesizer), this flag has no
     *         effect.
     * @param  allowSequencer if true, Sequencers are considered appropriate.
     *         Otherwise only pure MidiDevices are considered appropriate
     *         (unless allowSynthesizer is true). This flag only has an effect
     *         for deviceClass Receiver and Transmitter. For other device
     *         classes (Sequencer and Synthesizer), this flag has no effect.
     * @return true if the device is considered appropriate according to the
     *         rules given above, false otherwise
     */

    /**
     * Obtains the set of services currently installed on the system using the
     * SPI mechanism in 1.3.
     *
     * @param  providerClass The type of providers requested. This should be one
     *         of AudioFileReader.class, AudioFileWriter.class,
     *         FormatConversionProvider.class, MixerProvider.class,
     *         MidiDeviceProvider.class, MidiFileReader.class,
     *         MidiFileWriter.class or SoundbankReader.class.
     * @return a List of instances of providers for the requested service. If no
     *         providers are available, a List of length 0 will be returned.
     */

}
