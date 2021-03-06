package projections.gui;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class InvalidFileDialog extends Dialog
  implements ActionListener
{


	protected InvalidFileDialog(Frame parent, Exception e)
  {
    super(parent, "ERROR", true);
    
    Button bOK = new Button("OK");
    bOK.addActionListener(this);
    Label l1 = new Label("Projections Exception on Initialization:", 
			 Label.LEFT);
    TextArea l2 = new TextArea(e.toString(), 5, 40, 
			       TextArea.SCROLLBARS_BOTH);
    
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    gbc.fill = GridBagConstraints.BOTH;
    Util.gblAdd(this, l1,          gbc, 0,0, 1,1, 1,1, 2, 2, -2, 2);
    Util.gblAdd(this, l2,          gbc, 0,1, 1,1, 1,1, -2, 2, 2, 2);
    gbc.fill = GridBagConstraints.VERTICAL;
    Util.gblAdd(this, bOK,         gbc, 0,2, 2,1, 0,1);
    
    pack();
  }   

  public void actionPerformed(ActionEvent evt)
  {
    setVisible(false);
    dispose();
  }   
}
