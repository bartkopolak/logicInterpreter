package logicInterpreter.Nodes;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.fathzer.soft.javaluator.StaticVariableSet;

import logicInterpreter.BoolInterpret.BoolEval;
import logicInterpreter.BoolInterpret.BoolInterpreter;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.RecurrentLoopException;

public class BlockBean {

	private String name;
	private final List<BlockInputBean> inputs = new ArrayList<BlockInputBean>();
	private final List<BlockOutputBean> outputs = new ArrayList<BlockOutputBean>();
	private String type;
	private DiagramBean diagram = null; //uzywany tylko gdy typ to diagram
	
	public BlockBean(){
	}
	
	/**
	 * Zwraca diagram przypisany do bloczka, jeśli typ bloczka = diagram<br>
	 * Jeśli blok jest innego typu, zwraca null<br>
	 * @return obiekt diagramu przypisany do bloczka, null jeśli typ jest inny od diagram
	 */
	public DiagramBean getDiagram() {
		if(type.equals("diagram")){
			return diagram;
		}
		else{
			return null;
		}
	}
	/**
	 * Przypisuje obiekt diagramu do bloczka, jeśli typ = diagram<br>
	 * Jeśli blok jest innego typu, nic się nie dzieje.
	 * @param diagram - obiekt układu logicznego
	 */
	public void setDiagram(DiagramBean diagram) {
		if(type.equals("diagram")){
			this.diagram = diagram;
		}

	}
	/**
	 * 
	 * @return typ bloczka
	 */
	public String getType() {
		return type;
	}
	/**
	 * Ustawia typ bloczka. Typ można ustawić tylko 1 raz, gdy typ jest null.<br>
	 * Typy:
	 * <ul>
	 * <li>formula - przechowuje funkcję boolowską</li>
	 * <li>diagram - przechowuje układ logiczny</li>
	 * </ul> 
	 * @param type - typ
	 */
	public void setType(String type) { //pozwalaj tylko na jednokrotne ustawienie typu bloczka
		if(this.type == null)
			this.type = type;
	}
	/**
	 * 
	 * @return nazwa bloczka
	 */
	public String getName() {
		return name;
	}
	/**
	 * Ustawia nazwę bloczka
	 * @param name - nazwa bloczka
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Zwraca obiekt wejścia bloczka po indeksie w liscie wejsc.<br>
	 * Jesli indeks > wielkosc listy, zwraca null
	 * @param index
	 * @return obiekt wejscia bloczka
	 */
	public BlockInputBean getInput(int index){
		try {
			return inputs.get(index);
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	/**
	 * Zwraca obiekt wejścia bloczka po nazwie<br>
	 * Jeśli obiekt o podanej nazwie nie istnieje, zwraca null
	 * @param name - nazwa
	 * @return obiekt wejścia
	 */
	public BlockInputBean getInput(String name){
		for(int i=0; i<inputs.size(); i++){
			BlockInputBean in = inputs.get(i);
			if(in.getName().equals(name)){
				return in;
			}
		}
		return null;
	}
	/**
	 * Dodaje wejscie bloczka do listy
	 * @param name - nazwa wejscia
	 */
	public void addInput(String name){
		BlockInputBean o = new BlockInputBean(this, name);
		inputs.add(o);
	}
	//dla leniwych cala lista za fryko
	/**
	 * Zwraca listę bloczkow.
	 * @return
	 */
	public List<BlockInputBean> getInputList(){
		return inputs;
	}
	
	/**
	 * Zwraca obiekt wyjścia bloczka po indeksie w liscie wejsc.<br>
	 * Jesli indeks > wielkosc listy, zwraca null
	 * @param index
	 * @return obiekt wyjscia bloczka
	 */
	public BlockOutputBean getOutput(int index){
		try {
			return outputs.get(index);
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	/**
	 * Zwraca obiekt wyjścia bloczka po nazwie<br>
	 * Jeśli obiekt o podanej nazwie nie istnieje, zwraca null
	 * @param name - nazwa
	 * @return obiekt wyjścia
	 */
	public BlockOutputBean getOutput(String name){
		for(int i=0; i<outputs.size(); i++){
			BlockOutputBean o = outputs.get(i);
			if(o.getName().equals(name)){
				return outputs.get(i);
			}
		}
		return null;
	}
	/**
	 * Dodaje obiekt wyjścia bloczka<br>
	 * Jeśli typ = diagram, fukcja logiczna powinna być równa null
	 * @param name - nazwa bloczka
	 * @param formula - funkcja logiczna (typ=formula)
	 */
	public void addOutput(String name, String formula){
		BlockOutputBean o = new BlockOutputBean(this, name, formula);
		outputs.add(o);
	}
	//jesli bydziesz chcial cala liste
	public List<BlockOutputBean> getOutputList(){
		return outputs;
	}
	/**
	 * Zwraca wynik funkcji boolowskiej dla wszystkich wyjść bloczku <br> Stan wyjsc zapisywany jest w wlaciwosci state wyjsc OutputBean
	 * @param output - obiekt wyjscia
	 * @param inputStates - tablica wartosci wejsc
	 * @return
	 */
	public void evaluate() throws RecurrentLoopException{
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
		
		
	}
	/**
	 * Drukuje do wyznaczonego strumienia tablice prawdy dla danego wyjścia bloczka.
	 * @param outputNode - wyjście bloczka
	 * @param outputStream - strumień w którym zostanie wydrukowana tablica prawdy
	 */
	public void printTruthTable(BlockOutputBean outputNode, PrintStream outputStream){
		int size = inputs.size();
		
		//System.out.println(x.getFormula());
		for(int j=0; j<size; j++){
			
			outputStream.print(getInput(j).getName() + "\t");
		}
		outputStream.print(outputNode.getName());
		outputStream.println();
		for(int i=0; i<Math.pow(2, size); i++){
			
			Boolean[] states = new Boolean[size];
			for(int j=0; j<size; j++){
				states[j] = ((i & (int)Math.pow(2, size-j-1)) == 0)?false:true;
				outputStream.print(states[j] + "\t");
				getInput(j).setState(states[j]);
			}
			try {
				evaluate();
			} catch (RecurrentLoopException e) {
				outputStream.println("\n" + e.getMessage());
			}
			System.out.println(outputNode.getState());
		}
	}

	@Override
	public String toString() {
		return "BlockBean ["+ name + "]";
	}
	
	
	
	
}
