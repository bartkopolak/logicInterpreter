package logicInterpreter.LogicElementsModels.Nodes;

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
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return new DiagramOutputBean(name);
	}
	
}
