
package projections.Tools.Timeline;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/** A class that handles the mouse events and painting of a user selection over the axis */
class AxisOverlayPanel extends JPanel implements MouseListener, MouseMotionListener
{ 
	
	private AxisOverlayPanel thisPanel;

	private Data  data;

	protected AxisOverlayPanel(Data data)
	{
		this.data = data;
		thisPanel = this;

		addComponentListener(new MyListener());
		
		addMouseListener(this);
		addMouseMotionListener(this);

		setOpaque(false);

	}   


	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if(data.selectionValid()){
			g.setColor(data.getForegroundColor());
			g.drawLine(data.leftSelection(),0, data.leftSelection(), getHeight()-1);
			g.drawLine(data.rightSelection(),0, data.rightSelection(), getHeight()-1);
		}

		if(data.highlightValid()){
			// Draw vertical line
			g.setColor(data.getForegroundColor());
			g.drawLine(data.getHighlight(),0, data.getHighlight(), getHeight()-1);
		}
		
	}


	public void mouseDragged(MouseEvent e) {
		data.setSelection2(e.getPoint().x);
		data.setHighlight(e.getPoint().x);
	}

	public void mouseMoved(MouseEvent e) {
		data.setHighlight(e.getPoint().x);
	}

	public void mouseClicked(MouseEvent e) {
		data.invalidateSelection();
	}

	public void mousePressed(MouseEvent e) {
		data.setSelection1(e.getPoint().x);
	}

	public void mouseReleased(MouseEvent e) {
		data.setSelection2(e.getPoint().x);
		data.setHighlight(e.getPoint().x);
	}

	public void mouseEntered(MouseEvent e) {
		setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		data.setHighlight(e.getPoint().x);
	}

	public void mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		data.removeHighlight();
	}


	public class MyListener implements ComponentListener {

		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
		}

		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
		}

		public void componentResized(ComponentEvent e) {
			// TODO Auto-generated method stub
			data.invalidateSelection();
			thisPanel.repaint();
		}

		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
		}
	
	}


}
