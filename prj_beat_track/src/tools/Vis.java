package tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

public class Vis extends JFrame {

	private JPanel contentPane;
	JPanel panel;
	JScrollPane scrollPane;
	

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
		//samples.addAll( Arrays.asList(sample) );
		for (int i = 0; i < sample.length; i++) {
			samples.add( sample[i] );
		}
//		System.out.println(samples.size());
		if( samples.size() >= panel.getWidth() ){
			panel.setPreferredSize(new Dimension(samples.size(), panel.getHeight()));
			panel.revalidate();
			//panel.setBounds(panel.getX(), panel.getY(), panel.getWidth()*2, panel.getHeight());
			//panel.repaint();
		}
	}
	
	
	ArrayList<Float> fft_re = new ArrayList<>();
	ArrayList<Float> fft_im = new ArrayList<>();
	public void addFFTRE( float[] fft ){
		//samples.addAll( Arrays.asList(sample) );
		for (int i = 0; i < fft.length; i++) {
			fft_re.add( fft[i] );			
		}
//		System.out.println(samples.size());
		if( fft_re.size() >= panel.getWidth() ){
			panel.setPreferredSize(new Dimension(fft_re.size(), panel.getHeight()));
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
		
		
		int mul = 200;
		panel = new JPanel(){
			 @Override
			 public void paint(Graphics g){
			       super.paint(g); 
			       Graphics2D g2D = (Graphics2D) g;
			       System.out.println(scrollPane.getHorizontalScrollBar().getMaximum());
			      
			       
			       int from = scrollPane.getHorizontalScrollBar().getValue();
			       int to = from + scrollPane.getWidth();
			       
			       g.setColor(Color.white);
			       GeneralPath gp1 = new GeneralPath();	
			       gp1.moveTo(from, mul);
			       gp1.lineTo(from,mul);
			       gp1.lineTo(to,mul);
			       gp1.moveTo(from, mul);
			       gp1.closePath();			       
			       g2D.draw(gp1);
			       
			       g.setColor(Color.blue);
			       gp1 = new GeneralPath();
			       
			       for (int i = from; i < to; i++) {
			    	   gp1.moveTo(i, mul);
			    	   gp1.lineTo(i, (int) (fft_re.get(i)*mul)+mul);
			    	   //g.fillRect(i, (int) (fft_re.get(i)*mul)+mul, 2, 1);
			       }
			       gp1.moveTo(from, mul);
			       gp1.closePath();			       
			       g2D.draw(gp1);
			       
			       
			       g.setColor(Color.RED);
			       gp1 = new GeneralPath();			       
		    	   gp1.moveTo(from, mul);
			       for (int i = from; i < to; i++) {
			    	   
			    	   gp1.lineTo(i, (int) (samples.get(i)*mul)+mul);
//			    	   g.fillRect(i, (int) (samples.get(i)*mul)+mul, 1, 1);
//			    	   g.setColor(Color.white);
//			    	   g.fillRect(i, mul, 1, 1);
			           //Line2D lin = new Line2D.Float(i, samples.get(i)*200, i+1, samples.get(i+1)*200);
			           //g2.draw(lin);
			       }
			       gp1.moveTo(from, mul);
			       gp1.closePath();			       
			       g2D.draw(gp1);
			 }
		};
		panel.setBackground(Color.DARK_GRAY);
		scrollPane.setViewportView(panel);
	}

}
