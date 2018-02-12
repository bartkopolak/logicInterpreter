package logicInterpreter.LogicElementsModels;

import logicInterpreter.LogicElementsModels.Nodes.InputBean;

public class DistanceToInputBean implements Comparable<DistanceToInputBean> {
	
	private BlockBean block;
	
	public BlockBean getBlock() {
		return block;
	}

	public void setBlock(BlockBean block) {
		this.block = block;
	}

	private int distance;
	
	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public DistanceToInputBean() {

		
	}

	@Override
	public int compareTo(DistanceToInputBean d) {
		
		return (int)Math.signum(distance - d.getDistance());
	}

}
