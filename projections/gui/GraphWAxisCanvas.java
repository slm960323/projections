package projections.gui;

import java.awt.*;

public class GraphWAxisCanvas extends Canvas 
{
   private GraphData data;
   private int width;
   private int textheight;
   private int labelwidth;
   private int labelincrement;
   private double deltay;
   
   public GraphWAxisCanvas()
   {
      width = 0;
      textheight = 0;
      labelwidth = 0;
      deltay = 0;
      labelincrement = 0;
      setBackground(Color.black);
      setForeground(Color.white);
   }
   
   public void print(Graphics pg)
   {
      setBackground(Color.white);
      setForeground(Color.black);
      int w = getSize().width;
      int h = getSize().height;
      pg.clearRect(0, 0, w, h);
      paint(pg);
      setBackground(Color.black);
      setForeground(Color.white);
   }   
   
   public void setData(GraphData data)
   {
      this.data = data;
   }   
   
   public int getPreferredWidth()
   {
      if(width == 0)
      {
         Graphics g = getGraphics();
         if(g != null)
         {
            FontMetrics fm = g.getFontMetrics(g.getFont());
            width = fm.stringWidth("" + 100) + fm.stringWidth("%") + 20;
            g.dispose();
         }   
      }
      
      return width;
   }   
   
   public void setBounds(int x, int y, int w, int h)
   {
      if(textheight == 0)
      {
         Graphics g = getGraphics();
         FontMetrics fm = g.getFontMetrics(g.getFont());
         textheight = fm.getHeight();
         labelwidth = fm.stringWidth("%");
         g.dispose();
      }    
      
      deltay = (double)((h - data.offset - data.offset2) / 100.0);
      labelincrement = (int)(Math.ceil((textheight + 10) / deltay));
      labelincrement = Util.getBestIncrement(labelincrement);
   
      data.wscale = deltay;
      
      super.setBounds(x, y, w, h);
   }         
   
   public void paint(Graphics g)
   {
      if(data == null)
         return;
         
      int w = getSize().width;
      int h = getSize().height - data.offset2;

      if(textheight == 0)
      {
         FontMetrics fm = g.getFontMetrics(g.getFont());
         textheight = fm.getHeight();
         labelwidth = fm.stringWidth("%");
      }   
     
      g.setColor(getForeground());
      
      g.drawString("%", w - 5 - labelwidth, h/2);
      g.drawLine(5, data.offset, 5, h-1); 
      
      int cury;
      for(int y=0; y<=100; y++)
      {
         cury = h - (int)(y * deltay)-1; 
            
         if(y % labelincrement == 0)
         {  
            g.drawLine(0, cury, 10, cury);
            cury += (int)(0.5*textheight); 
            g.drawString("" + y, 15, cury);
         }
         else
         {
            g.drawLine(3, cury, 7, cury);
         }
      }                  
   }                         
}
      
      
