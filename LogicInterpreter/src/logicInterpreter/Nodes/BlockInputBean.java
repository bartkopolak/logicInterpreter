package logicInterpreter.Nodes;

import logicInterpreter.DiagramInterpret.BlockBean;

public class BlockInputBean extends InputBean{
	private BlockBean parent;
	
	public BlockInputBean() {
		this(null, "");
	}
	
	public BlockInputBean(String name){
		this(null,name);
	}
	
	public BlockInputBean(BlockBean parent, String name){
		super(name);
		this.parent = parent;
	}
	
	public BlockInputBean(BlockBean parent, BlockInputBean blockInput){
		this(parent, blockInput.getName());
	}
	/**
	 * 
	 * @return blok podłączony do tego wejścia
	 */
	public BlockBean getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return parent.getName() +"."+name;
	}

	
}
