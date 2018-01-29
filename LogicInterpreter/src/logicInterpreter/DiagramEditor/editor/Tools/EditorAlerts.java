package logicInterpreter.DiagramEditor.editor.Tools;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class EditorAlerts {


	public static void show(JComponent parent, String code) {
		if(code.equals("defaultBlockEditAttempt")) JOptionPane.showMessageDialog(parent, "Nie można edytować domyślnych bloków.", "Uwaga", JOptionPane.ERROR_MESSAGE);
		if(code.equals("blockRemoveError")) JOptionPane.showMessageDialog(parent, "Błąd przy usuwaniu bloku.", "Uwaga", JOptionPane.ERROR_MESSAGE);
		
	}
}
