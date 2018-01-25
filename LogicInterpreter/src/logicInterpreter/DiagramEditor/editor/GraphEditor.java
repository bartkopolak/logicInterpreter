package logicInterpreter.DiagramEditor.editor;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.BasicGraphEditor;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPopupMenu;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.GNDNode;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.VCCNode;
import logicInterpreter.Nodes.Wire;
import logicInterpreter.Tools.DiagFileUtils;

public class GraphEditor extends BasicGraphEditor {

	class PathPaletteGroup{
		private EditorPalette pal;
		private String path;
		PathPaletteGroup(EditorPalette a, String b){
			path = b;
			pal = a;
		}
		public EditorPalette getPalette() {
			return pal;
		}
		public String getPath() {
			return path;
		}
		
	}
	
	public static int checkCellBusy(mxCell trgCell,ArrayList<mxCell> visitedEdges) {
		int count = 0;
		for(int i=0; i<trgCell.getEdgeCount(); i++) {
			
			mxCell trgEdge = (mxCell) trgCell.getEdgeAt(i);	//pobierz polaczenie pomiedzy pinami
			if(visitedEdges.contains(trgEdge)) continue;
			mxCell trgEdgeTarget = (trgEdge.getSource().equals(trgCell)) ? 	//pobierz cel polaczenia
					(mxCell)trgEdge.getTarget() : (mxCell)trgEdge.getSource();
			if(trgEdgeTarget != null) {
				if(trgEdgeTarget.getValue() instanceof OutputBean || trgEdgeTarget.getValue() instanceof DiagramInputBean) {	//jesli polaczono z wyjsciem, wejscie zajęte
					count++;
				}
				if(trgEdgeTarget.getValue() instanceof InputBean) {     //jesli polaczono z wejsciem, sprawdz, czy owe wejscie nie ma polaczenia z wyjsciem
					visitedEdges.add(trgEdge);
					count += checkCellBusy(trgEdgeTarget, visitedEdges);
				}
			}
		}
		return count;	//jesli nie znaleziono zadnego polaczenia z pinem wyjscia oraz przeszukano wszystkie połączenia wejscie-wejscie, to dany pin nie jest zajety
	}
	
	ArrayList<PathPaletteGroup> palettes = new ArrayList<PathPaletteGroup>();
	
	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	FontMetrics fontMetr;
	
	public void addBlockToPalette(BlockBean templateBlock, PathPaletteGroup palette) {
		
		BlockBean block = new BlockBean(templateBlock);
		block.setTemplateBlock(templateBlock);
		int inputsNo =  block.getInputList().size();
		int outputsNo = block.getOutputList().size();
		int ncount=0, wcount=0,scount=0;
		for(int i=0; i<inputsNo; i++) {
			BlockInputBean inputNode = block.getInput(i);
			String pos = inputNode.getPosition();
			if(pos.equals("west") || pos.equals("") || pos == null) wcount++;
			else if(pos.equals("north")) ncount++;
			else if(pos.equals("south")) scount++;
		}
		int maxNSpins = Math.max(ncount, scount);
		if(maxNSpins == 0) maxNSpins++;
		int height = 20 + 20 *wcount;
		
		int width = 20*(maxNSpins) + fontMetr.stringWidth(block.toString());
		mxGeometry geo = new mxGeometry(0,0,width,height);
		mxCell cell = new mxCell(block, geo, "");
		cell.setConnectable(false);
		cell.setVertex(true);
		
		int nindex=0, windex=0,sindex=0;
		
		for(int i=0; i<inputsNo; i++) {
			BlockInputBean inputNode = block.getInput(i);
			String posAttr = inputNode.getPosition();
			double pos;
			mxGeometry geo1 = null;
			if(posAttr.equals("west") || posAttr.equals("") || posAttr == null) {

				pos = (double)(windex+1)/(wcount+1.0);
				windex++;
				geo1 = new mxGeometry(0, pos, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
			}
			else if(posAttr.equals("north")) {
				pos = (double)(nindex+1)/(ncount+1.0);
				nindex++;
				geo1 = new mxGeometry(pos, 0, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_DIAMETER));
			}
			else if(posAttr.equals("south")) {
				pos = (double)(sindex+1)/(scount+1.0);
				sindex++;
				geo1 = new mxGeometry(pos, 1, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_RADIUS, 0));
			}
			
			geo1.setRelative(true);
			mxCell port = new mxCell(inputNode, geo1, "portConstraint=west;deletable=0;labelPosition=left;labelWidth=20;labelPadding=10");
			port.setVertex(true);
			port.setAttribute("conntype", "input");
			cell.insert(port);
		}
		for(int i=0;i<outputsNo; i++) {
			mxGeometry geo1 = new mxGeometry(1, (double)(i+1)/(outputsNo+1.0), PORT_DIAMETER,
					PORT_DIAMETER);

			geo1.setOffset(new mxPoint(0, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(block.getOutput(i), geo1, "portConstraint=east;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10");
			port.setVertex(true);
			
			cell.insert(port);
		}
	    palette.getPalette().addTemplate(block.getName(),null,cell);
	}
	
	public void fillPalette(PathPaletteGroup palette) {
		palette.getPalette().removeAll();
		File xmlFolder = new File(palette.getPath());
		if(xmlFolder.exists() && xmlFolder.isDirectory()) {
			File[] list = xmlFolder.listFiles();
			for(int i=0; i<list.length; i++) {
				try {
				if(!list[i].isFile() && !list[i].getAbsolutePath().endsWith(".tmpb")) continue;
				
					BlockBean block;
						block = DiagFileUtils.readTemplateBlockFile(list[i].getAbsolutePath());
						addBlockToPalette(block, palette);
					
					
				
			}
				catch (Exception e) {}
			}
		}
	}
	
	private class StringInt {
		String str;
		int i;
		
		public StringInt(String str) {
			this.str = str;
			this.i = 0;
		}
		public synchronized int getInt() {
			i += 1;
			return i;
		}
		public boolean equals(String str) {
			return this.str.equals(str);
		}
		
	}

	public int checkList(ArrayList<StringInt> list, String name) {
		int count = 0;
		for(int j=0; j<list.size(); j++) {
			StringInt si = list.get(j);
			if (si.equals(name)) {
				return si.getInt();
			}
			count++;
		}
		if(list.size() == 0 || count == list.size()) list.add(new StringInt(name));
		return -1;
	}
	
	private ArrayList<mxCell> allOutputCells = new ArrayList<mxCell>();
	

	public ArrayList<mxCell> getAllOutputCells() {
		return allOutputCells;
	}
	/**
	 * Tablica zawierająca połączenia pomiedzy wejściem a wejściem
	 * , wykorzystywana do kolorowania połaczenia w debuggerze
	 */
	private ArrayList<mxCell> inputsLinkedWithInputEdges = new ArrayList<mxCell>();
	

	public ArrayList<mxCell> getinputsLinkedWithInputEdges() {
		return inputsLinkedWithInputEdges;
	}

	public DiagramBean createDiagram() throws MultipleOutputsInInputException {
		
		mxGraph graph = getGraphComponent().getGraph();
		DiagramBean diagram = new DiagramBean();
		ArrayList<Object> cellsList;
		{
		Collection<Object> cellsCol = mxGraphModel.filterDescendants(graph.getModel(),
				new mxGraphModel.Filter()
				{
					public boolean filter(Object cell)
					{
						return graph.getView().getState(cell) != null
								&& (graph.getModel().isVertex(cell));
					}
				});

		cellsList = new ArrayList<Object>(cellsCol);
		}
		ArrayList<mxCell> allOutputCells = new ArrayList<mxCell>();
		ArrayList<DiagramInputBean> diagInputs = new ArrayList<DiagramInputBean>();
		ArrayList<DiagramOutputBean> diagOutputs = new ArrayList<DiagramOutputBean>();
		ArrayList<BlockOutputBean> blOutputs = new ArrayList<BlockOutputBean>();
		ArrayList<BlockBean> blockList = new ArrayList<BlockBean>();
		
		
		
		
		for(int i=0; i<cellsList.size();i++) {
			Object value = ((mxCell)(cellsList.get(i))).getValue();
			if (value instanceof BlockOutputBean) {
				blOutputs.add((BlockOutputBean) value);
				allOutputCells.add((mxCell) cellsList.get(i));
				
			}
			else if(value instanceof DiagramInputBean) {
				if(!(((mxCell) cellsList.get(i)).getChildCount() == 0)) {
					diagInputs.add((DiagramInputBean) value);
					mxCell pin = (mxCell) ((mxCell) cellsList.get(i)).getChildAt(0);
					pin.setValue((DiagramInputBean) value);
					allOutputCells.add(pin);
				}
				
			}
			else if (value instanceof DiagramOutputBean) {
				if(!(((mxCell) cellsList.get(i)).getChildCount() == 0)) {
					diagOutputs.add((DiagramOutputBean) value);
					mxCell pin = (mxCell) ((mxCell) cellsList.get(i)).getChildAt(0);
					pin.setValue((DiagramOutputBean) value);
				}
			}
			else if (value instanceof BlockBean) blockList.add((BlockBean) value);
		}

		//rename blocks
		{
			ArrayList<StringInt>blockNames = new ArrayList<StringInt>();
			for(int i=0; i< blockList.size(); i++) {
				BlockBean b = blockList.get(i);
				int n = checkList(blockNames, b.getName());
				if(n >= 0) b.setName(b.getName() + "_" + n);
				b.setName(b.getName().replaceAll("[.]", ""));
			}
		}
		{
			ArrayList<StringInt> diagInputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagInputs.size(); i++) {
				DiagramInputBean b = diagInputs.get(i);
				int n = checkList(diagInputsNames,b.getName());
				if(n >= 0) b.setName(b.getName() + "_" + n);
				b.setName(b.getName().replaceAll("[.]", ""));
			}
		}
		{
			ArrayList<StringInt> diagOutputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagOutputs.size(); i++) {
				DiagramOutputBean b = diagOutputs.get(i);
				int n = checkList(diagOutputsNames,b.getName());
				if(n >= 0) b.setName(b.getName() + "_" + n);
				b.setName(b.getName().replaceAll("[.]", ""));
			}	
		}
		
		inputsLinkedWithInputEdges.clear();
		//link outputs with inputs
		for(int i=0; i<allOutputCells.size(); i++) {
			mxCell outcell = allOutputCells.get(i);
			OutputBean outputNode = (OutputBean) outcell.getValue();
			
			for(int j=0; j<outcell.getEdgeCount(); j++) {
				mxCell edge = (mxCell) outcell.getEdgeAt(j);
				if(edge != null) {
					mxCell targetCell = (mxCell) edge.getTarget();
					Object value = targetCell.getValue();
					if(value != null) {
						if(!(value instanceof InputBean)){
							targetCell = (mxCell) edge.getSource();
							value = targetCell.getValue();
						}
						if(value instanceof InputBean){
							InputBean target = (InputBean) value;
							if(checkCellBusy(targetCell, new ArrayList<mxCell>()) > 1) throw new MultipleOutputsInInputException(".", target.toString());
							outputNode.addLink(target);
							//stwórz połączenie do wejść, które poł;aćzone są z innymi wejściami
							//(gdy wejście B połączone jest z wejściem A, a A połączone jest z wyjściem X to wejście B ma stan wyjścia X)
							for(int k=0; k<targetCell.getEdgeCount(); k++) {
								mxCell trgInputEdge = (mxCell) targetCell.getEdgeAt(k);
								mxCell trgInputEdgeTarget = (trgInputEdge.getSource().equals(trgInputEdge)) 
										? (mxCell) trgInputEdge.getTarget() : (mxCell) trgInputEdge.getSource();
								Object tietVal = trgInputEdgeTarget.getValue();
								if(!trgInputEdgeTarget.equals(outcell) && tietVal instanceof InputBean) {
									InputBean tietNode = (InputBean) tietVal;
									inputsLinkedWithInputEdges.add(trgInputEdge);
									outputNode.addLink(tietNode);
								}
							}
						}
						
					}
				}	  
			}
			
		}
		
		diagram.getBlocksList().addAll(blockList);
		diagram.getInputList().addAll(diagInputs);
		diagram.getOutputList().addAll(diagOutputs);
		
		this.allOutputCells.clear();
		this.allOutputCells.addAll(allOutputCells);
		
		return diagram;
		
	}
	
	public ArrayList<mxCell> getCellsOfType(Class<?> cl){
		mxGraph graph = getGraphComponent().getGraph();
		ArrayList<mxCell> cellsList = new ArrayList<mxCell>();
		
		Collection<Object> cellsCol = mxGraphModel.filterDescendants(graph.getModel(),
				new mxGraphModel.Filter()
				{
					public boolean filter(Object cell)
					{
						return graph.getView().getState(cell) != null
								&& (graph.getModel().isVertex(cell));
					}
				});
		for(Object o : cellsCol) {
			if(o instanceof mxCell) {
				mxCell cell = (mxCell) o;
				if(cl.isInstance(cell.getValue())) {
					cellsList.add(cell);
				}
			}
		}
		return cellsList;
	}
	
	public void ioPalette(EditorPalette p) {
		//wejscie diagramu
		{
			DiagramInputBean input = new DiagramInputBean();
			input.setName("input");
			mxGeometry geo = new mxGeometry(0,0,50,20);
			mxCell cell = new mxCell(input, geo, "");
			cell.setConnectable(false);
			cell.setVertex(true);
			mxGeometry geo1 = new mxGeometry(1, 1/2.0, PORT_DIAMETER,
					PORT_DIAMETER);
	
			geo1.setOffset(new mxPoint(0, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(input, geo1, "portConstraint=east;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
			port.setVertex(true);
			cell.insert(port);
			p.addTemplate("Wejście", null, cell);
		}
		{
			DiagramOutputBean output = new DiagramOutputBean();
			output.setName("output");
			mxGeometry geo = new mxGeometry(0,0,50,20);
			mxCell cell = new mxCell(output, geo, "");
			cell.setConnectable(false);
			cell.setVertex(true);
			mxGeometry geo1 = new mxGeometry(0, 1/2.0, PORT_DIAMETER,
					PORT_DIAMETER);

			geo1.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(output, geo1, "portConstraint=west;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
			port.setVertex(true);
			
			cell.insert(port);
			p.addTemplate("Wyjście", null, cell);
		}
		{
			VCCNode input = new VCCNode();
			
			mxGeometry geo = new mxGeometry(0,0,50,20);
			mxCell cell = new mxCell(input, geo, "");
			cell.setConnectable(false);
			cell.setVertex(true);
			mxGeometry geo1 = new mxGeometry(1, 1/2.0, PORT_DIAMETER,
					PORT_DIAMETER);
	
			geo1.setOffset(new mxPoint(0, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(input, geo1, "portConstraint=east;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
			port.setVertex(true);
			cell.insert(port);
			p.addTemplate("VCC", null, cell);
			
		}
		{
			GNDNode gnd = new GNDNode();
			mxGeometry geo = new mxGeometry(0,0,50,20);
			mxCell cell = new mxCell(gnd, geo, "");
			cell.setConnectable(false);
			cell.setVertex(true);
			mxGeometry geo1 = new mxGeometry(1, 1/2.0, PORT_DIAMETER,
					PORT_DIAMETER);
	
			geo1.setOffset(new mxPoint(0, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(gnd, geo1, "portConstraint=east;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
			port.setVertex(true);
			cell.insert(port);
			p.addTemplate("GND", null, cell);
			
		}
		
	}
	
	protected void showGraphPopupMenu(MouseEvent e)
	{
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
				graphComponent);
		EditorPopupMenu menu = new EditorPopupMenu(GraphEditor.this);
		menu.show(graphComponent, pt.x, pt.y);

		e.consume();
	}
	
	public EditorPalette getPalette(int index) {
		return palettes.get(index).getPalette();
	}
	
	public void fillAllPalettes() {
		for(PathPaletteGroup p : palettes) {
			fillPalette(p);
		}
	}
	
	public GraphEditor(String appTitle, mxGraphComponent component) {
		super(appTitle, component);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fontMetr = graphComponent.getFontMetrics(new Font("Times",Font.PLAIN, 12));
		EditorPalette io = this.insertPalette("Wej/Wyj");
		EditorPalette gatespalette = this.insertPalette("Bramki logiczne");
		EditorPalette oth = this.insertPalette("Domyslne");
		palettes.add(new PathPaletteGroup(gatespalette, "xmls/gates"));
		palettes.add(new PathPaletteGroup(oth, "xmls"));
		fillAllPalettes();
		ioPalette(io);
		
	}

}
