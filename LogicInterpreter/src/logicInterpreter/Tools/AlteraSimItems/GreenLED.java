package logicInterpreter.Tools.AlteraSimItems;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class GreenLED extends JCheckBox {
	Color offColor = new Color(4,74,6);
	Color onColor = new Color(106,247,108);
	int width = 9;
	int height = 12;
	public GreenLED() {
		super();
		setEnabled(false);
		this.setPreferredSize(new Dimension(width,height));
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		g.clearRect(0, 0, width, height);
		if(isSelected()) {
			g.setColor(onColor);
		}	
		else {
			
			g.setColor(offColor);
		}
		g.fillRect(0, 0, width, height);
			
	}

	@Override
	public String toString() {
		return getName();
	}
	
	
}
