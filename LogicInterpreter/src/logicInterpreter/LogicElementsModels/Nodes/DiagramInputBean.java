package logicInterpreter.LogicElementsModels.Nodes;

public class DiagramInputBean extends OutputBean{

	private String position;
	
	public DiagramInputBean(String name){
		super(name);
	}
	
	public DiagramInputBean(){
		super();
		position = "west";
	}

	@Override
	public String toString() {
		return "inputs." + name;
	}

	public void setPosition(String attribute) {
		position = attribute;
	}

	public String getPosition() {
		return position;
	}
	
	
	
}
