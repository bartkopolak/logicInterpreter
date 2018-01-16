package logicInterpreter.Nodes;

import java.io.Serializable;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class InputBean extends Node implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337196295572766075L;
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
	/**
	 * 
	 * @return wyjście, z którym połączone jest wejście
	 */
	public OutputBean getFrom() {
		return from;
	}
	
}
