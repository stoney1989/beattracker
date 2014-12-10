package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A fake beat tracker which jitters the reference data
 * and signals the jittered reference data as beats. 
 * It also writes the beats to the right channel of a WAV file together
 * with the audio stream on the left channel.
 * 
 * @author simon.schubiger@fhnw.ch
 *
 */
public class RandomizingReferenceTracker extends AbstractBeatTracker {
	private static final double JITTER = 0.1;		

	public RandomizingReferenceTracker(File track) throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT, Flags.WAVE));
	}

	@Override
	public void run() {
		try {
			float[]               buffer         = new float[1024];
			double                freq           = getSampleRate();
			BitSet                beats          = new BitSet();

			for(double beat : getRefBeats())
				beats.set((int) ((beat + Math.random() * JITTER) * freq));

			int sample = 0;

			// audio processing loop
			for(;;) {
				// fill our buffer with samples and exit if there is no more data
				if(!getSamples(buffer)) break;

				// process samples / find beats
				for(int i = 0; i < buffer.length; i++, sample++)
					if((beats.get(sample)))
						beat();
			}
		} catch(Throwable t) {
			handleException(t);
		}
	}
}
