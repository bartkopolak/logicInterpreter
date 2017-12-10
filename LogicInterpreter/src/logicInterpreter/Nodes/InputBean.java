package logicInterpreter.Nodes;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class InputBean {
	protected String name;
	protected ThreeStateBoolean state;
	protected OutputBean from = null;
	
	/**
	 * Klasa abstrakcyjna wejścia w układzie logicznym
	 * @param name - nazwa wejścia
	 */
	public InputBean(String name){
		this.name = name;
		state = new ThreeStateBoolean(null);
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
	/**
	 * 
	 * @return stan logiczny wejścia
	 */
	public ThreeStateBoolean getState() {
		return state;
	}
	/**
	 * Ustawia stan logiczny wejścia
	 * @param state
	 */
	public void setState(ThreeStateBoolean state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return name;
	}
	/**
	 * 
	 * @return wyjście, z którym połączone jest wejście
	 */
	public OutputBean getFrom() {
		return from;
	}
	
}
