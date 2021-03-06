package logicInterpreter;

import java.io.File;
import java.io.FileNotFoundException;

import org.xml.sax.SAXException;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.LogicElementsModels.BlockBean;
import logicInterpreter.LogicElementsModels.CircuitSchemaBean;
import logicInterpreter.LogicElementsModels.Nodes.BlockOutputBean;
import logicInterpreter.Tools.DiagFileUtils;

/*
 * UWAGA CUDOENE GOWNOOOOO
 * http://jgrapht.org/visualizations.html
 * BIERZ TOOOOOOOOOOOOOOOOO
 */

public class Test {

	public static void main(String[] args){
		
		//test bloczka - tablica prawdy
		BlockBean blok;
		try {
			blok = DiagFileUtils.parseXMLBlock(new File("xmls/binto7sd.xml"),"");
			System.out.println(blok.getName());
			BlockOutputBean x = blok.getOutput("OA");
			blok.getInput(0).setState(new ThreeStateBoolean(false));
			blok.getInput(1).setState(new ThreeStateBoolean(false));
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			int[] a = blok.getTruthTable(x);
			for(int i = 0; i< a.length; i++) {
				System.out.print(a[i]);
			}
			
		} 
		catch(FileNotFoundException e){
			System.out.println("Nie ma takiego pliku: " + e.getMessage().substring(0));
		}
		catch(SAXException e){
			System.out.println("Błąd w pliku xml bloku: " + e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("---------------");
		//test diagramow - tablica prawdy
		/*
		DiagramBean d;
		try {
			d = XMLparse.parseXMLDiagram(new File("xmls/diagram2.xml"));
			int size2 = d.getInputList().size();
			System.out.println(d.getName());
			for(int j=0; j<size2; j++){
				
				System.out.print(d.getInput(j) + "\t");
			}
			System.out.println();
			for(int k=0; k<1; k++){
				for(int i=0; i<Math.pow(2, size2); i++){
					
					Boolean[] states = new Boolean[size2];
					for(int j=0; j<size2; j++){
						if(k==0){
							states[j] = ((i & (int)Math.pow(2, size2-j-1)) == 0)?false:true;
						}
						else{
							states[j] = true;
						}
						
						System.out.print(states[j] + "\t\t");
						d.getInput(j).setState(states[j]);
						
					}
					System.out.print(" -> ");
					d.evaluate();
					System.out.println(d.getOutput("X").getState());
				}
			}
			
			
			
			
		} catch (Exception e) {
			//
			e.printStackTrace();
		}
		*/
		
		
		
	}
}
