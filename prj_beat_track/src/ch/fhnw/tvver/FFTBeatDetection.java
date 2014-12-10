package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jtransforms.fft.FloatFFT_1D;

public class FFTBeatDetection extends AbstractBeatTracker {

	
	final static int SUBBAND_SIZE = 32;
	final static int HISTORY_SIZE = 43;
	final static int BUFFER_SIZE = 1024;
	
	final static int C = 250;
	
	// 1024 / 32 = 127
	final static int NUMBER_OF_SUBBANDS = BUFFER_SIZE / SUBBAND_SIZE;
	
	// usually 32 / 1024 = 0.03125
	private float magicNumber;
	
	
	private float[][] Ei;
	private int[] EiIndexes;
	private float[] buffer; 
	
	
	public FFTBeatDetection(File track)throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT));		
		
		//setup subbands amplitudes (Es) + subband history (Ei) + sample buffer for later use...		
		Ei = new float[ NUMBER_OF_SUBBANDS ][ HISTORY_SIZE ];
		EiIndexes = new int[ NUMBER_OF_SUBBANDS ];		
		buffer = new float[ BUFFER_SIZE ];
		
		magicNumber = NUMBER_OF_SUBBANDS / BUFFER_SIZE;
		
	}
	
	
	
	
	
	@Override
	public void run() {
		
		//see:
		// http://archive.gamedev.net/archive/reference/programming/features/beatdetection/index.html
		// http://gamedev.stackexchange.com/questions/9761/beat-detection-and-fft
							
		
		try {
			while( getSamples(buffer) ){			
				
				FloatFFT_1D fft_do = new FloatFFT_1D(buffer.length);
				float[] fft = new float[buffer.length * 2];
				System.arraycopy(buffer, 0, fft, 0, buffer.length);
				fft_do.complexForward( fft );				
		
				//calculate...
				// frequency amplitudes (B)
				// amplitudes of subbands (Es) 
				// subband history buffer (Ei)
				// average history amplitude <Ei>
				// aschis mom
				
				//float[] B = new float[ buffer.length ];
				float[] Es = new float[ NUMBER_OF_SUBBANDS  ];
				boolean isBeat = false;
				
				for (int i = 0; i < fft.length / 2; i++) {
					int index = i*2;
					float re = fft[ index    ];
					float im = fft[ index + 1];
					
					//calculate amplitude
					// X[k] = a + j�b
					// E[k] = |X[k]|^2 = (a+j�b)�(a-j�b) = a�a + b�b
					//B[i] = re*re + im*im;		
					
					//calculate amplitude of subband ------------------------
					int subbandIndex = ( i / SUBBAND_SIZE );
					//System.out.println(subbandIndex);
					
					//Es[ subbandIndex ] += B[i];
					Es[ subbandIndex ] += re*re + im*im;
//					System.out.println( buffer[i] );
//					System.out.println( re +" + i * " + im );

//					System.out.println( re*re + im*im );
					
					
					//System.out.println( (i+1) % SUBBAND_SIZE );
					// switch to next subband					
					if( (i+1) % SUBBAND_SIZE == 0){
						
						//set average amplitude of this subband
						
						//TODO: infinity bug...
						if(Es[ subbandIndex ] != 0 ) Es[ subbandIndex ] /= magicNumber;
						//if(Float.isInfinite(Es[ subbandIndex ]))Es[ subbandIndex ] = 0;
						
						float averageEI = this.calculateHistoryAverage( subbandIndex ); 
						
						//has subband a beat?
						if( Es[ subbandIndex ] > C * averageEI ){
							isBeat = true;
							System.out.println(Es[ subbandIndex ] +">"+ (C * averageEI));
						}
						
						//System.out.println(Es[ subbandIndex ] +">"+ (C * averageEI));
						
						
						//update history
						addAmplitudeToHistory( subbandIndex, Es[ subbandIndex ] );
						
					} 
				}
				if(isBeat)beat();
				//break;
				
				
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private float calculateHistoryAverage( int subband ){
		float averageEI = 0f;
		for (int i = 0; i < HISTORY_SIZE; i++) {
			averageEI += Ei[ subband ][ i ]; 
		}
		return ( averageEI > 0 )? averageEI/HISTORY_SIZE : 0;
	}
	
	private void addAmplitudeToHistory( int subband, float amplitude ){
		int i = EiIndexes[ subband ];
		Ei[ subband ][ i ] = amplitude;
		
		//update index
		EiIndexes[ subband ]++;
		if( EiIndexes[ subband ] >= NUMBER_OF_SUBBANDS ) EiIndexes[ subband ] = 0;
	}
	
	

}