package projections.Tools.TimelineRendered;
import java.awt.Color;
import java.awt.image.BufferedImage;

import projections.Tools.Timeline.Data;
import projections.Tools.Timeline.MainHandler;
import projections.Tools.Timeline.MainPanel;
import projections.gui.JPanelToImage;
import projections.gui.OrderedIntList;

/** The reader threads for Time Profile tool. This class ought to be generalized for all the other tools needing similar functionality. */
class ThreadedFileReaderTimelineRendered extends Thread implements MainHandler {
	protected int PE;
	private Color background;
	private Color foreground;
	private long startTime, endTime;
	private BufferedImage image;
	private int width;

	protected ThreadedFileReaderTimelineRendered(int pe, long startTime, long endTime, Color backgroundColor, Color foregroundColor, int width){
		this.PE = pe;
		this.startTime = startTime;
		this.endTime = endTime;
		this.background = backgroundColor;
		this.foreground = foregroundColor;
		this.width = width;
	}

	public void run() { 

		OrderedIntList validPEs = new OrderedIntList();
		validPEs.insert(PE);

		// setup the Data for this panel 
		Data data = new Data(null);
		data.setProcessorList(validPEs);
		data.setRange(startTime, endTime);
//		data.setUseMinimalMargins(true);
//		data.setFontSizes(12, 10, true);
//		data.showIdle(false);
//		data.showPacks(true);
		data.setCompactView(true);
		
		if(background != null && foreground != null)
			data.setColors(background,foreground);

		data.setHandler(this);

		// create a MainPanel for it	
		MainPanel displayPanel = new MainPanel(data, this);
		displayPanel.loadTimelineObjects(false, null, false);

		displayPanel.setSize(width,data.singleTimelineHeight());
		displayPanel.revalidate();
		displayPanel.doLayout();

		image = JPanelToImage.generateImage(displayPanel);
		
		System.out.println("Created image for PE " + PE);
		
		data = null;
	}

	
	public void displayWarning(String message) {
		// do nothing
	}

	public void notifyProcessorListHasChanged() {
		// do nothing
	}

	public void refreshDisplay(boolean doRevalidate) {
		// do nothing
	}

	public void setData(Data data) {
		// do nothing
	}

	public BufferedImage getImage() {
		return image;
	}


}





