package tools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

public class Vis extends JFrame {

	private JPanel contentPane;
	private JPanel panel;
	private JScrollPane scrollPane;
	private JPanel panel_1;
	private JLabel lblNewLabel;
	private JCheckBox chckbxNewCheckBox;
	private JCheckBox chckbxNewCheckBox_1;
	private JCheckBox chckbxFftIm;
	private JCheckBox chckbxAmplitueSpectrum;
	private JCheckBox chckbxPhaseSpectrum;
	private JCheckBox chckbxBeatFrom;
	private JCheckBox chckbxBeatfromAlgorithm;
	private JCheckBox chckbxCustom;
	private JCheckBox chckbxCustom_1;
	

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					Vis frame = new Vis();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}


	ArrayList<Float> samples = new ArrayList<>();
	public void addSample( float[] sample ){		
		for (int i = 0; i < sample.length; i++) {
			samples.add( sample[i] );
		}		
		updatePaneWidth(samples.size());
	}
	
	BitSet textBeat = new BitSet();
	public void addTextBeat( BitSet beats ){		
		textBeat = (BitSet) beats.clone();	
		updatePaneWidth(textBeat.size());
	}
	
	
	ArrayList<Float> fft_re = new ArrayList<>();
	ArrayList<Float> fft_im = new ArrayList<>();
	
	public void addFFT( float[] fft ){		
		for (int i = 0; i < fft.length / 2; i++) {
			int index = i*2;
			float re = fft[ index    ];
			float im = fft[ index + 1];
			fft_re.add( re );
			fft_im.add( im );
		}
		updatePaneWidth( fft_re.size() );		
	}
	
	int frequency = 44100;
	public void setFrequency(int frequency){
		this.frequency = frequency;
	}
	
	private void updatePaneWidth( int length ){
		if( length >= panel.getWidth() ){
			panel.setPreferredSize(new Dimension(length, panel.getHeight()));
			panel.revalidate();
			//panel.setBounds(panel.getX(), panel.getY(), panel.getWidth()*2, panel.getHeight());
			//panel.repaint();
		}
	}
	
	
	
	
	/**
	 * Create the frame.
	 */
	public Vis() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 903, 311);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(300);
		
		int mul = 200;
		panel = new JPanel(){
			 /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			 public void paint(Graphics g){
			       super.paint(g); 
			       Graphics2D g2D = (Graphics2D) g;
			       //System.out.println(scrollPane.getHorizontalScrollBar().getMaximum());
			      
			       
			       int from = scrollPane.getHorizontalScrollBar().getValue();
			       int to = from + scrollPane.getWidth();
			       GeneralPath gp1;	
			       
			       
			      
			      
			       //fft amplitude
			       if(chckbxAmplitueSpectrum.isSelected()){
			    	   g.setColor(Color.magenta);
				       gp1 = new GeneralPath();
				       
				       for (int i = from; i < to && i < fft_re.size() && i < fft_im.size(); i++) {
				    	   float mag = (float) Math.sqrt((fft_re.get(i)*fft_re.get(i))+(fft_im.get(i)*fft_im.get(i)));
				    	   gp1.moveTo(i, mul);
				    	   gp1.lineTo(i, (int) (mag*mul)+mul);			    	  
				       }
				       gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
			       }
			       
			       //fft phase
			       if(chckbxPhaseSpectrum.isSelected()){
			    	   g.setColor(Color.orange);
				       gp1 = new GeneralPath();
				       
				       
				       for (int i = from; i < to && i < fft_re.size() && i < fft_im.size(); i++) {
				    	   float phase = (float) Math.atan((fft_im.get(i)/fft_re.get(i)));
				    	   gp1.moveTo(i, mul);
				    	   gp1.lineTo(i, (int) (phase*mul)+mul);			    	  
				       }
				       gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
			       }
			      
			       
			       //fft re
			       if(chckbxNewCheckBox_1.isSelected()){
			    	   g.setColor(Color.blue);
				       gp1 = new GeneralPath();
				       
				       for (int i = from; i < to && i < fft_re.size(); i++) {
				    	   gp1.moveTo(i, mul);
				    	   gp1.lineTo(i, (int) (fft_re.get(i)*mul)+mul);			    	  
				       }
				       gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
			       }
			       
			     //fft im
			       if(chckbxFftIm.isSelected()){
			    	   g.setColor(Color.green);
				       gp1 = new GeneralPath();
				       
				       for (int i = from; i < to && i < fft_im.size(); i++) {
				    	   gp1.moveTo(i, mul);
				    	   gp1.lineTo(i, (int) (fft_im.get(i)*mul)+mul);			    	  
				       }
				       gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
			       }
			      
			       //draw samples
			       if(chckbxNewCheckBox.isSelected()){
				       g.setColor(Color.RED);
				       gp1 = new GeneralPath();			       
			    	   gp1.moveTo(from, mul);
				       for (int i = from; i < to && i < samples.size(); i++) {			    	   
				    	   gp1.lineTo(i, (int) (samples.get(i)*mul)+mul);
				       }
				       gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
			       }
			       
			       //frequency border
				     
		    	   g.setColor(Color.white);
		    	   
		    	   Stroke oldStroke = g2D.getStroke();
		    	   g2D.setStroke(new BasicStroke(20));
		    	  		    	   
		    	   gp1 = new GeneralPath();			       
		    	   gp1.moveTo(from, mul);
		    	   
		    	   //System.out.println(textBeat.length());
		    	   for (int i = from; i < to; i++) {
		    		   //System.out.print(textBeat.get(i));
		    		   if( i % frequency == 0){
		    			   gp1.moveTo(i, mul);
		    			   gp1.lineTo(i, 0);
//		    			   gp1.lineTo(i-44100, 0);
//		    			   gp1.lineTo(i-44100, mul*2);
		    			   gp1.lineTo(i, mul*2);
		    			   //System.out.println(i);
		    		   }
			       }
		    	   gp1.moveTo(from, mul);
			       gp1.closePath();			       
			       g2D.draw(gp1);
			       g2D.setStroke(oldStroke);
		       
			       
			       //beats from text file
			       if(chckbxBeatFrom.isSelected()){
			    	   g.setColor(Color.yellow);
			    	   
			    	   oldStroke = g2D.getStroke();
			    	   g2D.setStroke(new BasicStroke(10));
			    	  
			    	   
			    	   
			    	   gp1 = new GeneralPath();			       
			    	   gp1.moveTo(from, mul);
			    	   
			    	   //System.out.println(textBeat.length());
			    	   for (int i = from; i < to; i++) {
			    		   //System.out.print(textBeat.get(i));
			    		   if((textBeat.get(i))){
			    			   gp1.moveTo(i, mul);
			    			   gp1.lineTo(i, 0);
//			    			   gp1.lineTo(i-44100, 0);
//			    			   gp1.lineTo(i-44100, mul*2);
			    			   gp1.lineTo(i, mul*2);
			    			   //System.out.println(i);
			    		   }
				       }
			    	   gp1.moveTo(from, mul);
				       gp1.closePath();			       
				       g2D.draw(gp1);
				       g2D.setStroke(oldStroke);
			       }
			       
			     
  
			     //frequencyborder
			       
			     //base line
			       g.setColor(Color.white);
			       gp1 = new GeneralPath();	
			       gp1.moveTo(from, mul);
			       gp1.lineTo(from,mul);
			       gp1.lineTo(to,mul);
			       gp1.moveTo(from, mul);
			       gp1.closePath();			       
			       g2D.draw(gp1);
			 }
		};
		panel.setBackground(Color.DARK_GRAY);
		scrollPane.setViewportView(panel);
		
		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblNewLabel = new JLabel("Show...");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		chckbxCustom = new JCheckBox("Custom 1");
		GridBagConstraints gbc_chckbxCustom = new GridBagConstraints();
		gbc_chckbxCustom.anchor = GridBagConstraints.WEST;
		gbc_chckbxCustom.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxCustom.gridx = 0;
		gbc_chckbxCustom.gridy = 1;
		panel_1.add(chckbxCustom, gbc_chckbxCustom);
		
		chckbxCustom_1 = new JCheckBox("Custom 2");
		GridBagConstraints gbc_chckbxCustom_1 = new GridBagConstraints();
		gbc_chckbxCustom_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxCustom_1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxCustom_1.gridx = 1;
		gbc_chckbxCustom_1.gridy = 1;
		panel_1.add(chckbxCustom_1, gbc_chckbxCustom_1);
		
		chckbxNewCheckBox = new JCheckBox("Samples");
		chckbxNewCheckBox.setSelected(true);
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 2;
		panel_1.add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
		
		chckbxNewCheckBox_1 = new JCheckBox("FFT Re");
		GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
		gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox_1.gridx = 1;
		gbc_chckbxNewCheckBox_1.gridy = 2;
		panel_1.add(chckbxNewCheckBox_1, gbc_chckbxNewCheckBox_1);
		
		chckbxFftIm = new JCheckBox("FFT Im");
		GridBagConstraints gbc_chckbxFftIm = new GridBagConstraints();
		gbc_chckbxFftIm.anchor = GridBagConstraints.WEST;
		gbc_chckbxFftIm.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxFftIm.gridx = 2;
		gbc_chckbxFftIm.gridy = 2;
		panel_1.add(chckbxFftIm, gbc_chckbxFftIm);
		
		chckbxAmplitueSpectrum = new JCheckBox("Amplitude Spectrum");
		GridBagConstraints gbc_chckbxAmplitueSpectrum = new GridBagConstraints();
		gbc_chckbxAmplitueSpectrum.anchor = GridBagConstraints.WEST;
		gbc_chckbxAmplitueSpectrum.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxAmplitueSpectrum.gridx = 3;
		gbc_chckbxAmplitueSpectrum.gridy = 2;
		panel_1.add(chckbxAmplitueSpectrum, gbc_chckbxAmplitueSpectrum);
		
		chckbxPhaseSpectrum = new JCheckBox("Phase Spectrum");
		GridBagConstraints gbc_chckbxPhaseSpectrum = new GridBagConstraints();
		gbc_chckbxPhaseSpectrum.anchor = GridBagConstraints.WEST;
		gbc_chckbxPhaseSpectrum.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxPhaseSpectrum.gridx = 4;
		gbc_chckbxPhaseSpectrum.gridy = 2;
		panel_1.add(chckbxPhaseSpectrum, gbc_chckbxPhaseSpectrum);
		
		chckbxBeatFrom = new JCheckBox("Beat ( From Textfile )");
		GridBagConstraints gbc_chckbxBeatFrom = new GridBagConstraints();
		gbc_chckbxBeatFrom.anchor = GridBagConstraints.WEST;
		gbc_chckbxBeatFrom.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxBeatFrom.gridx = 5;
		gbc_chckbxBeatFrom.gridy = 2;
		panel_1.add(chckbxBeatFrom, gbc_chckbxBeatFrom);
		
		chckbxBeatfromAlgorithm = new JCheckBox("Beat (From Algorithm)");
		chckbxBeatfromAlgorithm.setEnabled(false);
		GridBagConstraints gbc_chckbxBeatfromAlgorithm = new GridBagConstraints();
		gbc_chckbxBeatfromAlgorithm.anchor = GridBagConstraints.WEST;
		gbc_chckbxBeatfromAlgorithm.gridx = 6;
		gbc_chckbxBeatfromAlgorithm.gridy = 2;
		panel_1.add(chckbxBeatfromAlgorithm, gbc_chckbxBeatfromAlgorithm);
	}

}
