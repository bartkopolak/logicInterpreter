package logicInterpreter.Nodes;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fathzer.soft.javaluator.StaticVariableSet;

import logicInterpreter.BoolInterpret.BoolEval;
import logicInterpreter.BoolInterpret.BoolInterpreter;
import logicInterpreter.DiagramInterpret.DiagramBean;

public class BlockBean {

	private String name;
	private final List<BlockInputBean> inputs = new ArrayList<BlockInputBean>();
	private final List<BlockOutputBean> outputs = new ArrayList<BlockOutputBean>();
	private String type;
	private DiagramBean diagram = null; //uzywany tylko gdy typ to diagram
	
	public BlockBean(){
	}
	
	public DiagramBean getDiagram() {
		if(type.equals("diagram")){
			return diagram;
		}
		else{
			return null;
		}
	}

	public void setDiagram(DiagramBean diagram) {
		if(type.equals("diagram")){
			this.diagram = diagram;
		}

	}

	public String getType() {
		return type;
	}

	public void setType(String type) { //pozwalaj tylko na jednokrotne ustawienie typu bloczka
		if(this.type == null)
			this.type = type;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	public BlockInputBean getInput(int index){
		return inputs.get(index);
	}
	
	public BlockInputBean getInput(String name){
		for(int i=0; i<inputs.size(); i++){
			BlockInputBean in = inputs.get(i);
			if(in.getName().equals(name)){
				return in;
			}
		}
		return null;
	}
	public void addInput(String name){
		BlockInputBean o = new BlockInputBean(this, name);
		inputs.add(o);
	}
	//dla leniwych cala lista za fryko
	public List<BlockInputBean> getInputList(){
		return inputs;
	}
	
	
	public BlockOutputBean getOutput(int index){
		return outputs.get(index);
	}
	public BlockOutputBean getOutput(String name){
		for(int i=0; i<outputs.size(); i++){
			BlockOutputBean o = outputs.get(i);
			if(o.getName().equals(name)){
				return outputs.get(i);
			}
		}
		return null;
	}
	public void addOutput(String name, String formula){
		BlockOutputBean o = new BlockOutputBean(this, name, formula);
		outputs.add(o);
	}
	//jesli bydziesz chcial cala liste
	public List<BlockOutputBean> getOutputList(){
		return outputs;
	}
	/**
	 * Zwraca wynik funkcji boolowskiej dla 1 wyjscia bloczku <br> Stan wyjscia zapisywany jest w wlaciwosci state wyjscia OutputBean
	 * @param output - obiekt wyjscia
	 * @param inputStates - tablica wartosci wejsc
	 * @return
	 */
	public void evaluate(){
		//typ:formula
		if(type.equals("formula")){
				boolean resultState = false;
			//pobierz nazwy wejsc w celu zdefinowania zmiennych
			String[] inputNames = new String[inputs.size()];
			for(int i=0; i<inputs.size(); i++){
				inputNames[i] = inputs.get(i).getName();
			}
			
			BoolEval eval = new BoolEval();
			//definowanie zmiennych
			StaticVariableSet<Boolean> variables = new StaticVariableSet<Boolean>();
			for(int i=0; i<inputs.size(); i++){
				BlockInputBean input = inputs.get(i);
				variables.set(input.getName(), input.getState());
			}
			//wykonaj funkcje logiczna dla kazdego wyjscia
			for(BlockOutputBean output : outputs){
				String func = BoolInterpreter.InsertMultiplyOperators(output.getFormula(), inputNames);
				resultState = eval.evaluate(func, variables);
				output.setState(resultState);
			}
		}
		else if(type.equals("diagram")){
			if(diagram != null){
				for(BlockInputBean b : inputs){
					diagram.getInput(b.getName()).setState(b.getState());
				}
				diagram.evaluate();
				for(BlockOutputBean b : outputs){
					b.setState(diagram.getOutput(b.getName()).getState());
				}
			}
			
		}
		else {
			
		}
		
		//TODO:dodaj ewaluacje typu diagram
		
	}
	
	public void printTruthTable(BlockOutputBean outputNode, OutputStream outputStream){
		int size = inputs.size();
		
		//System.out.println(x.getFormula());
		for(int j=0; j<size; j++){
			
			System.out.print(getInput(j).getName() + "\t");
		}
		System.out.print(outputNode.getName());
		System.out.println();
		for(int i=0; i<Math.pow(2, size); i++){
			
			Boolean[] states = new Boolean[size];
			for(int j=0; j<size; j++){
				states[j] = ((i & (int)Math.pow(2, size-j-1)) == 0)?false:true;
				System.out.print(states[j] + "\t");
				getInput(j).setState(states[j]);
			}
			evaluate();
			System.out.println(outputNode.getState());
		}
	}

	@Override
	public String toString() {
		return "BlockBean ["+ name + "]";
	}
	
	
	
	
}
