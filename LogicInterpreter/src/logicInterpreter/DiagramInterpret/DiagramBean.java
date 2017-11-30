package logicInterpreter.DiagramInterpret;

import java.util.ArrayList;
import java.util.List;

import logicInterpreter.Nodes.BlockBean;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;

public class DiagramBean {
	
	private String name;
	private final List<DiagramInputBean> inputs = new ArrayList<DiagramInputBean>();
	private final List<DiagramOutputBean> outputs = new ArrayList<DiagramOutputBean>();
	private final List<BlockBean> blocks = new ArrayList<BlockBean>();
	private final List<List<BlockBean>> flowList = new ArrayList<List<BlockBean>>();
	
	public DiagramBean(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	//lista wejść diagramu
	public DiagramInputBean getInput(int index){
		return inputs.get(index);
	}
	public DiagramInputBean getInput(String name){
		for(int i=0; i<inputs.size(); i++){
			DiagramInputBean input = inputs.get(i);
			if(input.getName().equals(name)){
				return input;
			}
		}
		return null;
	}
	public void addInput(DiagramInputBean input){
		inputs.add(input);
	}
	//dla leniwych cala lista za fryko
	public List<DiagramInputBean> getInputList(){
		return inputs;
	}
	
	//lista wyjść diagramu
	public DiagramOutputBean getOutput(int index){
		return outputs.get(index);
	}
	public DiagramOutputBean getOutput(String name){
		for(int i=0; i<outputs.size(); i++){
			DiagramOutputBean out = outputs.get(i);
			if(out.getName().equals(name)){
				return out;
			}
		}
		return null;
	}
	public void addOutput(DiagramOutputBean output){
		outputs.add(output);
	}
	public List<DiagramOutputBean> getOutputList(){
		return outputs;
	}
	
	
	public BlockBean getBlock(int index){
		return blocks.get(index);
	}
	public BlockBean getBlock(String name){
		for(int i=0; i<blocks.size(); i++){
			BlockBean b = blocks.get(i);
			if(b.getName().equals(name)){
				return b;
			}
		}
		return null;
	}
	public void addBlock(BlockBean b){
		blocks.add(b);
	}
	//jesli bydziesz chcial cala liste
	public List<BlockBean> getBlocksList(){
		return blocks;
	}
	
	
	/**
	 * Tworzy liste przeplywu bloczkow. Wymagane jest to aby zależne bloczki od poprzednich do wejść otrzymały właściwą wartość logiczną <br>
	 * Zapobiega to także zapętleniu się sygnału, dzzięki tej funkcji evaluate() wykonuje tylko 1 cykl w układzie logicznym.<br>
	 * Lista podzielona jest na poziomy. Poziomy oznaczają kolejność wykonywań fukcji logicznej bloczków (0- jako pierwsza, są to wejścia układu, 1 - bloczki połączone bezpośrednio z wejściami, 2-bloczki połączone z bloczkami z poprzedniego poziomu)
	 * Ostatni element listy jest zawsze pusty.
	 * 
	 * *algorytm ustalenia przepływu
	 *
	 * poziom 0 - wejścia układu - tutaj sprawdzasz czy nie zaszła jakas zmiana wejść<br>
	 * poziom 1 - bloki które są połączone bezpośrednio z wejściami układu<br>
	 *		->tworzone poprzez analize polaczen wychodzących z inputów diagramu, czyli:<br>
	 *<ul>
     *			 <li>pobierz nazwy wejsc polaczen (z wartości 'to') </li>
     *			 <li> jesli polaczono z bloczkiem, to pobierz rodzica i dodaj go do listy</li>
	 *			 <li>jesli polaczono z wyjsciem diagramu, nic nie dodawaj<br>
	 *</ul>
	 * poziomy 2 i dalej - bloki połączone z blokami poprzedniego poziomu<br>
	 *	-> tworzenie:
	 *<ul>
	 *
	 * <li>pobierz bloki z poprzedniego poziomu</li>
	 *	              <li>jeśli brak bloków z poprzedniego poziomu, to kończymy tworzenie flowcharta :P, bo to znaczy ze doszlismy do wszystkich polaczonych outputów.</li>
	 *			  <li>dla kazdego bloku z poprz poziomu:
	 *				<ul>
	 *		      <li>pobierz połączenia bloku</li>
	 *		      <li>pobierz wyjscia (InputBean) polaczenia z ToList </li>
	 *		      <li>jeśli polaczono z bloczkiem</li>
	 *			  <li>jeśli nazwa bloczka istnieje we wczesniejszych poziomach, nie dodawaj go do nastepnych poziomow</li>
	 *		      <li>wpisz obiekt do listy</li>
	 *			 </ul>
	 *			</li>
	 *
	 * </ul>
	 * 
	 */

	public void createFlowList(){
		boolean stop = false;
		//poziomu 0 nie tworze, bo wiadomo, ze są pierwsze, i ze wszystkie wejscia są juz ustalone
		//poziom 1 tworza bloczki, ktore są połączone wyłącznie z wejściami układu lub z samym sobą
		List<BlockBean> firstLevel = new ArrayList<BlockBean>();
		for(DiagramInputBean input : inputs){	//dla kazdego wejscia ukladu
			List<InputBean> linkedInputList = input.getWire().getToList(); //pobierz polaczenia wychodzace z wejscia ukladu
			for(InputBean linkedInput : linkedInputList){	//dla kazdego polaczenia
				if(linkedInput instanceof BlockInputBean){	//jesli polaczono z wejsciem bloczka (polaczenia bezposrednio z wyjsciem ukladu konczy przeplyw w danej sciezce do wyjscia ofc, tak jakby koniec gałęzi, wiec nie dodaje do listy)
					BlockInputBean inputB = ((BlockInputBean) linkedInput);	
					BlockBean parent = inputB.getParent(); //pobierz bloczek
					List<BlockInputBean> allInputs = parent.getInputList(); //pobierz wszystkie wejscia bloczka
					boolean isFirstLevel = true;
					for(BlockInputBean bi : allInputs){
						if(!(bi.getFrom() instanceof DiagramInputBean || (bi.getFrom() instanceof BlockOutputBean && ((BlockOutputBean) bi.getFrom()).getParent().equals(parent)))){ //jesli wejscie nie jest polaczone z wejsciem diagramu lub wejscie nie jest polaczone z wyjsciem tego samego bloczka
							isFirstLevel = false; //bloczek nie jest w 1. poziomie
						}
					}
					if(isFirstLevel){
						if(firstLevel.indexOf(parent) == -1){	//jesli bloczek nie istnieje w liscie, dodaj go
							firstLevel.add(parent);
						}
					}
					
				}
			}
			
		}
		flowList.add(firstLevel);
		//poziomy >2 - tworza juz wszystkie bloczki, polaczone z blokami z poprzedniego poziomu
		int currLevel = 1; //zaczynajac od zera ofc, wiec 1 = 2 
		
		while(!stop){
				if(flowList.get(currLevel-1).isEmpty()){
					stop = true;
				}
				else{
					List<BlockBean> prevLevel = flowList.get(currLevel-1);
					List<BlockBean> thisLevel = new ArrayList<BlockBean>();
					for(BlockBean b : prevLevel){	//dla kazdego bloczka z poprz poziomu
						List<BlockOutputBean> outputList = b.getOutputList();
						for(BlockOutputBean output : outputList){	//dla kazdego wyjscia bloczka z poprz poziomu
							List<InputBean> linkedInputList = output.getWire().getToList();
							for(InputBean linkedInput : linkedInputList){	//sprawdz kazde wejscie polaczone z wyjsciem bloczka
								if(linkedInput instanceof BlockInputBean){
									BlockInputBean inputB = ((BlockInputBean) linkedInput);	
									BlockBean parent = inputB.getParent(); //pobierz bloczek
									//sprawdz, czy w jakimkolwiek poprzednim poziomie bloczek sie pojawil
									boolean existsInPrevLevel = false;
									for(int x=0; x<flowList.size(); x++){
										if(flowList.get(x).indexOf(parent) != -1)
											existsInPrevLevel = true;
									}
									if(thisLevel.indexOf(parent) == -1 && !existsInPrevLevel){	//jesli bloczek nie istnieje w liscie, dodaj go
										thisLevel.add(parent);
									}
								}
							}
						}
					}
					flowList.add(thisLevel);
					currLevel++;
				}
		}
	}
	/* 
	 * Wysyła sygnał z wejść układu poprzez wszystkie bloczki do wyjść układu.
	 * 
	 */
	public void evaluate(){
		if(flowList.isEmpty()) createFlowList();
		//ostatni element flowListy jest zawsze pusty - oznacza to kuniec listy i to, ze jesli flowlist zawiera tylko pusty element, to znaczy ze flowlist zostal wygenerowany i nie musi byc juz tworzony raz jezcze
		if(flowList.size() > 1){//jesli lista jest pusta, oznacza to polaczenie bezposrednie z wejsc ukladu do wyjsc ukladu.
			List<BlockBean> firstLevelBlocks = flowList.get(0);
			for(BlockBean b : firstLevelBlocks){
				//ustaw wejscia bloczkow 1. poziomu - wartosci podlaczonego do wejsc bloczka wejscia ukladu
				List<BlockInputBean> blockInputs = b.getInputList(); 
				for(BlockInputBean input : blockInputs){
					input.setState(input.getFrom().getState());
				}
				//wykonaj funkcje
				b.evaluate();
				
			}
			for(int i = 1; i<flowList.size(); i++){
				List<BlockBean> nextLevelBlocks = flowList.get(i);
				for(BlockBean b : nextLevelBlocks){
					//ustaw wejscia bloczkow 1. poziomu - wartosci podlaczonego do wejsc bloczka wejscia ukladu
					List<BlockInputBean> blockInputs = b.getInputList();
					for(BlockInputBean input : blockInputs){
						input.setState(input.getFrom().getState());
					}
					//wykonaj funkcje
					b.evaluate();
				}
			}

		}

		for(DiagramOutputBean output : outputs){
			output.setState(output.getFrom().getState());
		}
	
	}
	
	
	
}
