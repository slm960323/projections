package projections.gui.Timeline;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/** A jpanel that is a specified size and color */
public class SolidColorJPanel extends JPanel{
	int width, height;
	Color color;
	
	public SolidColorJPanel(Color c, int w, int h){
		width = w;
		height = h;
		color = c;
	}

	public int getWidth(){
		return width;
	}

	public int getHeight(){
		return height;
	}
	
	public int getDesiredWidth(){
		return width;
	}

	public int getDesiredHeight(){
		return height;
	}

	protected void paintComponent(Graphics g)
	{
		System.out.println("Painting SolidColorJPanel with color=" + color + "getWidth()=" + getWidth() + " getHeight()" + getHeight() );
		g.setColor(color);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}