package logicInterpreter.Nodes;

public class InputBean {
	protected String name;
	protected boolean state;
	protected OutputBean from;
	
	/**
	 * Klasa abstrakcyjna wejścia w układzie logicznym
	 * @param name - nazwa wejścia
	 */
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
	/**
	 * 
	 * @return stan logiczny wejścia
	 */
	public boolean getState() {
		return state;
	}
	/**
	 * Ustawia stan logiczny wejścia
	 * @param state
	 */
	public void setState(boolean state) {
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
