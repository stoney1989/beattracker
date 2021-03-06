package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

import at.ofai.music.audio.FFT;

//import ddf.minim.analysis.FFT;

public class FFTBeatDetection extends AbstractBeatTracker {

	final  int   SAMPLE_RATE = (int)getSampleRate();	
	final  int   SAMPLE_SIZE   = 1024;
	
	final float HOP_TIME  = 0.010f;
	final float FFT_TIME  = 0.04644f;
	
	public FFTBeatDetection(File track)throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT));		
		
		
		
	}
	
	
	
	
	
	@Override
	public void run() {
		try{
			
			int hopSize = (int) Math.round(SAMPLE_RATE * HOP_TIME);
			int fftSize = (int) Math.round( Math.pow(2, Math.round( Math.log( FFT_TIME * SAMPLE_RATE) / Math.log(2) ) ) );
			
			//makeFreqMap------------------------------
			int[] freqMap = new int[fftSize/2+1];
			double binWidth = SAMPLE_RATE / fftSize;
			int crossoverBin = (int)(2 / (Math.pow(2, 1/12.0) - 1));
			int crossoverMidi = (int)Math.round( Math.log( crossoverBin * binWidth / 440 ) / Math.log(2) * 12 + 69);
			int i = 0;
			while (i <= crossoverBin)freqMap[i++] = i;
			while (i <= fftSize/2) {
				double midi = Math.log(i*binWidth/440) / Math.log(2) * 12 + 69;
				if (midi > 127)	midi = 127;
				freqMap[i++] = crossoverBin + (int)Math.round(midi) - crossoverMidi;
			}
			int freqMapSize = freqMap[i-1] + 1;
			//------------------------------------------
			
			int buffSize = hopSize * 2;
			
			//init buffers
			float[] inputBuffer = new float[buffSize];
			float[] circBuffer  = new float[fftSize];
			float[] reBuffer    = new float[fftSize];
			float[] imBuffer    = new float[fftSize];
			float[] prevPhase   = new float[fftSize];
			float[] prevPrevPhase = new float[fftSize];
			float[] prevFrame   = new float[fftSize];
			
			//init window
			double[] window = FFT.makeWindow(FFT.HAMMING, fftSize, fftSize);
			for (int j=0; j < fftSize; j++)	window[j] *= Math.sqrt(fftSize);
			
			//init frames
			int totalFrames = (int) (3600 / HOP_TIME);			
			float[] newFrame = new float[freqMapSize];
			float[][] frames = new float[totalFrames][freqMapSize];
			
			
			int energyOversampleFactor = 2;
			float[] energy = new float[totalFrames*energyOversampleFactor];
			float[] phaseDeviation = new float[totalFrames];
			float[] spectralFlux   = new float[totalFrames];
			int frameCount = 0;
			int cbIndex = 0;
			int frameRMS = 0;
			int ltAverage = 0;
			
			
			float[] buffer =  new float[1];
			while( getSamples(buffer) ){
				
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

}
