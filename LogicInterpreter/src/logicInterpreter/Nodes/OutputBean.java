package logicInterpreter.Nodes;



public class OutputBean {
	
	protected String name;
	protected boolean state;
	protected Wire wire;
	
	
	public OutputBean(String name){
		this.name = name;
		state = false;
		wire = new Wire(this);
	}
	
	public OutputBean(){
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
	public void addLink(InputBean toInput){
		wire.addToInput(toInput);
	}
	public void resetLinks(){
		wire.resetLinks();
	}
	public Wire getWire(){
		return wire;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
