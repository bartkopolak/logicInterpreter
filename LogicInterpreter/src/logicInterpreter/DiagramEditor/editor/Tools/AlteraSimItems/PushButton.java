package logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

public class PushButton extends JCheckBox {

	private Image imgoff;
	private Image imgon;
	
	public PushButton() {
		
		super();
		setSelected(false);
		imgon = new ImageIcon("img/buttonon.png").getImage();
		imgoff = new ImageIcon("img/buttonoff.png").getImage();
		this.setPreferredSize(new Dimension(imgon.getWidth(null), imgon.getHeight(null)));
		
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
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
