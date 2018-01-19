package logicInterpreter.Nodes;

import logicInterpreter.DiagramInterpret.BlockBean;

public class BlockOutputBean extends OutputBean {

	private String formula;
	private BlockBean parent;
	
	public BlockOutputBean() {
		this(null, "", "");
	}
	
	public BlockOutputBean(BlockBean parent, String name, String formula){
		super(name);
		this.formula = formula;
		this.parent = parent;
	}
	
	public BlockOutputBean(BlockBean parent, BlockOutputBean bo) {
		this(parent, bo.getName(), bo.getFormula());
	}
	/**
	 * 
	 * @return  funkcja logiczna
	 */
	public String getFormula() {
		return formula;
	}
	/**
	 * Ustawia funkcję logiczną wyjścia
	 * @param formula
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
	/**
	 * 
	 * @return blok podłączony do tego wyjścia
	 */
	public BlockBean getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return parent.getName() +"."+name;
	}
	
	
}
