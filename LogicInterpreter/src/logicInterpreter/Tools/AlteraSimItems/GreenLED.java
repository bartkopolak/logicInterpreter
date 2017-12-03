package logicInterpreter.Tools.AlteraSimItems;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class GreenLED extends JCheckBox {

	Image imgon, imgoff;

	public GreenLED() {
		super();
		setEnabled(false);
		imgon = new ImageIcon("img/greenLEDon.png").getImage();
		imgoff = new ImageIcon("img/greenLEDoff.png").getImage();
		this.setPreferredSize(new Dimension(imgon.getWidth(null), imgon.getHeight(null)));
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		g.clearRect(0, 0, getWidth(), getHeight());
		if(isSelected()) {
			
			g.drawImage(imgon , 0, 3, null);
		}	
		else {
			
			g.drawImage(imgoff, 0, 3, null);
		}
		
			
	}

	@Override
	public String toString() {
		return getName();
	}

	
	
}
