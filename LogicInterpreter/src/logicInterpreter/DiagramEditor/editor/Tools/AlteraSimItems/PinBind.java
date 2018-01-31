package logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems;
/**
 * Klasa przechowująca informacje dotyczące przypisań wejść/wyjść diagramu do elementów na płytce Altera
 * @author pango
 *
 */
public class PinBind {

	private String pinname;
	private int elemindex;
	public String getNodeName() {
		return pinname;
	}
	public void setNodeName(String nodename) {
		this.pinname = nodename;
	}
	public int getBoardElemIndex() {
		return elemindex;
	}
	public void setBoardElemIndex(int i) {
		this.elemindex = i;
	}
	public PinBind(String nodename, int elemindex) {
		this.pinname = nodename;
		this.elemindex = elemindex;
	}
	
	
}
