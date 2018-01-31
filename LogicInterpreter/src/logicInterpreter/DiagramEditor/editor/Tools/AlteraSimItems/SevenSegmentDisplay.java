package logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SevenSegmentDisplay extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private List<JCheckBox> segments = new ArrayList<JCheckBox>(7);
	
	private Rectangle[] segmentDrawData = {
			new Rectangle(6, 5, 13, 2),		//a
			new Rectangle(19, 7, 2, 12),	//b
			new Rectangle(19, 21, 2, 12),	//c
			new Rectangle(6, 33, 13, 2),	//d
			new Rectangle(4, 21, 2, 12),	//e
			new Rectangle(4, 7, 2, 12),		//f
			new Rectangle(6, 19, 13, 2)		//g
	};
	
	private void repaintComponent() {
		repaint();
	}
	
	ItemListener repaintOnChangeListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			repaintComponent();
			
		}
	};
	
	private String[] names = {"A", "B", "C", "D", "E", "F", "G"};
	public SevenSegmentDisplay() {
		for(int i=0; i<7; i++) {
			Segment input = new Segment();
			input.setParentPanel(this);
			input.setName(names[i]);
			input.addItemListener(repaintOnChangeListener);
			segments.add(input);
		}
			
		setPreferredSize(new Dimension(25, 40));
	}
	
	public List<JCheckBox> getSegmentList(){
		return segments;
	}
	

	
	
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.red);
		for(int i=0; i<7; i++) {
			if(segments.get(i).isSelected())
				g.fillRect(segmentDrawData[i].x, segmentDrawData[i].y, segmentDrawData[i].width, segmentDrawData[i].height);
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
