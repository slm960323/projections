package projections.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import javax.swing.*;

import org.apache.xpath.operations.And;
import org.apache.xpath.operations.Variable;

import projections.gui.*; 
import projections.gui.graph.Graph;

/**
 * 
 * This class is for displaying general profiling data
 * Y-Axis is the percent usage while X-Axis is a list strings where every
 * string must be displayed! 
 * Originated from Graph.java
 */
public class ProfileGraph extends JPanel 
    implements MouseMotionListener
{

    //Data source: display profile data such as float[][]
    //For convinience, currently set type to float[][]
    //dataSource[x][y] indicates the section y on bar x should have this amount of usage
    float[][] dataSource;
    //colorsPool[x][y] indicates the section y on bar x should use this color
    private int[][] colorsMap;
    private Color[] colorsPool;
    //sectionNames[x][y] indicates the section y on bar x should have this name
    private String[][] sectionNames;
    
    //About general view of the profile graph
    //The graph title may have several lines to display
    private String[] graphTitles; 
    private int gTitleH;
    private int canvasWidth, canvasHeight;
    
    //baseWidth and baseHeight is whatever the width of height
    //of the parent window at the time. Variable xscale will
    //control how much more of the graph one can see.
    private int baseWidth = -1;
    private int baseHeight = -1;

    //About X-Axis settings
    private int xWidth;
    private int originX;
    private double xscale;
    private String xTitle;
    private String xUnit;
    private String[] xNames;
    private double pixelIncX;

    //About Y-Axis settings
    private int yHeight;
    private int originY;
    private double yscale;
    private String yTitle;
    private double pixelIncY, tickIncY;

    //About font settings on the canvas
    private static final int FONT_SIZE = 12;   
    private Font font = null;
    private FontMetrics fm = null;
   
    private double barWidth;

    //About showing popup texts
    private Bubble bubble;
    private int bubbleXVal;
    private int bubbleYVal;

    public ProfileGraph()
    {
	setPreferredSize(new Dimension(600,450));	
	
        //need initialize data source etc later!!!!!!!
        dataSource = null;
        xscale = 1.0;
	yscale = 1.0;
	
	addMouseMotionListener(this);
    }

    public ProfileGraph(String[] titles){
        this();
        graphTitles = titles;
    }

    public void setXAxis(String title, String unit, String[] names) {
        xTitle = title;
        xUnit = unit;
        xNames = names;
    }

    public void setYAxis(String title) {
        yTitle = title;
    }

    public void setGraphTiltes(String[] gTitles) {
        graphTitles = gTitles;
    }
    
    public Dimension getMinimumSize() {
	return new Dimension(600,450);
    }


    // ***** API Interface to the control panel *****
    public void setDisplayDataSource(float[][] d, int[][] cMap, Color[] c, String[][] n){
        dataSource = d;
        colorsMap = cMap;
        colorsPool = c;
        sectionNames = n;
    }

    public void setScaleX(double val) {
	xscale = val;
	setPreferredSize(new Dimension((int)(baseWidth*xscale),
				       (int)(baseHeight*yscale)));
	revalidate();
	repaint();
    }

    public double getScaleX(){
        return xscale;
    }

    public void setScaleY(double val) {
	yscale = val;
	setPreferredSize(new Dimension((int)(baseWidth*xscale),
				       (int)(baseHeight*yscale)));
	revalidate();
	repaint();
    }

    public double getScaleY(){
        return yscale;
    }

    /**  
     *  getXValue
     *	returns the x value of the bar graph the mouse is currently over
     * 	if the mouse is not over any bar data, then return -1.
     *  It is not supposed to work with line data of any sort.
     */
    private int getXValue(int xPos) {        
        if( (xPos > originX) && (xPos < canvasWidth+originX)) {
	    // get the expected value
	    int dist = xPos - originX;
            int expectedValue = (int)(dist/pixelIncX);
	    
            int midX = originX + (int)(expectedValue*pixelIncX) + (int)(pixelIncX/2);
	    
            // now find out if it the mouse actually falls 
            // onto the drawn bar
            if ((xPos > midX-(barWidth/2)) && (xPos < midX+(barWidth/2))) {
		    return expectedValue;
            }
        }
	return -1;
    }
	
    /**
     *  similar function with getXValue but more tricky
     */
    private int getYValue(int xVal, int yPos) {
        if(yPos<originY && yPos>gTitleH){
            int hPos;
            int dist = originY - yPos;
            for(hPos=0; hPos<dataSource[xVal].length; hPos++){
                double sH = dataSource[xVal][hPos]*pixelIncY;
                int intSH = ((int)sH==0)?1:(int)sH;
                dist -= intSH;
                if(dist<0) break;                      
            }
            if(dist<0)
               return hPos;  
        }
	return -1;
    }

    // ***** Painting Routines *****
    
    public void paint(Graphics g)
    {
	if (font == null) {
	    font = new Font("Times New Roman",Font.BOLD,FONT_SIZE);
	    g.setFont(font);
	    fm = g.getFontMetrics(font);
	}
	drawDisplay(g);
    }

    public void print(Graphics pg)
    {
	if (font == null) {
	    font = new Font("Times New Roman",Font.BOLD,FONT_SIZE);
	    pg.setFont(font);
	    fm = pg.getFontMetrics(font);
	}
	Color oldBackground = Analysis.background;
	Color oldForeground = Analysis.foreground;
	Analysis.background = Color.white;
	Analysis.foreground = Color.black;
	drawDisplay(pg);
	Analysis.background = oldBackground;
	Analysis.foreground = oldForeground;
    }

    /**
     * Dealing with showing popup window 
     */
    public void mouseMoved(MouseEvent e) {

        if(dataSource==null) return; 
        
	int x = e.getX();
    	int y = e.getY();
	
	int xVal = getXValue(x);
	int yVal = -1;
        if(xVal>-1) yVal = getYValue(xVal, y);

	if((xVal > -1) && (yVal > -1)) {
	    showPopup(xVal, yVal, x, y);
	} else if (bubble != null) {
	    bubble.setVisible(false);
	    bubble.dispose();
	    bubble = null;
	}
    }

    /**
     *  This is for the benefit of the bubble text placement. 
     *  We need to figure out the location of the graph's origin with
     *  respect to the screen (from which the bubble Window will attempt
     *  to set its location).
     *
     *  The algorithm will go through each parent of the graph and cumulate
     *  the offsets, finally tacking on the offset of the frame to the screen.
     */
    private Point getBubbleOffset() {
	Component c = this;
	int xOffset = c.getLocation().x;
	int yOffset = c.getLocation().y;

	while ((c = c.getParent()) != null) {
	    xOffset += c.getLocation().x;
	    yOffset += c.getLocation().y;
	}

	return new Point(xOffset,yOffset);
    }

    public void showPopup(int xVal, int yVal, int xPos, int yPos){        
	Point offset = getBubbleOffset();
	//String text[] = dataSource.getPopup(xVal, yVal);
        String[] text = new String[2];
        text[0] = sectionNames[xVal][yVal];
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);        
        text[1] = "Usage: "+df.format(dataSource[xVal][yVal])+"%";
		
	// else display ballon
	int bX, bY;
        
	// old popup still exists, but mouse has moved over a new 
	// section that has its own popup
	if (bubble != null && (bubbleXVal != xVal || bubbleYVal != yVal)){
	    bubble.setVisible(false);
	    bubble.dispose();
	    bubble = null;
	}
	
	if (bubble == null) {
            bubble = new Bubble(this, text);
            bubble.setLocation(xPos+offset.x, yPos+offset.y);
            bubble.setVisible(true);
            bubbleXVal = xVal;
            bubbleYVal = yVal;
	}
    }

    public void mouseDragged(MouseEvent e) {
    }

    private void drawDisplay(Graphics _g)
    {
        Graphics2D g = (Graphics2D)_g;
	
	g.setBackground(Analysis.background);
	g.setColor(Analysis.foreground);

	canvasWidth = getWidth();
	canvasHeight = getHeight();
	g.clearRect(0, 0, canvasWidth, canvasHeight);

	// if there's nothing to draw, don't draw anything!!!
	if (dataSource == null) {
	    return;
	}

        //reset font size to make graph more clear
        Font oldFont = font;        
        font = new Font("Times New Roman",Font.BOLD,18);
	g.setFont(font);
	fm = g.getFontMetrics(font);

	// xOffset and yOffsets are determined by the scrollbars that *may*
	// be asked to control this drawing component.
	int xOffset = 0;
	int yOffset = 0;

	// baseWidth is whatever the width of the parent window at the time.
	// xscale will control just how much more of the graph one can see.
	baseWidth = getParent().getWidth();
	baseHeight = getParent().getHeight();

	//draw X-Axis titles
        if(!xTitle.equals(""))
            g.drawString(xTitle,
		     (canvasWidth-fm.stringWidth(xTitle))/2 + xOffset, 
		     canvasHeight - 10 + yOffset);
    
	// draw Graph title
	for(int i=0; i<graphTitles.length; i++){
            g.drawString(graphTitles[i],
                         (canvasWidth-fm.stringWidth(graphTitles[i]))/2 + xOffset,
                         10 + (fm.getHeight())*(i+1) +yOffset);
        }

        gTitleH = fm.getHeight()*graphTitles.length+15;

	// draw Y-Axis title
	g.rotate(-Math.PI/2);
	g.drawString(yTitle, 
		     -(canvasHeight+fm.stringWidth(yTitle))/2 + yOffset, 
		     fm.getHeight() + xOffset);
	g.rotate(Math.PI/2);

        originX = fm.getHeight()*2 + fm.stringWidth(""+100);
        originY = canvasHeight - (30 + fm.getHeight()*2);

        font = oldFont;
        g.setFont(font);
        fm = g.getFontMetrics(font);
        drawXAxis(g);
        drawYAxis(g, gTitleH);
        
        //begin to draw the bar graph
        drawBarGraph(g);
        
    }

    private void drawXAxis(Graphics2D g) {

	// width available for drawing the graph
    	canvasWidth = (int)((baseWidth-30-originX)*xscale);

	// *NOTE* pixelincrementX = # pixels per value.
        int barCnt = dataSource.length;
	pixelIncX = ((double)canvasWidth)/barCnt;

	//setBestIncrements(X_AXIS, pixelincrementX, (int)maxvalueX);

	// draw xAxis
	g.drawLine(originX, originY, canvasWidth+originX, originY);

        // drawing xAxis divisions
        int curx = originX + (int)pixelIncX/2;
	for(int i=0; i<barCnt; i++){
            String s = xNames[i];
            g.drawString(s, curx-fm.stringWidth(s)/2, originY + 10 + fm.getHeight());
            curx += (int)pixelIncX;
        }
    }

    private void drawYAxis(Graphics2D g, int gTitleH) {

	// draw yAxis
	g.drawLine(originX, originY, originX , gTitleH);

        canvasHeight = originY-gTitleH;
	pixelIncY = (double)canvasHeight / 100;
	//setBestIncrements(Y_AXIS, pixelincrementY, (long)maxvalueY);
        int fH = fm.getHeight();
        double cury = (double)originY;
        int unitW = 5;
        int tickW = 2;
        for(int i=0; i<=100; i++){
            if(i%5==0){
                g.drawLine(originX, (int)cury, originX-unitW, (int)cury);
                String l = ""+i;
                g.drawString(l, originX-fm.stringWidth(l)-5, (int)cury+fH/2);
            } else {
                g.drawLine(originX, (int)cury, originX-tickW, (int)cury);
            }             
            cury -= pixelIncY;
        }
    }

    private void drawBarGraph(Graphics2D g) {
        Color gColor = g.getColor();
        double barStartX = originX+pixelIncX/8;
        barWidth = pixelIncX*3/4;
        for(int i=0;i<dataSource.length;i++) {            
            double barStartY = originY;         
            for(int j=0; j<dataSource[i].length; j++) {
                g.setColor(colorsPool[colorsMap[i][j]]);
                double sH = dataSource[i][j]*pixelIncY;
                int intSH = ((int)sH)==0?1:(int)sH;             
                g.fillRect((int)barStartX, (int)(barStartY-intSH), (int)barWidth, intSH);
                barStartY -= sH;
            }
            barStartX += pixelIncX;            
        }
        g.setColor(gColor);
    }
}