package logicInterpreter.Tools.AlteraSimItems;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

public class Switch extends JCheckBox{
	
	private Image imgoff;
	private Image imgon;
	
	public Switch() {
		super();
		setSelected(false);
		imgon = new ImageIcon("img/switchon.png").getImage();
		imgoff = new ImageIcon("img/switchoff.png").getImage();
		this.setPreferredSize(new Dimension(imgon.getWidth(null), imgon.getHeight(null)));
		
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		g.clearRect(0, 0, getWidth(), getHeight());
			if(isSelected()) {
				g.drawImage(imgon , 0, 0, null);
			}	
			else {
				g.drawImage(imgoff, 0, 0, null);	
			}
			
			
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
