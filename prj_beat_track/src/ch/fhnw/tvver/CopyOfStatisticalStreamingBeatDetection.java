package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

public class CopyOfStatisticalStreamingBeatDetection extends AbstractBeatTracker {
	
	final  int    FREQ      = (int)getSampleRate();

// score: 2.5
//	final  int   TIME_SIZE  = 1024;
//	final  float TIME_LIMIT = 0.3f;	
//	final static int MAGIC_MULTIPLIER = 10;
	
//score: 2.8076863
//	final  int   TIME_SIZE  = 512;
//	final  float TIME_LIMIT = 0.4f;
//	final static float MAGIC_MULTIPLIER = 10.5f;
	
// score: 2.885
//	final  int   TIME_SIZE  = 1024;
//	final  float TIME_LIMIT = 0.4f;
//	final static int MAGIC_MULTIPLIER = 10;

// score: 2.9640708
//	final  int   TIME_SIZE  = 1024;
//	final  float TIME_LIMIT = 0.4f;
//	final static float MAGIC_MULTIPLIER = 10.5f;
	
// score: 3.0089693	
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.2f;
//	final static float MAGIC_MULTIPLIER = 8f;
	

	
	
//---------------------------------------
	//agoria-kofea.mp3
	//score: 2.726023
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.3f;
//	final static float MAGIC_MULTIPLIER = 4.99f;
	
	
	//alter_ego-lavender.mp3
	//score: 3.7584167
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.3f;
//	final static float MAGIC_MULTIPLIER = 22f;
	
	//astral_projection-neurocity.mp3
	//score: 2.4685934
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.2f;
//	final static float MAGIC_MULTIPLIER = 22f;
	
	//bomb_the_bass-megablast.mp3
	//score: 1.1595054
//	final  int   TIME_SIZE  = 256;
//	final  float TIME_LIMIT = 0.8f;
//	final static float MAGIC_MULTIPLIER = 5.0f;
	
	//fischer_spooner-emerge.mp3
	//score: 1.3000097
//	final  int   TIME_SIZE  = 512;
//	final  float TIME_LIMIT = 0.3f;
//	final static float MAGIC_MULTIPLIER = 22.0f;
	
	//Leftfield - Afro Ride.mp3
	//score: 2.1780486
//	final  int   TIME_SIZE  = 2048;
//	final  float TIME_LIMIT = 0.3f;
//	final static float MAGIC_MULTIPLIER = 7.1f;
	
	
	//Leftfield - Afro Ride.mp3
	//score: 2.1780486
	final  int   TIME_SIZE  = 2048;
	final  float TIME_LIMIT = 0.3f;
	final static float MAGIC_MULTIPLIER = 7.1f;

	
	private float[] energyHistory;
	private float[] differenceHistory;
	
	private float[] buffer;
	
	private int energyHistoryIndex;
	private int differenceHistoryIndex;
	
	private float time;
	private float dTime;
	
	private boolean isBeat = false;

	public CopyOfStatisticalStreamingBeatDetection(File track)throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT));
		 buffer            = new float[ TIME_SIZE ];
		 energyHistory     = new float[ FREQ / TIME_SIZE ];
		 differenceHistory = new float[ FREQ / TIME_SIZE ];
		 energyHistoryIndex     = 0;
		 differenceHistoryIndex = 0;
		 time = 0;
		 dTime  = (float) TIME_SIZE / FREQ; 
	}
	
	
	@Override
	public void run() {
		//System.out.println("asdfasdfasfd");
		
		
		try {
			while( getSamples(buffer) ){
				float e = calculateInstantSoundEnergy( buffer );
				float E = calculateLocalAverageEnergy();
				
				float V = calculateVariance(E);
				float C = (-0.0025714f * V) + 1.5142857f;
				
				float d = (float)Math.max(e - C * E, 0);				
				float D = calculateAverageDifference();
				
				float diffdiff = (float)Math.max(d - D, 0);
				
				time += dTime;
				
				//System.out.println(time);
				
								
				if( time >= TIME_LIMIT && diffdiff > 0 && e > 2 ){
					time = 0;
					beat();
					//isBeat = true;
				}
				
				
				addToHistory(e, energyHistory, energyHistoryIndex);
				addToHistory(d, differenceHistory, differenceHistoryIndex);
				//addBufferToHistory(buffer);
				
				//if(e > (c*E))System.out.println( "Sound Energy: "+e+" Energy Average: "+(c*E) );
				//if(e>(c*E))beat();
				
				
				//System.out.println(c);
				
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
	
//	private void addBufferToHistory( float[] buffer ){
//		System.out.println(historyIndex);
//		System.arraycopy(buffer, 0, energyHistory, historyIndex, 1024);
//		historyIndex+=1024;
//		if( historyIndex >= 44032 )historyIndex = 0;
//		//energyHistory[ historyIndex ] = e;
//	}
	
	private static float calculateInstantSoundEnergy( float[] buffer ){
		float e = 0;
		for (int i = 0; i < buffer.length; i++) {			
			e+=buffer[i]*buffer[i];
		}
		e/=buffer.length; 
		return (float) Math.sqrt(e) * MAGIC_MULTIPLIER;
	}
	
	private float calculateVariance(float E){
		float V = 0;
		for (int i = 0; i < energyHistory.length; i++) {
				V += (float)Math.pow( energyHistory[i]-E, 2);	
		}
		return  V / energyHistory.length;
	}

}
