package logicInterpreter.DiagramInterpret;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fathzer.soft.javaluator.StaticVariableSet;

import logicInterpreter.BoolInterpret.BoolEval;
import logicInterpreter.BoolInterpret.BoolInterpreter;
import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;

public class BlockBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7589693470370349245L;
	private String name;
	private final List<BlockInputBean> inputs = new ArrayList<BlockInputBean>();
	private final List<BlockOutputBean> outputs = new ArrayList<BlockOutputBean>();
	private String type;
	private DiagramBean diagram = null; //uzywany tylko gdy typ to diagram
	private boolean defaultB = false;
	private File file = null;
	private BlockBean templateBlock = null;
	public static final String TYPE_FUNCTION = "formula";
	public static final String TYPE_DIAGRAM = "diagram";
	public BlockBean(){
		
	}
	/**
	 * Klonuje blok i okresla blok wzorca
	 * @param b
	 */
	public BlockBean(BlockBean b) {
		this.name = b.getName();
		for(BlockInputBean in : b.getInputList()) {
			inputs.add(new BlockInputBean(this, in));
		}
		for(BlockOutputBean out : b.getOutputList()) {
			outputs.add(new BlockOutputBean(this, out));
		}
		type = b.getType();
		diagram = b.getDiagram();
		defaultB = b.isDefault();
		file = b.getFile();
		templateBlock = b;
		
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
	public void addInput(String name, String position){
		BlockInputBean o = new BlockInputBean(this, name);
		if(position != null && !position.isEmpty()) o.setPosition(position);
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
			ThreeStateBoolean resultState = new ThreeStateBoolean(null);
			//pobierz nazwy wejsc w celu zdefinowania zmiennych
			String[] inputNames = new String[inputs.size()];
			for(int i=0; i<inputs.size(); i++){
				inputNames[i] = inputs.get(i).getName();
			}
			
			BoolEval eval = new BoolEval();
			//definowanie zmiennych
			StaticVariableSet<ThreeStateBoolean> variables = new StaticVariableSet<ThreeStateBoolean>();
			for(int i=0; i<inputs.size(); i++){
				BlockInputBean input = inputs.get(i);
				variables.set(input.getName(), input.getState());
			}
			//wykonaj funkcje logiczna dla kazdego wyjscia
			for(BlockOutputBean output : outputs){
				String func = BoolInterpreter.InsertMultiplyOperators(output.getFormula(), inputNames);
				ThreeStateBoolean prevCycleState = null;
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
	 * Zwraca tablice prawdy dla danego wyjścia bloczka.
	 * @param outputNode - wyjście bloczka
	 */
	public int[] getTruthTable(BlockOutputBean outputNode) throws RecurrentLoopException{
		int size = inputs.size();
		
		//System.out.println(x.getFormula());
		int pow2 = (int) Math.pow(2, size);
		int[] outputs = new int[pow2];
		for(int i=0; i<pow2; i++){
			
			ThreeStateBoolean[] states = new ThreeStateBoolean[size];
			for(int j=0; j<size; j++){
				states[j] = ((i & (int)Math.pow(2, size-j-1)) == 0)?new ThreeStateBoolean(false):new ThreeStateBoolean(true);
				getInput(j).setState(states[j]);
			}
			evaluate();
			ThreeStateBoolean output = outputNode.getState();
			if(output.equals(new ThreeStateBoolean(false))) outputs[i] = 0;
			else if(output.equals(new ThreeStateBoolean(true))) outputs[i] = 1;
		}
		return outputs;
	}
	
	public void setDefault(boolean state) {
		this.defaultB = state;
	}

	public boolean isDefault() {
		return defaultB;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public BlockBean getTemplateBlock() {
		return templateBlock;
	}

	public void setTemplateBlock(BlockBean templateBlock) {
		this.templateBlock = templateBlock;
	}

	@Override
	public String toString() {
		return  name;
	}
	
	
	
	
}
