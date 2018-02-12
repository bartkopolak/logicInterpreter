package logicInterpreter.LogicElementsModels.Nodes;

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
		state = ThreeStateBoolean.UNKNOWN;
		wire = new Wire(this);
	}
	
	public OutputBean(){
		this("");
	}
	
	public OutputBean(OutputBean o) {
		this(o.getName());
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

	@Override
	protected Object clone() throws CloneNotSupportedException {
		OutputBean b = new OutputBean(name);
		b.state = new ThreeStateBoolean(false);
		b.wire = new Wire(this);
		return b;
	}
	
	
}
