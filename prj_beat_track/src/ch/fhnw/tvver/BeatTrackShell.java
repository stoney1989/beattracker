package ch.fhnw.tvver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.EnumSet;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.fhnw.tvver.AbstractBeatTracker.Flags;

public final class BeatTrackShell {
	private final static double         MAX_LATENCY = 0.1;
	private final static double         NS2SEC      = 1.0 / 1000000000.0;
	private double                      time;
	private long                        before;
	private final double[]              detectedBeats = new double[64 * 1024];
	private int                         numDetectedBeats;
	private int                         numTrueDetectedBeats;
	private int                         numFalseDetectedBeats;
	private final double[]              refBeats      = new double[detectedBeats.length];
	private int                         refBeatsSize;
	private int                         numRefBeats;
	private AudioFormat                 decodedFormat; 
	private AudioInputStream            in;
	private byte[]                      readBufffer = new byte[1];
	private double                      minLat = MAX_LATENCY;
	private double                      maxLat;
	private double                      sumLat;
	private final EnumSet<Flags>        flags;
	private byte[]                      pcmOut = new byte[64 * 1024];
	private int                         pcmSize;
	private int                         samplePos;

	BeatTrackShell(File track, EnumSet<Flags> flags) throws UnsupportedAudioFileException, IOException {
		this.flags = flags;
		in         = AudioSystem.getAudioInputStream(track);
		AudioFormat fileFormat = in.getFormat();
		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
				fileFormat.getSampleRate(),
				16,
				fileFormat.getChannels(),
				fileFormat.getChannels() * 2,
				fileFormat.getSampleRate(),
				false);
		in = AudioSystem.getAudioInputStream(decodedFormat, in);

		BufferedReader ref = new BufferedReader(new FileReader(new File(track.getParent(), nameWithoutExtension(track.getName()) + "txt")));
		for(;;) {
			String line = ref.readLine();
			if(line == null) break;
			refBeats[numRefBeats++] = Double.parseDouble(line);
		}
		refBeats[numRefBeats++] = Double.MAX_VALUE;
		refBeatsSize = numRefBeats;
		ref.close();
	}

	boolean getSamples(float[] samples) throws IOException {
		final int   numChannels = decodedFormat.getChannels();
		final float gain        = 1f / (32767 * numChannels);

		if(!fillReadBuffer(samples)) {
			in.close();
			return false;
		}
		
		int   readIndex = 0;
		for(int i = 0; i < samples.length; i++) {
			float sample = 0;
			for(int c = 0; c < numChannels; c++) {
				short s = (short) (readBufffer[readIndex++] & 0xFF);
				s |= (readBufffer[readIndex++] & 0xFF) << 8;
				sample += s;
			}
			samples[i] = sample * gain;

			setOut(samplePos++, samples[i], 0);
		}
		before = System.nanoTime();
		time  += samples.length / decodedFormat.getFrameRate();
				
		return true;
	}

	private boolean fillReadBuffer(final float[] samples) throws IOException {
		final int   frameSize   = 2 * decodedFormat.getChannels();
		final int   blockSize   = samples.length * frameSize;
		if(readBufffer.length < blockSize) readBufffer = new byte[blockSize];
		
		int count = blockSize;
		int pos   = 0;
		for(;;) {
			int read = in.read(readBufffer, pos, count);
			if(read < 0) return false;
			count -= read;
			if(count == 0) break;
			pos += read;
		}
		
		return true;
	}

	void beat() {
		long   delta    = System.nanoTime() - before;
		double beatTime = time + delta * NS2SEC;
		setOut((int) (beatTime * getSampleRate()), 1f, 1);
		if(refBeatsSize == 0) {
			numFalseDetectedBeats++;
		} else {
			int idx = Arrays.binarySearch(refBeats, 0, refBeatsSize, beatTime);
			if(idx < 0) {
				idx = -idx - 1;
				if(idx == 0) idx = 1;
				double before = beatTime - refBeats[idx-1];
				double after  = refBeats[idx] - beatTime;

				if(before > 0 && before < after) {
					if(before < MAX_LATENCY) {
						minLat = Math.min(minLat, before);
						maxLat = Math.max(maxLat, before);
						sumLat += before;
						removeRefBeat(idx-1);
						numTrueDetectedBeats++;
					} else
						numFalseDetectedBeats++;
				} else {
					if(after > 0 && after < MAX_LATENCY) {
						minLat = Math.min(minLat, after);
						maxLat = Math.max(maxLat, after);
						sumLat += after;
						removeRefBeat(idx);
						numTrueDetectedBeats++;
					} else
						numFalseDetectedBeats++;
				}
			}
		}
		detectedBeats[numDetectedBeats++] = beatTime;
	}

	private void setOut(final int position, final float value, final int channel) {
		final int position2 = position * 2;
		if(position2 >= pcmOut.length)
			pcmOut = Arrays.copyOf(pcmOut, position2 + pcmOut.length);
		pcmOut[position2 + channel] = (byte) (value * 127);
		pcmSize = Math.max(pcmSize, position2);
	}

	private void removeRefBeat(int idx) {
		refBeatsSize--;
		System.arraycopy(refBeats, idx + 1, refBeats, idx, refBeatsSize - idx);
	}

	double getSampleRate() {
		return decodedFormat.getSampleRate();
	}

	double[] getRefBeats() {
		return Arrays.copyOf(refBeats, refBeatsSize);
	}	

	private static final String SEP = "\t";
	String getReport() {
		if(maxLat == 0 && numDetectedBeats == 0) maxLat = MAX_LATENCY;
		
		String result = "";

		double avgLat = numDetectedBeats == 0 ? 0 : sumLat / numDetectedBeats;
//		result += time + SEP;
		result += numRefBeats + SEP;
//		result += + (60 * numRefBeats) / time + SEP;
		result += + numTrueDetectedBeats + SEP;
		result += + numFalseDetectedBeats + SEP;
//		result += + (int)(minLat * 1000) + SEP;
//		result += (int)(maxLat * 1000) + SEP;
//		result += (int)(avgLat * 1000) + SEP;

		double latency       = maxLat;
		double detectedBeats = Math.max(numTrueDetectedBeats - numFalseDetectedBeats, 0); 
		double q             = detectedBeats / numRefBeats;
		double l             = latency < MAX_LATENCY ? latency / MAX_LATENCY : 1;

//		result += q + SEP;
//		result += l + SEP;
		result += (1 + (5 * Math.min(q / l, 1))) + SEP;

		return result;
	}

	private static final String COLUMNS = 
			"File" + SEP + 
			"Track length" + SEP +
			"# of reference beats" + SEP +
			"BPM" + SEP +
			"# of true detected beats" + SEP +
			"# of false detected beats" + SEP +
			"Min. Latency" + SEP +
			"Max. Latency" + SEP +
			"Avg. Latency" + SEP +
			"q" + SEP +
			"l" + SEP +
			"Grade";

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
		File        src    = new File(args[0]);
		PrintWriter report = null;
		if(src.isDirectory())
			report = new PrintWriter(new File(src, args[1] + "_report.txt"));
		else if(src.isFile())
			report = new PrintWriter(new File(src.getParent(), args[1] + "_report.txt"));

		report.println(COLUMNS);

		Class<AbstractBeatTracker> cls = (Class<AbstractBeatTracker>)Class.forName("ch.fhnw.tvver." + args[1]);
		if(src.isDirectory())
			for(File file : new File(args[0]).listFiles())
				run(cls, report, file);
		else if(src.isFile())
			run(cls, report, src);

		report.close();
	}

	private static void run(Class<AbstractBeatTracker> cls, PrintWriter report, File file) {
		if(file.isFile() && !file.getName().endsWith(".txt") && !file.getName().startsWith(".")) {
			String row = file.getName() + SEP;
			System.out.println("----------" + file.getName());
			try {
				AbstractBeatTracker bt  = cls.getConstructor(File.class).newInstance(file);
				bt.run();
				row += bt.getReport();
				if(bt.getFlag(Flags.REPORT))
					report.println(row);
				if(bt.getFlag(Flags.WAVE))
					bt.writeWAV(new File(file.getParent(), nameWithoutExtension(file.getName()) + "wav"));
			} catch(Throwable t) {
				if(t.getCause() != null) t = t.getCause();
				row += t.getClass().getName() + ":" + t.getMessage() + SEP;
				report.println(row);
			}
			//System.out.println(COLUMNS);
			System.out.println(row);
			report.flush();
		}
	}

	private static String nameWithoutExtension(String name) {
		return name.substring(0, name.lastIndexOf('.') + 1);
	}

	boolean getFlag(Flags flag) {
		return flags.contains(flag);
	}

	void writeWAV(File file) throws IOException {
		AudioSystem.write(
				new AudioInputStream(new ByteArrayInputStream(pcmOut, 0, pcmSize), 
						new AudioFormat((float)getSampleRate(), 8, 2, true, false), 
						pcmSize / 2), 
						Type.WAVE, 
						file);
	}
}
