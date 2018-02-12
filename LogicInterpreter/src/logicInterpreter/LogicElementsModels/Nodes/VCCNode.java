package logicInterpreter.LogicElementsModels.Nodes;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;

public class VCCNode extends DiagramInputBean {

	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "vcc";
	}

	@Override
	public ThreeStateBoolean getState() {
		// TODO Auto-generated method stub
		return ThreeStateBoolean.TRUE;
	}

	@Override
	public String toString() {
		return "inputs.vcc" + name;
	}
}
