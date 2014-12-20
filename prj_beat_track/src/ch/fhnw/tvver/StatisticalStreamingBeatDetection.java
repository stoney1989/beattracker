package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jtransforms.fft.FloatFFT_1D;

public class StatisticalStreamingBeatDetection extends AbstractBeatTracker {
	
	final  int    FREQ      = (int)getSampleRate();

	//parts of the algorithm are from http://archive.gamedev.net/archive/reference/programming/features/beatdetection/index.html
	
// average score: 4.270447951096755	
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.2f;
//	final float  D_TIME_MODIFIER = 0.5f;
//	final  float HISTORY_SIZE_MODIFIER = 1f;
//	final static float POWER_UP_MULTIPLIER = 8f;
//	final boolean FILTER_FREQ = false;
//	final float   MIN_FREQ = 0;
//	final float   MAX_FREQ = FREQ;
	
	// average score:	4.31801282442533
//		final  int   TIME_SIZE  = 1024;
//		final  float TIME_LIMIT = 0.2f;
//		final  float D_TIME_MODIFIER = 0.5f;
//		final  float HISTORY_SIZE_MODIFIER = 2f;
//		final static float POWER_UP_MULTIPLIER = 7f;
//	    final boolean FILTER_FREQ = false;
//	    final float   MIN_FREQ = 0;
//	    final float   MAX_FREQ = FREQ;

	// average score: 4.4248636685884914	
		final  int   TIME_SIZE  = 1536;
		final  float TIME_LIMIT = 0.2f;
		final float  D_TIME_MODIFIER = 0.5f;
		final  float HISTORY_SIZE_MODIFIER = 1f;
		final static float POWER_UP_MULTIPLIER = 8f;
	    final boolean FILTER_FREQ = false;
	    final float   MIN_FREQ = 0;
	    final float   MAX_FREQ = FREQ;
	
	// average score:	4.3545391376201925
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.2f;
//	final  float D_TIME_MODIFIER = 0.5f;
//	final  float HISTORY_SIZE_MODIFIER = 2f;
//	final static float POWER_UP_MULTIPLIER = 8f;	
//	final boolean FILTER_FREQ = true;
//	final float   MIN_FREQ = 20;
//	final float   MAX_FREQ = FREQ;

	

	private int FFT_SIZE = TIME_SIZE / 2;
	FloatFFT_1D mFFT = new FloatFFT_1D(FFT_SIZE);
	
	private float[] energyHistory;
	private float[] differenceHistory;	
	private float[] buffer;
	
	private int energyHistoryIndex;
	private int differenceHistoryIndex;
	
	private float time;
	private float dTime;
	
	private boolean isBeat = false;

	public StatisticalStreamingBeatDetection(File track)throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT));
		 buffer            = new float[ TIME_SIZE ];
		 energyHistory     = new float[ (int) ((FREQ / TIME_SIZE)*HISTORY_SIZE_MODIFIER) ];
		 differenceHistory = new float[ (int) ((FREQ / TIME_SIZE)*HISTORY_SIZE_MODIFIER) ];
		 energyHistoryIndex     = 0;
		 differenceHistoryIndex = 0;
		 time = 0;
		 dTime  = (float) TIME_SIZE / FREQ; 
	}
	
	
	@Override
	public void run() {
	
		
		
		
		try {
			while( getSamples(buffer) ){
				
				if(FILTER_FREQ)filterFrequencies(MIN_FREQ,MAX_FREQ);
				
				//calculate instant energy of current sample
				float e = calculateInstantSoundEnergy( buffer );
				
				//calculate average of energy history
				float E = calculateLocalAverageEnergy();
				
				//calculate variance of energy history
				float V = calculateVariance(E);
				
				
				// linear degression of constant 'C' with variance
				float C = 1.35f;//(-0.0025714f * V) + 1.5142857f;
				
				//difference from instant energy to 'historic' energy
				float d = (float)Math.max(e - C * E, 0);			
				
				//average 'historic' difference 
				float D = calculateAverageDifference();
				
				//difference of the difference between current difference and average difference
				float diffdiff = (float)Math.max(d - D, 0);
				
				//time between one beat to the next
				time += dTime*D_TIME_MODIFIER;			
				
				if( time >= TIME_LIMIT && diffdiff > 0 && e > 2 ){
					time = 0;
					beat();
				}				
				
				addToHistory(e, energyHistory, energyHistoryIndex);
				addToHistory(d, differenceHistory, differenceHistoryIndex);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private float calculateLocalAverageEnergy(){
		float E = 0;		
		for (int i = 0; i < energyHistory.length; i++) {		
			E += energyHistory[i];
		}
		return E / energyHistory.length;
	}
	
	private float calculateAverageDifference(){
		float D = 0;		
		int countNonZero = 0;
		for (int i = 0; i < differenceHistory.length; i++) {
			if( differenceHistory[i] > 0 ){
				D += differenceHistory[i];
				countNonZero++;
			}			
		}
						
		return (countNonZero>0)? D / countNonZero : 0;
	}
	
	private static void addToHistory( float e, float[] history, int index ){		
		history[ index ] = e;
		index++;
		if( index >= history.length )index = 0;
	}
	
	
	private static float calculateInstantSoundEnergy( float[] buffer ){
		float e = 0;
		for (int i = 0; i < buffer.length; i++) {			
			e+=buffer[i]*buffer[i];
		}
		e/=buffer.length; 
		return (float) Math.sqrt(e) * POWER_UP_MULTIPLIER;
	}
	
	private void filterFrequencies(float freqMin, float freqMax){
		mFFT.realForward(buffer);
		for(int fftBin = 0; fftBin < FFT_SIZE; fftBin++){
			float frequency = (float)fftBin * FREQ / (float)FFT_SIZE;
			if(frequency  < freqMin || frequency > freqMax){
				 int real = 2 * fftBin;
			     int imaginary = 2 * fftBin + 1;
			     
			     buffer[real]      = 0;
			     buffer[imaginary] = 0;
			}
		}
		
		
		mFFT.realInverse(buffer, true);	
	}
	
	private float calculateVariance(float E){
		float V = 0;
		for (int i = 0; i < energyHistory.length; i++) {
				V += (float)Math.pow( energyHistory[i]-E, 2);	
		}
		return  V / energyHistory.length;
	}

}
