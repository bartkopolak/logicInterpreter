package logicInterpreter.DiagramEditor.editor.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class MintermGroup {
	private ArrayList<Integer> minterms;
	private BitSet dashPos;
	private boolean checked;
	private int noOfInputs;
	
	public int getNoOfInputs() {
		return noOfInputs;
	}

	public MintermGroup(ArrayList<Integer> minterms, BitSet dashPos, int noOfInputs) {
		this.minterms = minterms;
		this.dashPos = dashPos;
		checked = false;
		this.noOfInputs = noOfInputs;
	}

	public ArrayList<Integer> getMinterms() {
		return minterms;
	}

	public void setMinterms(ArrayList<Integer> minterms) {
		this.minterms = minterms;
	}



	public BitSet getDashPos() {
		return dashPos;
	}

	public void setDashPos(BitSet dashPos) {
		this.dashPos = dashPos;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public MintermGroup addGroup(ArrayList<Integer> pairedMinterms, int changePos) {
		BitSet nbs = (BitSet) dashPos.clone();
		nbs.set(changePos);
		ArrayList<Integer> nmt = (ArrayList<Integer>) minterms.clone();
		nmt.addAll(pairedMinterms);
		nmt.sort(null);
		return new MintermGroup(nmt,nbs,noOfInputs);
	}
	
	public String getImplicant() {
		if(minterms.size() > 0) {
			String str = "";
			BitSet a = BitSet.valueOf(new long[] {minterms.get(0)});
			String[] t = new String[noOfInputs];
			Arrays.fill(t, "0");
			for(int i=0; i<a.length(); i++) {
				if(a.get(i)) t[i] = "1";
			}
			for(int i=0; i<dashPos.length(); i++) {
				if(dashPos.get(i)) t[i] = "-";
			}
			for(int i=0; i<t.length; i++) {
				str = t[i] + str;
			}
			return str;
		}
		return "";
		
	}
	/**
	 * Zwraca funkcję logiczną w postaci tekstowej
	 * @param inputNames - nazwy wejść
	 * @param clean - czy drukować znak mnożenia *
	 * @return
	 */
	public String printFunction(String[] inputNames, boolean clean) {
		String implicant = getImplicant();
		String out = "";
		boolean printStar = false;
		int c= 0;
		for(int i=0;i<implicant.length();i++) {
			char st = implicant.charAt(i);
			if(st == '1') out = out + inputNames[i];
			else if(st == '0') out = out + inputNames[i] + "'";
			else if(st == '-') c++;
			if(!clean) {
				if(i<implicant.length()-1) {
					if(implicant.charAt(i) != '-' && implicant.charAt(i+1) == '-' || implicant.charAt(i) != '-' && implicant.charAt(i+1) != '-') printStar = true;
					if(printStar && implicant.charAt(i+1) != '-') 
					{
					    out += "*";
						printStar = false;
					}
					
				}
			}
		}
		if(implicant.length() == c) return ("" + inputNames[0] + " + " + inputNames[0] + "'");
		return out;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "m(";
		for(int i=0; i<minterms.size(); i++) {
			str = str + minterms.get(i).toString();
			if(i<minterms.size()-1) str = str +", ";
		}
		str = str + ") - " + getImplicant();
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MintermGroup) {
			if(minterms.containsAll(((MintermGroup) obj).getMinterms())) return true;
		}
		return false;
	}
	
	
	
	
}