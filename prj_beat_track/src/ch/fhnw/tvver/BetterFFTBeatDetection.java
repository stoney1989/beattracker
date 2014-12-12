package ch.fhnw.tvver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jtransforms.fft.FloatFFT_1D;

import tools.Vis;
import ddf.minim.analysis.FFT;

public class BetterFFTBeatDetection extends AbstractBeatTracker {

	public BetterFFTBeatDetection(File track)
			throws UnsupportedAudioFileException, IOException {
		super(track, EnumSet.of(Flags.REPORT));

	}

	@Override
	public void run() {

		FFT fft = new FFT(1024, 44100);
		
		fft.window(FFT.HAMMING); //against spectral leakage
		
		float[] sample = new float[1024];		

		try {
			Vis v = new Vis();
			v.setVisible(true);
			while (getSamples( sample )) {
				v.addSample(sample);
				fft.forward(sample);
				v.addFFTRE( fft.getSpectrumReal() );
				//v.repaint();
//				int mul = 100;
//				for (int i = 0; i < samples.length; i++) {
//					int value = (int) (samples[i]*mul);
//					for (int j = -mul; j <= mul; j++) {
//						if(value == j){
//							System.out.print('#');
//						}else if( j == 0){
//							System.out.print('|');
//						}else{
//							System.out.print(' ');
//						}
//						
//					}
//					System.out.println();
//					
//				}
				
				
				//fft.forward( samples );
			
				//System.out.println("\n\n");
				// buffer = new float[ BUFFER_SIZE ];
				// float[] buffer2 = new float[ BUFFER_SIZE ];
				//
				// FloatFFT_1D fft_do = new FloatFFT_1D(BUFFER_SIZE);
				// float[] fft = new float[BUFFER_SIZE * 2];
				// System.arraycopy(buffer, 0, fft, 0, BUFFER_SIZE);
				// fft_do.complexForward( fft );
				// fft_do.realForward(buffer);
				//
				// fft.forward( buffer );
				//
				// System.arraycopy( spectrum, 0, lastspectrum, 0,
				// spectrum.length );
				// System.arraycopy( fft.getSpectrumReal(), 0, spectrum, 0,
				// spectrum.length );
				//
				// float flux = 0;
				// for( int i = 0; i < spectrum.length; i++ ){
				// //flux += ( spectrum[i] - lastspectrum[i] );
				// }
				//
				// //spectralFlux.add( flux );
				//
				// System.out.println("-------------------------------------");
				//
				// // for (int i = 0; i < BUFFER_SIZE; i++) {
				// // //System.out.println(buffer[i]);
				// //// int index = i*2;
				// //// float re = fft[ index ];
				// //// float im = fft[ index + 1];
				// ////
				// //// spectrum[i] = (float) Math.sqrt( Math.pow( re, 2 ) +
				// Math.pow( im, 2 ) );
				// //
				// //
				// // System.out.println(spectrum[i]);
				// // }
				// //
				// // if(x>43)break;
				// // x++;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
