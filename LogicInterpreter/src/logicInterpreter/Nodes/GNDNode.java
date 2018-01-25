package logicInterpreter.Nodes;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class GNDNode extends DiagramInputBean {

	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "gnd";
	}

	@Override
	public ThreeStateBoolean getState() {
		// TODO Auto-generated method stub
		return ThreeStateBoolean.FALSE;
	}

	@Override
	public String toString() {
		return "inputs.gnd" + name;
	}
}
