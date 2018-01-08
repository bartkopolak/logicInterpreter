package logicInterpreter.BoolInterpret;

import com.fathzer.soft.javaluator.*;

import logicInterpreter.BoolInterpret.ThreeStateBoolean.State;

public class BoolInterpreter {
	
	
	
	
	//ABC + A'B'C -> A*B*C + A'*B'*C
	//algorytm:
	/** Zamienia zapis z bezznakowym mnożeniem na zapis z znakowym mnożeniem:<br> 
	 * ABC + A'B'C -> A*B*C + A'*B'*C<br>
	 * <br>Algorytm:<br>
	 * 1.wydziel grupy zmiennych ("ABC + A'B'C" -> {"ABC", "A'B'C"}<br>
	 * 2 kazda grupe podziel na 2 czesci, dzielac ja w miejscu wystapienia zmiennej<br>
	 * 3.jeśli występuje operator negacji ', przenieś go do 1 czesci
	 * 
	 * @param formula - wejściowa funkcja logiczna
	 * @param variables - lista zmiennych występująca w funkcji logicznej
	 */
	private static String splitOperator(String formula, String[] variables) {
		String result = "";
		String[] spl = formula.split("\\+|\\^|\\*");
		// get operators
		int index = 0;
		String[] operators = new String[spl.length - 1];
		for (int i = 0; i < spl.length - 1; i++) {
			index += spl[i].length();
			operators[i] = formula.substring(index, index + 1);
			index++;
		}
		
			
		for (int i = 0; i < spl.length; i++) {
			String s = spl[i];
			// negations first
			for (int j = 0; j < variables.length; j++) {
				String[] cos = s.split("\\b" +variables[j], 2);
				if (cos.length > 1) {
					if (cos[1].isEmpty() || cos[1].matches("[(]|[)]")) {
						cos[0] += variables[j];
					} else if (cos[1].charAt(0) == '\'') {
						cos[0] += variables[j] + "'";
						cos[1] = cos[1].substring(1);
					} else {
						cos[0] += variables[j];
					}
					if (!(cos[1].isEmpty() || cos[1].matches("[(]|[)]|[)][']"))) {
						if (!(cos[1].charAt(0) == '*')) {
							if (j < variables.length)
								s = cos[0] + "*" + cos[1];
						}
					}
				} else {
					cos[0] += variables[j];
				}
			}
			if(operators.length > 0 && i < spl.length - 1)
				result += s + operators[i];
			else
				result += s;
		}
		if(result.endsWith("\\+|\\^|\\*")) result = result.substring(0, result.length()-1);
		return result;
	}
	
	
/**
 * Zamienia zapis z bezznakowym mnożeniem na zapis z znakowym mnożeniem:<br> 
 * ABC + A'B'C -> A*B*C + A'*B'*C<br>
 * @param formula - wejściowa funkcja logiczna
 * @param variables - lista zmiennych występująca w funkcji logicznej
 * @return fukcja logiczna zapisana z ze znakowym mnożeniem
 */
	  public static String InsertMultiplyOperators(String formula, String[] variables){
			String formulaTemp = formula;
			//splitting - delete spaces
			formulaTemp = formulaTemp.replaceAll(" ", "");
			formulaTemp = splitOperator(formulaTemp,variables);
			return formulaTemp;
		}
	
	public static void testdeleteme(){
		String t = "CLK*B*L"; 
		String[] vars = {"L", "B", "CLK"};
		String t2 = InsertMultiplyOperators(t, vars);
		System.out.println(t);
		System.out.println(t2);
		 BoolEval eval = new BoolEval();
		StaticVariableSet<ThreeStateBoolean> variables = new StaticVariableSet<ThreeStateBoolean>();
		variables.set("L", new ThreeStateBoolean(false));
		variables.set("B", new ThreeStateBoolean(null));
		variables.set("CLK", new ThreeStateBoolean(false));
		ThreeStateBoolean rt = eval.evaluate(t2, variables);
		System.out.println("" + String.valueOf(rt));	
		//wynik ma byc undef
	}
	
	public static void main(String[] args){
		//JAVALUATOR!!!!!!!
		//http://javaluator.sourceforge.net/en/doc/tutorial.php?chapter=creatingSimple
		//
		//do zrobienia: przerzutniki d,t,jk
		//uklady 7447, bin->hex te dziwne,
		//multipleksery, itp
		//
		
		testdeleteme();
	}
	
	
	
}

//TODO
/*
 * W STRUKTURZE DRZEWA
 * MAJA BYC POSZCZEGOLNE BLOCZKI
 * STWORZ GRAF POLACZEN
 * JESLI W GRAFIE POLACZENIE IDZIE DO RODZICA, TO MA SIE NIE WYKONYWAC OD RAZU!
 * IDZIEMY OD KORZENIA W DOL!
 * 
 * + ogarnij z xorem - wejscie 1 oraz wyjscie xora 
 * wyjscie powinno byc 0, ale bedie 101010101010
 * jak co to co kazdy krok bedzie oscylowac
 * jesli nie bedzie zegara, to po prostu przejdz cale drzewo do konca, czyi tylko 1 przebieg.
 * jesli zegar bedzie, cos sie wykombinuje....
 */ 
