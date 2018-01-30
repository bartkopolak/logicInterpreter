package logicInterpreter.Nodes;

import java.io.Serializable;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class Node implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9172163125423473373L;

	protected String name;
	protected ThreeStateBoolean state;
	
	public Node(String name){
		this.name = name;
		state = new ThreeStateBoolean(false);
	}
	
	public Node(){
		this("");
	}
	
	public String getName() {
		return name;
	}
	
	public String getVHDLName() {
		return name.replaceAll(" ", "");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 
	 * @return stan tego wyjścia 
	 */
	public ThreeStateBoolean getState() {
		return state;
	}
	/**
	 * Ustawia stan logiczny wyjścia układu
	 * @param state
	 */
	public void setState(ThreeStateBoolean state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
