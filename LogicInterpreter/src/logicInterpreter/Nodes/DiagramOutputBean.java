package logicInterpreter.Nodes;

public class DiagramOutputBean extends InputBean{
		
	public DiagramOutputBean(String name){
		super(name);
	}
	public DiagramOutputBean(){
		super();
	}
	
	@Override
	public String toString() {
		return "outputs." + name;
	}
}
