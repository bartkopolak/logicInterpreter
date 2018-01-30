package logicInterpreter.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logicInterpreter.DiagramEditor.editor.GraphEditor;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.GNDNode;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.VCCNode;

public class VHDLCreator {

	DiagramBean diagram;
	public VHDLCreator(DiagramBean diagram) {
		this.diagram = diagram;
	}

	private class SignalOutputPair {
		private OutputBean out;
		private int index;
		private String prefix;
		public SignalOutputPair(OutputBean out, int index, String prefix) {
			this.out = out;
			this.index = index;
			this.prefix = prefix;
		}
		public OutputBean getOut() {
			return out;
		}
		public void setOut(OutputBean out) {
			this.out = out;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public String getSignalName() {
			return prefix+"sig"+String.valueOf(index);
		}
		
	}
	
	private SignalOutputPair getSignal(ArrayList<SignalOutputPair> list, OutputBean out) {
		for(SignalOutputPair pair : list) {
			if(pair.getOut().equals(out)) return pair;
		}
		return null;
	}
	
	private String convertFunctionToVHDL(String func) {
		StringBuffer sb = new StringBuffer();
		String[] orSplit = func.split("[+]");
		for(int i=0;i<orSplit.length;i++) {
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(orSplit[i]);
		     while(m.find()) {
		       if(orSplit[i].endsWith("'")) orSplit[i] = "not("+m.group(1)+")";   
		     }
			
			String[] andSplit = orSplit[i].split("[*]");
			sb.append("(");
			for(int j=0; j< andSplit.length; j++) {
				if(andSplit[j].contains("'")) {
					 
					sb.append("not("+ andSplit[j].replace("'","").replace(" ", "")+ ")");
				}
				else sb.append(andSplit[j].replace(" ", ""));
				if(j<andSplit.length-1) sb.append(" and ");
			}
			sb.append(")");
			if(i<orSplit.length-1) sb.append(" or ");
		}
		return sb.toString();
	}
	
	private String listBlockIO(BlockBean block) {
		StringBuffer sb = new StringBuffer();
		sb.append("(\n");
		for(int i=0; i<block.getInputList().size(); i++) {
			sb.append(block.getInput(i).getVHDLName() + ": in std_logic;\n");
		}
		for(int i=0; i<block.getOutputList().size(); i++) {
			sb.append(block.getOutput(i).getVHDLName() + ": out std_logic");
			if(i<block.getOutputList().size()-1) sb.append(";");
			sb.append("\n");
		}
		sb.append(");\n");
		return sb.toString();
	}
	
	private int portMap(DiagramBean diagram, StringBuffer sb, String signalPrefix) {
		int mapCount = 0;
		int sigCount = 0;
		ArrayList<SignalOutputPair> signals = new ArrayList<SignalOutputPair>();
		List<List<BlockBean>> flowList = diagram.getFlowList();
		for(List<BlockBean> level : flowList) {
			for(BlockBean b : level) {
				sb.append("U_"+b.getVHDLName()+"_"+mapCount+": c_"+b.getTemplateBlock().getVHDLName()+" port map (");
				for(int i=0; i<b.getInputList().size(); i++) {
					OutputBean src = b.getInput(i).getFrom();
					if(src == null) sb.append("open,");
					else {
						if(src instanceof DiagramInputBean) {
							if(src instanceof VCCNode) 
								sb.append("'1',");
							else if(src instanceof GNDNode) 
								sb.append("'0',");
							else
								sb.append(src.getVHDLName()+",");
						}
							
						else if(src instanceof BlockOutputBean) {
							SignalOutputPair signal = getSignal(signals,src);
							if(signal == null) {
								signal = new SignalOutputPair(src, sigCount, signalPrefix);
								signals.add(signal);
								sigCount++;
							}
							sb.append(signal.getSignalName()+",");
						}
					}
				}
				for(int i=0;i<b.getOutputList().size();i++) {
					OutputBean out = b.getOutput(i);
					SignalOutputPair signal = getSignal(signals,out);
					if(signal == null) {
						signal = new SignalOutputPair(out, sigCount, signalPrefix);
						signals.add(signal);
						sigCount++;
					}
					sb.append(signal.getSignalName());
					if(i<b.getOutputList().size()-1)sb.append(",");
				}
				sb.append(");\n");
				mapCount++;
			}
		}
		for(DiagramOutputBean diagOut : diagram.getOutputList()) {
			OutputBean src = diagOut.getFrom();
			if(src instanceof VCCNode) {
				sb.append(diagOut.getVHDLName() + " <= '1';\n");
			}
			else if(src instanceof GNDNode) {
				sb.append(diagOut.getVHDLName() + " <= '0';\n");
			}
			else if(src instanceof DiagramInputBean) {
				
				sb.append(diagOut.getVHDLName() + " <= " + src.getVHDLName() + ";\n");
			}
			else if(src instanceof BlockOutputBean) {
				SignalOutputPair signal = getSignal(signals,src);
			if(signal != null) {
				sb.append(diagOut.getVHDLName() + " <= " + signal.getSignalName() + ";\n");
			}
			}
			
		}
		
		
		return sigCount;
	}
	
	private String createDiagramArchitecture(DiagramBean diagram, String signalPrefix) {
		StringBuffer sb = new StringBuffer();
		ArrayList<BlockBean> templBlocks = diagram.getAllTemplateBlocks(null, false);
		for(int i=0;i<templBlocks.size(); i++) {
			BlockBean templBlock = templBlocks.get(i);
			sb.append("component c_" + templBlock.getVHDLName() + " is port ");
			sb.append(listBlockIO(templBlock));
			sb.append("end component;\n");
		}
		
		StringBuffer mapSB = new StringBuffer();
		diagram.createFlowList();
		int sigCount = portMap(diagram, mapSB, signalPrefix);
		if(sigCount > 0) {
			sb.append("signal ");
			for(int i=0; i<sigCount; i++) {
				sb.append(signalPrefix +"sig"+String.valueOf(i));
				if(i<sigCount-1)sb.append(", ");
			}
			
			sb.append(": std_logic;\nbegin\n");
		}
		sb.append(mapSB.toString());
		return sb.toString();
	}
	public String createEntity(BlockBean block) {
		StringBuffer sb = new StringBuffer();
		sb.append("LIBRARY ieee;\n");
		sb.append("USE ieee.std_logic_1164.all;\n");
		sb.append("entity c_" + block.getVHDLName() + " is\n");
		sb.append("port\n");
		sb.append(listBlockIO(block));
		sb.append("end entity;\narchitecture a_"+block.getVHDLName()+ " of c_" +block.getVHDLName()+ " is\n");
		
		if(block.getType().equals(BlockBean.TYPE_FUNCTION)) {
			sb.append("begin\n");
			for(int i=0;i<block.getOutputList().size();i++) {
				BlockOutputBean out = block.getOutput(i);
				sb.append(out.getVHDLName() + " <= " + convertFunctionToVHDL(out.getFormula()) +";\n");
			}
		}
		else if(block.getType().equals(BlockBean.TYPE_DIAGRAM)) {
			
			if(block.getDiagram() != null) {
				DiagramBean diagram = block.getDiagram();
				String signalPrefix = "c_" + block.getVHDLName() +"_";
				sb.append(createDiagramArchitecture(diagram, signalPrefix));
				
			}
		}
		sb.append("end a_"+block.getVHDLName()+";\n----------\n");
		return sb.toString();
	}
	
	public String createVHDL() {
		ArrayList<BlockBean> list = diagram.getAllTemplateBlocks(null, true);
		StringBuffer sb = new StringBuffer();
		sb.append("LIBRARY ieee;\n");
		sb.append("USE ieee.std_logic_1164.all;\n");
		sb.append("entity main_ent is\n");
		sb.append("port\n");
		sb.append("(\n");
		for(int i=0; i<diagram.getInputList().size(); i++) {
			DiagramInputBean diagInput = diagram.getInput(i);
			if(!(diagInput instanceof VCCNode) && !(diagInput instanceof GNDNode))
				sb.append(diagInput.getVHDLName() + ": in std_logic;\n");
		}
		for(int i=0; i<diagram.getOutputList().size(); i++) {
			sb.append(diagram.getOutput(i).getVHDLName() + ": out std_logic");
			if(i<diagram.getOutputList().size()-1) sb.append(";");
			sb.append("\n");
		}
		sb.append(");\n");
		sb.append("end entity;\narchitecture a_main_ent of main_ent is\n");
		sb.append(createDiagramArchitecture(diagram, "m"));
		sb.append("end a_main_ent;\n----------\n");
		
		for(BlockBean b : list) {
			sb.append(createEntity(b));
		}
		return sb.toString();
		
	}
	
	
	
}
