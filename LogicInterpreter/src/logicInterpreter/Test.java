package logicInterpreter;

import java.io.File;
import java.io.FileNotFoundException;

import org.xml.sax.SAXException;

import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Nodes.BlockBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Tools.XMLparse;

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
			blok = XMLparse.parseXMLBlock(new File("xmls/rsblok.xml"));
			System.out.println(blok.getName());
			BlockOutputBean x = blok.getOutput("Q");
			blok.getInput(0).setState(false);
			blok.getInput(1).setState(true);
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			blok.evaluate();
			System.out.println(blok.getOutput(0).getState());
			
		} 
		catch(FileNotFoundException e){
			System.out.println("Nie ma takiego pliku: " + e.getMessage().substring(0));
		}
		catch(SAXException e){
			System.out.println("Błąd w pliku xml bloku: " + e.getMessage());
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		
	}
}
