package logicInterpreter.Nodes;

import java.io.Serializable;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class OutputBean extends Node implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -974925799049827830L;

	protected Wire wire;
	
	
	public OutputBean(String name){
		this.name = name;
		state = new ThreeStateBoolean(false);
		wire = new Wire(this);
	}
	
	public OutputBean(){
		this("");
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
