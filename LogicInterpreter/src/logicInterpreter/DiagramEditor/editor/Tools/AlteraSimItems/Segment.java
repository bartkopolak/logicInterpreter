package logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class Segment extends JCheckBox {

	private static final long serialVersionUID = 1L;
	
	JPanel parentDisplay;
	
	public Segment() {
		setSelected(false);
	}
	
	public void setParentPanel(JPanel parent) {
		parentDisplay = parent;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return parentDisplay.getName() + "." + getName();
	}
	
}
