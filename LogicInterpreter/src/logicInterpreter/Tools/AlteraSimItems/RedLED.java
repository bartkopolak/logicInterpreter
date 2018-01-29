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

public class RedLED extends LED {
	
	
	public RedLED() {
		super();
		offColor = new Color(73,5,9);
		onColor = new Color(241,78,86);
	}

	
}
