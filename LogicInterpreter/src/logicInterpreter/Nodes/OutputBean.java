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
	/**
	 * 
	 * @return stan tego wyjścia 
	 */
	public boolean getState() {
		return state;
	}
	/**
	 * Ustawia stan logiczny wyjścia układu
	 * @param state
	 */
	public void setState(boolean state) {
		this.state = state;
	}
	/**
	 * Dodaje połaczenie z podanym wejściem
	 * @param toInput
	 */
	public void addLink(InputBean toInput){
		wire.addToInput(toInput);
	}
	/**
	 * Zrywa połączenie z wszystkimi wejściami układu
	 */
	public void resetLinks(){
		wire.resetLinks();
	}
	/**
	 * Zwraca obiekt połączenia z wejściami
	 * @return
	 */
	public Wire getWire(){
		return wire;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
