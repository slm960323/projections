package projections.gui.Timeline;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.*;
import java.awt.FontMetrics;

/** Draws the left column of the timeline view. The labels such as "PE 0", "PE 1" */
public class LabelPanel extends JPanel 
{  
	private static final long serialVersionUID = 1L;

	/** The desired width of the panel */
	private int myWidth = 65; // This is what corresponds to the font we use now
	
	
	// Temporary hardcode. This variable will be assigned appropriate
	// meaning in future versions of Projections that support multiple
	// runs.
	int myRun = 0;

	private Data data;
	private MainHandler parentWindow; 
	
	public LabelPanel(Data data, MainHandler parentWindow)
	{
		setOpaque(true);	
		this.data = data;
		this.parentWindow = parentWindow;
	}
	
	/** Determine the preferred width for this panel, respecting the data.useMinimalMargins flag 
	 * 
	 * @note if data.useMinimalMargins is true then only the PE number will be printed without the idle %
	 * */
	private int preferredWidth(){
		if(data.useMinimalView())
			return 40;
		else
			return 85;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(preferredWidth(), data.screenHeight());
	}	


	protected void paintComponent(Graphics g)
	{

		g.setFont(data.labelFont);
		FontMetrics fm = g.getFontMetrics();

		g.setColor(data.getBackgroundColor());
		Rectangle clipBounds = g.getClipBounds();
		g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

		// Draw the labels onto the screen
		g.setColor(data.getForegroundColor());
		data.processorList().reset();
		
		for (int p=0; p<data.numPs(); p++) {

			if(data.useMinimalView()){
				// A simpler version (right justified, bold larger PE label, no idle percentage)
				String peString = "PE "+data.processorList().nextElement();
				int stringWidth = fm.stringWidth(peString);			
				g.drawString(peString, preferredWidth()-stringWidth, fm.getHeight()/2+data.singleTimelineHeight()/2 + p*data.singleTimelineHeight());
				
			} else {
				// The full version
				String peString = "PE "+data.processorList().nextElement();
				g.drawString(peString, 10, data.singleTimelineHeight()/2 + p*data.singleTimelineHeight());
				
				String percentString = "(" + (int)(100 - data.idleUsage[p]) + "%," + (int)(data.processorUsage[p]) + "%)";
				g.drawString(percentString, 15, data.singleTimelineHeight()/2 + p*data.singleTimelineHeight() + fm.getHeight() + 2);
		
			}
			
		}


	}


	public int getDesiredWidth() {
		return myWidth;
	}   

}

