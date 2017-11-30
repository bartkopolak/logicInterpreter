package logicInterpreter.Nodes;

public class DiagramInputBean extends OutputBean{

	
	public DiagramInputBean(String name){
		super(name);
	}
	
	public DiagramInputBean(){
		super();
	}

	@Override
	public String toString() {
		return "inputs." + name;
	}
	
}
