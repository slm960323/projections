package projections.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class PieChartWindow extends Frame{

	// Temporary hardcode. This variable will be assigned appropriate
    // meaning in future versions of Projections that support multiple
    // runs.
    private int myRun = 0;

//    private MainWindow      mainWindow;
    private MyPanel         displayPanel;
    private float[]         data;
    private int             dataLen;
    private float           thresh;
    private int[][]         arc;        // An array of arcs, storing its starting degree, ending degree and index in data that it correlates to
    private int             numArcs;
    private Color[]         colors;
    private int             leftBuf;
	private int rightBuf;
	private int topBuf;
	private int bottomBuf;

    // PieChart Values
    private int             diameter;
    private int             centerX;
	private int centerY;
	private int 			numEntries;

    // Flags
    private int             outOfRadius;
    private int             hasChanged;
    
    // Cursor values
    private int 			currX;
	private int currY;
    private int 			dist;
    private int 			degree;
    private int 			currArcIndex;
	
    // Bubble values
    private Bubble			bubble;
    private String[]		bString;

    /* PieChartWindow Constructor
     * inputs
     *		data 		= an array of the calculated averages
     *		dataLen 	= length of data array
     *		thresh		= the threshold under which items will not be displayed
     *		colors		= array of Colors to be used for display, indexing is the same as data
     *	
     * Creates the window and initializes data
     */
    
    protected PieChartWindow(float[] data, int dataLen, float thresh, Color[] colors){
        this.data = data;
        this.dataLen = dataLen;
        this.thresh = thresh;
        this.colors = colors;
	bString = new String[3];
	bString[0] = " ";
	bString[1] = " ";
	bString[2] = "AVERAGE";
	bubble = null;
	currArcIndex = -1;

        leftBuf = rightBuf = topBuf = bottomBuf = 25;
	
        arc = null;
        outOfRadius = 1;
	
        addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){close();}
	    });
	
        addComponentListener(new ComponentAdapter(){
		public void componentResized(ComponentEvent e){
		    setCursor(new Cursor(Cursor.WAIT_CURSOR));
		    setSizes();
		    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	    });
	
        setTitle("Projections Pie Chart");
        createLayout();
	
        pack();
        setVisible(true);
    }
    
    /* close()
     *   Closes the PieChartWindow
     *
     */
    
    private void close(){
        setVisible(false);
        dispose();
    }

    /* setSizes()
     * calculates size of pie chart using the buffers defined in the constructor
     * 
     */
    private void setSizes(){
        int width = (displayPanel.getSize().width);
        int height = (displayPanel.getSize().height);
	
        if(width < height)
            diameter = width - leftBuf - rightBuf;
        else
            diameter = height - topBuf - bottomBuf;
	
        centerX = leftBuf + diameter/2;
        centerY = topBuf + diameter/2;
    }

    /* createLayout()
     * creates and adds the panel needed for checking mouse position
     */
    private void createLayout(){
	displayPanel = new MyPanel(this);
	displayPanel.setLayout(null);
	displayPanel.setBackground(Color.black);
	add(displayPanel);
    }


    private class MyPanel extends Panel implements MouseMotionListener{
		private PieChartWindow pcw;
	
        private MyPanel(PieChartWindow pcw){
            addMouseMotionListener(this);
            this.pcw = pcw;
        }

	/* paint(g)
	 * draws out the pie chart and calculates and creates the array (arc[][]) to store the degree of each arc and the index in data the
	 * arc correlates to
	 */
        public void paint(Graphics g){
            int arcPos = 0;
            numArcs = 0;
            int count = 0;
	    
            for(int k=0; k<dataLen-1; k++){
                if(data[k] >= thresh)
                    numArcs++;
            }
	    
            arc = new int[numArcs][];
	    
            for(int k=0; k<numArcs; k++)
                arc[k] = new int[3];
	    
            for(int k=0; k<dataLen-1; k++){
                if(data[k] < thresh) continue;
		
                g.setColor(colors[k]);
                g.fillArc(leftBuf, topBuf, diameter, diameter, arcPos, (int)(data[k]*3.60));
		
                arc[count][0] = arcPos;
                arcPos += (int)(data[k]*3.60);
                arc[count][1] = arcPos;
                arc[count][2] = k;
                count++;
            }
            
            g.setColor(Color.gray);
            g.drawOval(leftBuf, topBuf, diameter, diameter);
            
        }
		
	/* mouseMoved()
	 * calculates the position of the mouse and determines what arc(if any) the mouse is over and creates/displays the bubble
	 */
	public void mouseMoved(MouseEvent e){
	    currX = e.getX();
	    currY = e.getY();
	    
	    double dX = Math.abs((double)(currX - centerX));
	    double dY = Math.abs((double)(currY - centerY));
	    
	    dist = (int)Math.sqrt(Math.pow(dX, 2.00) + Math.pow(dY, 2.00));
	    
	    if(dist <= diameter/2){
		if(outOfRadius == 1){
		    hasChanged = 1;
		    outOfRadius = 0;
		}
		
		degree = (int)Math.toDegrees(Math.atan(dY/dX));
		
		if((currX < centerX) && (currY <= centerY)) 		// Cursor in second quadrant
		    degree = 180 - degree;
		else if((currX < centerX) && (currY > centerY)) 	// Cursor in thrid quadrant
		    degree += 180;
		else if((currX > centerX) && (currY >= centerY))	// Cursor in fourth quadrant
		    degree = 360 - degree;
		// Cursor in first quadrant
		
		int inArc = 0;
		for(int k=0; k<numArcs; k++){
		    if((degree >= arc[k][0]) && (degree < arc[k][1])){
			inArc = 1;
			
			if(currArcIndex != arc[k][2]){
			    hasChanged = 1;
			    currArcIndex = arc[k][2];
			}else{	// all ready in arc, so ballon should exist.Just move bubble location
			    if(bubble != null)
				bubble.setLocation(currX +20 +pcw.getBounds().x, currY +40 +pcw.getBounds().y);
			}
			
			break;
		    }
		}
		
		if(inArc == 0){
		    if(bubble != null){
			bubble.setVisible(false);
			bubble.dispose();
			bubble = null;
		    }	
		    hasChanged = 0;
		    currArcIndex = -1;
		}
		
		if(hasChanged == 1){
		    if(bubble != null){
			bubble.setVisible(false);
			bubble.dispose();
			bubble = null;
		    }
		    
		    numEntries = MainWindow.runObject[myRun].getNumUserEntries();
		    
		    degree = (int)Math.toRadians(degree);
		    
		    int x = currX + 20 + pcw.getBounds().x;
		    int y = currY + 40 + pcw.getBounds().y;
		    
		    if(currArcIndex < numEntries)
			bString[0] = MainWindow.runObject[myRun].getEntryFullNameByID(currArcIndex);
		    else if (currArcIndex == numEntries)
			bString[0] = "PACKING";
		    else if (currArcIndex == numEntries+1)
			bString[0] = "UNPACKING";
		    else if (currArcIndex == numEntries+2)
			bString[0] = "IDLE";
		    
		    bString[1] = "usage: " +data[currArcIndex] +"%";
		    
//		    Rectangle bounds = (displayPanel.getGraphicsConfiguration()).getBounds();
		    bubble = new Bubble(displayPanel, bString);
		    bubble.setLocation(new Point(x,y));
		    bubble.setVisible(true);
		    hasChanged=0;
		}	
	    }else{					// Cursor not inside of pie graph
		outOfRadius = 1;
		if(bubble != null){
		    bubble.setVisible(false);
		    bubble.dispose();
		    bubble=null;
		}
	    }
	}
		
	public void mouseDragged(MouseEvent e){
	    mouseMoved(e);
	}
        
        public Dimension getPreferredSize(){
            return new Dimension(500,500);
        }

    }
    
}
