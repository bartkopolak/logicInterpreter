package logicInterpreter.Nodes;

public class InputBean {
	protected String name;
	protected boolean state;
	protected OutputBean from;
	
	public InputBean(String name){
		this.name = name;
		state = false;
	}
	
	public InputBean(){
		this("");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return name;
	}

	public OutputBean getFrom() {
		return from;
	}
	
}
