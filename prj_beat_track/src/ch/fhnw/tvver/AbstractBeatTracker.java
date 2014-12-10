package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

public abstract class AbstractBeatTracker implements Runnable {
	enum Flags {REPORT, WAVE}

	private final BeatTrackShell bts;
	private       Throwable      exception;

	/**
	 * Get sample rate in samples/seconds.
	 * 
	 * @return The sample rate. 
	 */
	protected double getSampleRate() {
		return bts.getSampleRate();
	}

	/**
	 * Fill the provided buffer with samples. Samples are mixed down mono values in the range [-1..1].
	 * 
	 * @param buffer The buffer to fill.
	 * @return True if more samples are available, false otherwise.
	 * @throws IOException Thrown if an exception occurred while reading the source file.
	 */
	protected boolean getSamples(float[] buffer) throws IOException {
		return bts.getSamples(buffer);
	}

	/**
	 * Signal a beat. The beat will be recorded at the time this method is called relative to the samples retrieved by <code>getSamples()</code>.
	 */
	protected void beat() {
		bts.beat();
	}

	/**
	 * Create a beat tracker instance.
	 * 
	 * @param track The file to read samples from.
	 * @param flags Control output such as writing the reports and a WAV file.
	 * @throws UnsupportedAudioFileException Thrown if an exception occurred while reading the source file.
	 * @throws IOException Thrown if an exception occurred while reading the source file or writing one of the output files.
	 */
	protected AbstractBeatTracker(File track, EnumSet<Flags> flags) throws UnsupportedAudioFileException, IOException {
		bts = new BeatTrackShell(track, flags);
	}

	//--- internal interface
	
	final double[] getRefBeats() {
		return bts.getRefBeats();
	}

	final void handleException(Throwable t) {
		this.exception = t;
	}

	final Throwable getException() {
		return exception;
	}

	final String getReport() {
		if(exception != null)
			return exception.getClass().getName() + ":" + exception.getMessage();
		else
			return bts.getReport();
	}

	final boolean getFlag(Flags flag) {
		return bts.getFlag(flag);
	}

	final void writeWAV(File file) throws IOException {
		bts.writeWAV(file);
	}	
}
