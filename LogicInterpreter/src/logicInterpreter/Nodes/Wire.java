package logicInterpreter.Nodes;

import java.util.ArrayList;
import java.util.List;

public class Wire {
	private String id;
	private List<InputBean> to = new ArrayList<InputBean>();
	private OutputBean parent;
	
	public Wire(OutputBean parent){
		this(parent,"");
	}

	public Wire(OutputBean parent, String id){
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public OutputBean getParent() {
		return parent;
	}
	
	public void setParent(OutputBean parent) {
		this.parent = parent;
	}

	public void addToInput(InputBean toInput){
		toInput.from = parent;
		to.add(toInput);
	}
	
	public List<InputBean> getToList() {
		return to;
	}
	
	public void resetLinks(){
		to.clear();
	}
	
}
