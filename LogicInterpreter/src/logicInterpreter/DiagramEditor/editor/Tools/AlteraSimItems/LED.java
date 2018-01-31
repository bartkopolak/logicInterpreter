package logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JCheckBox;

public class LED extends JCheckBox{
	
	Color offColor;
	Color onColor;
	int width = 9;
	int height = 12;
	public LED() {
		super();
		setEnabled(false);
		this.setPreferredSize(new Dimension(width,height));
		this.setDoubleBuffered(true);
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		g.clearRect(0, 0, this.getBounds().width, this.getBounds().height);
		if(isSelected()) {
			g.setColor(onColor);
		}	
		else {
			
			g.setColor(offColor);
		}
		g.fillRect(0, 0, width, height);
			
	}
	
	@Override
	public void update(Graphics g) {
		paintComponent(g);
	}


	@Override
	public String toString() {
		return getName();
	}
}
