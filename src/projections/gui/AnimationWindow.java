package projections.gui;

import java.awt.Button;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.SortedSet;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

class AnimationWindow extends ProjectionsWindow
   implements ActionListener, AdjustmentListener
{

	// Temporary hardcode. This variable will be assigned appropriate
    // meaning in future versions of Projections that support multiple
    // runs.
	private int myRun = 0;

    private AnimationColorBarPanel colorbarPanel;
    private AnimationDisplayPanel  displayPanel;
    private AnimationWindow thisWindow;
   
    
    private JMenuBar mbar = new JMenuBar();

    private IntervalChooserPanel intervalPanel;

	private Button bPlusOne, bMinusOne, bAuto;
    private Button setRanges;
    private TimeTextField delayField;
    private JPanel statusPanel;
    private JPanel titlePanel; 
    
    //private Label lTitle, lStatus;
    private JLabel lTitle, lStatus, lDelay;
    private Scrollbar slider;
    
    private int redrawDelay; //Real time between frames (ms)
    private boolean keepAnimating;
    private AnimateThread thread;

    private boolean layoutComplete = false;

    // basic parameter variables consistent with IntervalRangeDialog
    protected long intervalSize;
    protected SortedSet<Integer> selectedPEs;
    protected long startTime;
    protected long endTime;
    
    private class AnimateThread extends Thread
    {
	public AnimateThread()
	{
	    keepAnimating=true;
	}
   
	public void run()
	{
	    long finish = redrawDelay + System.currentTimeMillis();
	    while (keepAnimating) {
		long timeLeft = finish - System.currentTimeMillis();
		if (timeLeft > 0) {
		    try { //Give other threads a chance
			sleep(timeLeft);
		    } catch (InterruptedException E) {
		    	// ignore
		    }
		} else { //Advance to next frame
		    finish += redrawDelay;
		    changeCurI(displayPanel.getCurI() + 1);
		}   
	    }
	}  
    }       

   
  
    protected AnimationWindow(MainWindow mainWindow)
    {
	super(mainWindow);
	setBackground(MainWindow.runObject[myRun].background);
	setTitle("Projections Animation - " + MainWindow.runObject[myRun].getFilename() + ".sts");
          
	thisWindow = this;

	showDialog();
    }   

    public void showDialog() {
    	if (dialog == null) {
    		intervalPanel = new IntervalChooserPanel();  	
    		dialog = new RangeDialog(this, "Select Animation Range", intervalPanel, false);
    	} 

    	dialog.displayDialog();
    	if (!dialog.isCancelled()){
    		intervalSize = intervalPanel.getIntervalSize();
    		selectedPEs = dialog.getSelectedProcessors();
    		startTime = dialog.getStartTime();
    		endTime = dialog.getEndTime();
    		final SwingWorker worker =  new SwingWorker() {
    			public Object doInBackground() {
    				if (thisWindow.layoutComplete) {
    					displayPanel.setParameters();
    					slider.setValues(0, 1, 0, displayPanel.getNumI());
    				} else {
    					createMenus();
    					createLayout();
    				}
    				return null;
    			}
    			public void done() {
    				if (thisWindow.layoutComplete) {
    					pack();
    					thisWindow.setVisible(true);
    				} else {
    					thisWindow.repaint();
    				}
    			}
    		};
    		worker.execute();
    	}
    }

    public void actionPerformed(ActionEvent evt)
    {
	if (evt.getSource()==delayField) {
	    // System.out.println("event on delay field");
	    // Get redraw in milliseconds
	    redrawDelay=(int)(delayField.getValue()/1000); 
	}else if(evt.getSource() instanceof Button) {
	    Button b = (Button)evt.getSource();
	    if (b == bPlusOne) {
		changeCurI(displayPanel.getCurI() + 1);
	    } else if (b == bMinusOne) {
		changeCurI(displayPanel.getCurI() - 1);
	    } else if (b == bAuto) {
		if (b.getLabel().equals("Auto")) {
		    b.setLabel("Stop");
		    keepAnimating=true;
		    thread = new AnimateThread();
		    thread.start();
		} else { // label is "Stop"
		    b.setLabel("Auto");
		    if (thread != null && thread.isAlive()) {
			keepAnimating=false;
			thread = null;
			changeCurI(displayPanel.getCurI());
		    }   
		}
	    } else if (b == setRanges) {
		showDialog();
	    }
        } else if (evt.getSource() instanceof JMenuItem) {
            String arg = ((JMenuItem)evt.getSource()).getText();
            if (arg.equals("Close")) {
                close();
            } else if(arg.equals("Select Processors")) {
                showDialog();
            }
        }
    }   
    
    public void adjustmentValueChanged(AdjustmentEvent e){
    	if(slider.getValueIsAdjusting()){
	    changeCurI(slider.getValue());
	}
    }

    private void changeCurI(int i)
    {
	displayPanel.setCurI(i);
	setTitleInfo(displayPanel.getCurI()); 
	slider.setValue(i);
    }   

    private void createMenus(){
        mbar.add(Util.makeJMenu("File", new Object[]
            {
                "Select Processors",
                null,
                "Close"
            },
                                this));
        mbar.add(Util.makeJMenu("Tools", new Object[]
            {
                "Change Colors",
            },
                                this));
        mbar.add(Util.makeJMenu("Help", new Object[]
            {
                "Index",
                "About"
            },
                                this));
        setJMenuBar(mbar);
    }

    private void createLayout()
    {
	JPanel mainPanel     = new JPanel();
	titlePanel    = new JPanel();
	statusPanel   = new JPanel();
	JPanel controlPanel  = new JPanel();
	colorbarPanel = new AnimationColorBarPanel();
	displayPanel  = new AnimationDisplayPanel(this);
          
	bPlusOne  = new Button(">>");
	bMinusOne = new Button("<<");
	bAuto     = new Button("Auto");
          
	bPlusOne.addActionListener(this);
	bMinusOne.addActionListener(this);
	bAuto.addActionListener(this);
          
	redrawDelay=500;  // default delay value
	lDelay = new JLabel("Frame Refresh Delay:", JLabel.CENTER);
	delayField = new TimeTextField("500 ms", 8);
	delayField.addActionListener(this);
	
	//sharon implementing slider bar
	slider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0,
	displayPanel.getNumI());
	slider.addAdjustmentListener(this);
          
	setRanges = new Button("Set Ranges");
	setRanges.addActionListener(this);

	titlePanel.setBackground(MainWindow.runObject[myRun].background);
	titlePanel.setForeground(MainWindow.runObject[myRun].foreground);
	Font titleFont = new Font("SansSerif", Font.BOLD, 16);
	lTitle = new JLabel("", JLabel.CENTER);
	lTitle.setFont(titleFont);
	lTitle.setForeground(MainWindow.runObject[myRun].foreground);
	setTitleInfo(0);
	titlePanel.add(lTitle);
          
	lStatus = new JLabel("");
	statusPanel.add(lStatus, "Center");
	statusPanel.setBackground(MainWindow.runObject[myRun].background);
          
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
          
	controlPanel.setLayout(gbl);
	Util.gblAdd(controlPanel, slider,     gbc, 0,0, 6,1, 1,1);
	Util.gblAdd(controlPanel, bMinusOne,  gbc, 0,1, 1,1, 1,1);
	Util.gblAdd(controlPanel, bPlusOne,   gbc, 1,1, 1,1, 1,1);
	Util.gblAdd(controlPanel, bAuto,      gbc, 2,1, 1,1, 1,1);
	Util.gblAdd(controlPanel, lDelay,     gbc, 3,1, 1,1, 1,1);
	Util.gblAdd(controlPanel, delayField, gbc, 4,1, 1,1, 1,1);
	Util.gblAdd(controlPanel, setRanges,  gbc, 5,1, 1,1, 1,1);
          
	mainPanel.setBackground(MainWindow.runObject[myRun].background);
	mainPanel.setLayout(gbl);
          
	Util.gblAdd(mainPanel,titlePanel, gbc, 0,0, 1,1, 1,0);
	Util.gblAdd(mainPanel, displayPanel,  gbc, 0,1, 1,1, 1,1);
	Util.gblAdd(mainPanel, colorbarPanel, gbc, 0,2, 1,1, 1,0);
	Util.gblAdd(mainPanel, statusPanel,   gbc, 0,3, 1,1, 1,0);
	Util.gblAdd(mainPanel, controlPanel,  gbc, 0,4, 1,1, 1,0); 
          
	getContentPane().add(mainPanel,"Center"); //bug is here
	layoutComplete = true;
    }   

    protected void setStatusInfo(int p, int i, int u)
    {
	String status;
	if (p < 0) {
	    status = "";
	} else {
	    status = "Processor " + p + ": Usage = " + u + 
		"% at " + U.humanReadableString(startTime + i*intervalSize);
	}
	lStatus.setText(status);
	lStatus.invalidate();
	statusPanel.validate();
    }   

    public void setTitleInfo(int i)
    {
	String title = "Processor Usage at " + 
	    U.humanReadableString(startTime + i*intervalSize) +
	    "(" + U.humanReadableString(intervalSize) + " step)";
	lTitle.setText(title);
	lTitle.invalidate();
	titlePanel.validate();
    }   
}
