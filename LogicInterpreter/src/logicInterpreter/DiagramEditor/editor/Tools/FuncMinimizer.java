package logicInterpreter.DiagramEditor.editor.Tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class FuncMinimizer {
	
	/**
	 * Porownaj termy rozniacych sie 1 bitem i ew. wskaz miejsce zmiany 
	 * @param a
	 * @param b
	 * @return nr bitu, na ktorym zaszla zmiana, -1 jeśli brak takiej zmiany
	 */
	private static int pairMinterms(MintermGroup a, MintermGroup b) {
		int pos = -1;
		int count = 0;
		if(a.getNoOfInputs() != b.getNoOfInputs()) return -1;
		String ai = a.getImplicant();
		String bi = b.getImplicant();
		int len = a.getNoOfInputs();
		for(int i=0; i< len; i++) {
			if(ai.charAt(i) != bi.charAt(i)) {
				pos = len-i-1;
				count++;
			}
		}
		if(count == 1) return pos;
		else return -1;
	}
	/**
	 * Funkcja ta minimalizuje funkcję boolowską z postaci tablicy prawdy do zminimalizowanej funkcji<br>
	 * Do minimalizacji wykorzystano metodę Quine'a-McCluskeya.<br>
	 * W tablicy wyjść funkcji wejściowej wartości mogą być następujące:
	 * <ul>
	 * <li>0 - logiczne 0</li>
	 * <li>1 - logiczne 1</li>
	 * <li>2 - wartość dowolna (don't care)</li></ul>
	 * @param outputs - lista wyjść tabeli prawdy
	 * @param inputNames - nazwy wejść
	 * @param clean - gdy true, wynik nie będzie zawierał znaków mnożenia *
	 * @return łańcuch znakowy zawierający zminimalizowaną funkcję boolowską
	 */
	public static String minimize(int[] outputs, String[] inputNames, boolean clean) {
		int noOfInputs = (int)(Math.log(outputs.length) / Math.log(2));
		int[] terms;
		int[] termsSig;
		{
			ArrayList<Integer> termAList = new ArrayList<Integer>();
			ArrayList<Integer> termSigAList = new ArrayList<Integer>();
			for(int i = 0; i<outputs.length; i++) {
				if(outputs[i] > 0) {
					termAList.add(i);
				}
				if(outputs[i] == 1) {
					termSigAList.add(i);
				}
			}
			if(termSigAList.isEmpty()) return inputNames[0] + "*" + inputNames[0] + "'";
			
			//lista termów z wydzielona lista termow oznaczonych wartoscia dont care
			terms = new int[termAList.size()];	
			termsSig = new int[termSigAList.size()];
			
			for(int i = 0; i< terms.length; i++) {
				terms[i] = termAList.get(i);
			}
			for(int i = 0; i<termsSig.length; i++) {
				termsSig[i] = termSigAList.get(i);
			}
		}
		
		//1. Form  a  table  of  functions  of  minterms  according  to  the  number  of  1‟s  in  each minterm
		ArrayList<ArrayList<MintermGroup>> implicantsStart = new ArrayList<ArrayList<MintermGroup>>();
		for(int i = 0; i<noOfInputs+1; i++) {
			ArrayList<MintermGroup> list = new ArrayList<MintermGroup>();
			for(int k = 0; k< terms.length; k++) {
				BitSet a = BitSet.valueOf(new long[] {terms[k]});
				if(a.cardinality() == i) {
					ArrayList<Integer> t = new ArrayList<Integer>();
					t.add(terms[k]); 
					BitSet bs = new BitSet(); //same zera
					MintermGroup mt = new MintermGroup(t,bs,noOfInputs);
					list.add(mt);
				}
					
			}
			implicantsStart.add(list);
		}
		
		
		
		ArrayList<MintermGroup> implicantsFinal = new ArrayList<MintermGroup>();
		//2.Start pairing off each element of first group with the next
		boolean noMoreComparsions = false;
		ArrayList<ArrayList<MintermGroup>> implicants = null;
		do {
			ArrayList<ArrayList<MintermGroup>> pairedImplicants = new ArrayList<ArrayList<MintermGroup>>();
			if(implicants == null) implicants = implicantsStart;
			int noOfPairedImpl = 0;
			for(int i=0; i<implicants.size();i++) {
				ArrayList<MintermGroup> im = implicants.get(i);
				ArrayList<MintermGroup> imNext;
				if(i == implicants.size()-1) imNext = new ArrayList<MintermGroup>();
				else imNext = implicants.get(i+1);
				ArrayList<MintermGroup> paired = new ArrayList<MintermGroup>();
				
				for(int j=0;j<im.size();j++) {
					int count = 0;
					for(int k=0;k<imNext.size();k++) {
						int chPos = pairMinterms(im.get(j), imNext.get(k));
						if(chPos > -1) {
							MintermGroup m = im.get(j).addGroup(imNext.get(k).getMinterms(), chPos);
							im.get(j).setChecked(true);
							imNext.get(k).setChecked(true);
							if(!paired.contains(m)) paired.add(m);
							count++;
						}
					}
					if(count > 0) noOfPairedImpl++;
					if(!im.get(j).isChecked()) implicantsFinal.add(im.get(j));
				}
				pairedImplicants.add(paired);
				
			}
			if(noOfPairedImpl == 0) noMoreComparsions = true;
			implicants = pairedImplicants;
			
		}while(!noMoreComparsions);
		
		int implCount = implicantsFinal.size();
		int termsCount = termsSig.length;
		int[][] primeImplicantChart = new int[implCount][termsCount];
		
		//wypelnienie tablicy implikantow
		for(int i=0;i<implCount;i++) {
			MintermGroup impl = implicantsFinal.get(i);
			for(int j=0;j<termsCount;j++) {
				if(impl.getMinterms().contains(termsSig[j])) primeImplicantChart[i][j] = 1;
			}
		}
		//znajdz essential implikanty
		ArrayList<MintermGroup> implicantsEssential = new ArrayList<MintermGroup>();
		for(int i=0;i<termsCount;i++) {
			int count = 0;
			int pos = 0;
			for(int j=0;j<implCount;j++) {
				if(count > 1) break;
				if(primeImplicantChart[j][i] > 0) {
					count++;
					pos = j;
				}
			}
			if(count == 1) {
				if(!implicantsEssential.contains(implicantsFinal.get(pos)))
					implicantsEssential.add(implicantsFinal.get(pos));
			}
		}
		
		//teraz wyznacz ktore termy nie zostaly pokryte przez implikanty esencjalne
		ArrayList<Integer> termsNotCoveredByEssentials = new ArrayList<Integer>();
		 termsNotCoveredByEssentials.addAll(Arrays.asList(Arrays.stream( termsSig ).boxed().toArray( Integer[]::new )));
		for(MintermGroup m : implicantsEssential) {
			termsNotCoveredByEssentials.removeAll(m.getMinterms());
		}
		ArrayList<MintermGroup> implicantsNotEssential = implicantsFinal;
		implicantsNotEssential.removeAll(implicantsEssential);
		while(!termsNotCoveredByEssentials.isEmpty()) {
			int maxTermsCovered = 0;
			int pos = 0;
			ArrayList<Integer> termsToRemove = new ArrayList<Integer>();
			for(int i=0;i<implicantsNotEssential.size(); i++) {
				MintermGroup m = implicantsNotEssential.get(i);
				ArrayList<Integer> termsToRemoveTemp = new ArrayList<Integer>();
				int termsCovered = 0;
				for(int j=0;j<termsNotCoveredByEssentials.size();j++) {
					if(m.getMinterms().contains(termsNotCoveredByEssentials.get(j))) {
						termsCovered++;
						termsToRemoveTemp.add(termsNotCoveredByEssentials.get(j));
					}
				}
				if(termsCovered > maxTermsCovered) {
					maxTermsCovered = termsCovered;
					pos = i;
					termsToRemove = termsToRemoveTemp;
				}
			}
			implicantsEssential.add(implicantsNotEssential.get(pos));
			implicantsNotEssential.remove(implicantsNotEssential.get(pos));
			termsNotCoveredByEssentials.removeAll(termsToRemove);
			
		}
		
		String output = "";
		
		for(int i=0; i<implicantsEssential.size();i++) {
			MintermGroup m = implicantsEssential.get(i);
			output += m.printFunction(inputNames, clean);
			if(i<implicantsEssential.size()-1) output += " + ";
		}
		return output;
	}
	
	public static void main(String args[]) {
		String[] inputNames = {"A", "B", "C", "D"};
		int[] test = {1,0,2,1};
		System.out.println(FuncMinimizer.minimize(test, inputNames, true));
		
	}
}
