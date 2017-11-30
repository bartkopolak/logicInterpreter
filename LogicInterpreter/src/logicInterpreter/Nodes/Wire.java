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
	/**
	 * 
	 * @return identyfikator połączenia
	 */
	public String getId() {
		return id;
	}
	/**
	 * Ustawia identyfikator połączenia
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 
	 * @return obiekt wyjścia układu, od którego wychodzi połączenie
	 */
	public OutputBean getParent() {
		return parent;
	}
	/**
	 * Zmienia wejście połączenia
	 * @param parent - wyjście układu, do którego zmieniono wejście połączenia
	 */
	public void setParent(OutputBean parent) {
		this.parent = parent;
	}
	/**
	 * Dodaje wejście układu, do którego podłączone jest wyjście połączenia
	 * @param toInput
	 */
	public void addToInput(InputBean toInput){
		toInput.from = parent;
		to.add(toInput);
	}
	/**
	 * Pobiera listę wszystkich wejść układu, do których połączenie jest podłączone
	 * @return
	 */
	public List<InputBean> getToList() {
		return to;
	}
	/**
	 * Zrywa połączenie ze wszystkimi wejściami układu, do którego połączenie było podłączone
	 */
	public void resetLinks(){
		to.clear();
	}
	
}
