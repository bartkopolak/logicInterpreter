package logicInterpreter.Nodes;

public class BlockInputBean extends InputBean{
	private BlockBean parent;
	
	public BlockInputBean(String name){
		this(null,name);
	}
	
	public BlockInputBean(BlockBean parent, String name){
		super(name);
		this.parent = parent;
	}
	/**
	 * 
	 * @return blok podłączony do wejścia
	 */
	public BlockBean getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return parent.getName() + "." + name;
	}

	
}
