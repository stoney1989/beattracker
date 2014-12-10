package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

import ch.fhnw.tvver.AbstractBeatTracker.Flags;

public class StatisticalStreamingBeatDetection extends AbstractBeatTracker {

	public StatisticalStreamingBeatDetection(File track)throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT, Flags.WAVE));
		
	}
	
	private static float[] energyHistory = new float[43];
	private static int historyIndex = 0;
	
	private static float c = 1.3f;
	
	@Override
	public void run() {
		//System.out.println("asdfasdfasfd");
		float[] buffer = new float[1024];
		
		try {
			while( getSamples(buffer) ){
				float e = calculateInstantSoundEnergy( buffer );
				float E = calculateLocalAverageEnergy();
				
				addEnergyToHistory(e);
				//addBufferToHistory(buffer);
				
				//if(e > (c*E))System.out.println( "Sound Energy: "+e+" Energy Average: "+(c*E) );
				if(e>(c*E))beat();
				
				c = ( -0.0025714f * calculateVariance(E) ) + 1.5142857f;
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
				//E += energyHistory[i]*energyHistory[i];
			E += energyHistory[i];
		}
		return E / energyHistory.length;
	}
	
	private void addEnergyToHistory( float e ){		
		energyHistory[ historyIndex ] = e;
		historyIndex++;
		if( historyIndex >= energyHistory.length )historyIndex = 0;
	}
	
	private void addBufferToHistory( float[] buffer ){
		System.out.println(historyIndex);
		System.arraycopy(buffer, 0, energyHistory, historyIndex, 1024);
		historyIndex+=1024;
		if( historyIndex >= 44032 )historyIndex = 0;
		//energyHistory[ historyIndex ] = e;
	}
	
	private static float calculateInstantSoundEnergy( float[] buffer ){
		float e = 0;
		for (int i = 0; i < buffer.length; i++) {
			//e+= 2*(buffer[i]*buffer[i]);
			e+=Math.abs(buffer[i]);
		}
		return e;
	}
	
	private static float calculateVariance(float localeAverage){
		float V = 0;
		for (int i = 0; i < energyHistory.length; i++) {
				V += energyHistory[i]-localeAverage;	
		}
		return V/energyHistory.length;
	}

}