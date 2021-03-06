package projections.Tools.TimelineRendered;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import projections.Tools.Timeline.Data;
import projections.Tools.Timeline.MainHandler;
import projections.analysis.TimedProgressThreadExecutor;
import projections.gui.JPanelToImage;
import projections.gui.MainWindow;
import projections.gui.ProjectionsWindow;
import projections.gui.RangeDialog;
import projections.gui.Util;

public class TimelineRenderedWindow extends ProjectionsWindow implements MainHandler {

	private Color backgroundColor;
	private Color foregroundColor;
	private int width;
	private int height;

	private JMenuItem mSave;

	private JPanel combinedTimelinesPanel;

	private DialogExtension toolSpecificPanel;

	public TimelineRenderedWindow(MainWindow parentWindow) {
		super(parentWindow);
		createMenus();
		showDialog();
	}


	private void createMenus(){
		JMenuBar mbar = new JMenuBar();
		// Historic way of constructing menus. Needed for parent's class handling of menus.
		mbar.add(Util.makeJMenu("File", new Object[]
		                                           {
				"Select Processors",
				null,
				"Close"
		                                           },
		                                           this));


		// Construct menu items specific to this tool
		JMenu saveMenu = new JMenu("Save To Image");
		mSave = new JMenuItem("Save as JPG or PNG");
		menuHandler mh = new menuHandler();
		mSave.addActionListener(mh);
		saveMenu.add(mSave);

		mbar.add(saveMenu);

		setJMenuBar(mbar);
	}


	private class menuHandler implements ActionListener  {

		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == mSave){
				JPanelToImage.saveToFileChooserSelection(combinedTimelinesPanel, "Save Timeline Image", "./TimelineScreenshot.png");
			}

		}

	}



	/** Display the dialog box and load data specfied by user */
	protected void showDialog() {

		if (dialog == null) {
			toolSpecificPanel = new DialogExtension();
			dialog = new RangeDialog(this, "Select Range", toolSpecificPanel, false);
		}

		dialog.displayDialog();
		if (!dialog.isCancelled()){

			SortedSet<Integer> processorList = dialog.getSelectedProcessors();
			long startTime = dialog.getStartTime();
			long endTime = dialog.getEndTime();
			backgroundColor = Color.white;
			foregroundColor = Color.black;
			width = Integer.parseInt(toolSpecificPanel.dialogWidth.getText());

			final Date timeStart  = new Date();

			// Create a list of worker threads
			final List<Runnable> readyReaders = new LinkedList<Runnable>();

			// Iterate over user-specified processors
			int pIdx=0;		
			for(Integer pe : processorList){
				readyReaders.add( new ThreadedFileReader(pe, startTime, endTime, backgroundColor, foregroundColor, width) );
				pIdx++;
			}

			// Determine a component to show the progress bar with
			Component guiRootForProgressBar = null;

			// Pass this list of threads to a class that manages/runs the threads nicely
			final TimedProgressThreadExecutor threadManager = new TimedProgressThreadExecutor("Rendering Timelines in Parallel", readyReaders, guiRootForProgressBar, true);


			final SwingWorker worker =  new SwingWorker() {
				public Object doInBackground() {
					threadManager.runAll();
					return null;
				}
				
				public void done() {
					
					combinedTimelinesPanel = new JPanel();
					combinedTimelinesPanel.setLayout(new BoxLayout(combinedTimelinesPanel, BoxLayout.PAGE_AXIS));

					// Merge resulting images together into a single JPanel
					for (Runnable r : readyReaders) {
						BufferedImage i = ((ThreadedFileReader)r).getImage();
						JLabel l = new JLabel(new ImageIcon(i));
						l.setToolTipText("PE " + ((ThreadedFileReader)r).PE);
						combinedTimelinesPanel.add(l);
						width = i.getWidth();
						height = i.getHeight();
					}

					int totalHeight = readyReaders.size() * height;
					combinedTimelinesPanel.setPreferredSize(new Dimension(width, totalHeight));


					// put the resulting JPanel into a scrolling pane
					JScrollPane scrollpane = new JScrollPane(combinedTimelinesPanel);
					scrollpane.setPreferredSize(new Dimension(width+JScrollBar.WIDTH,
							totalHeight + JScrollBar.HEIGHT));

					// Add the resulting scroll pane to this tools window, then display it
					setLayout(scrollpane);
					pack();
					setVisible(true);

					Date time4  = new Date();

					double totalTime = ((time4.getTime() - timeStart.getTime())/1000.0);
					System.out.println("Time to render " + threadManager.numInitialThreads +  
							" input PE Timelines (using " + threadManager.numConcurrentThreads + " concurrent threads): " + 
							totalTime + "sec");	

				}
			};
			worker.execute();   

		}
	}


	/** Required by interface MainHandler. This one does nothing */
	public void setData(Data data) {
		// Do nothing
	}		

	/** Required by interface MainHandler. This one does nothing */
	public void refreshDisplay(boolean doRevalidate){
		// Do nothing	
	}

	/** Required by interface MainHandler. This one does nothing */
	public void notifyProcessorListHasChanged() {
		// Do nothing
	}

	/** Required by interface MainHandler. This one does nothing */
	public void displayWarning(String message) {
		// Do nothing
	}
	

}