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
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.BasicGraphEditor;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPopupMenu;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.Wire;
import logicInterpreter.Tools.XMLparse;

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
	
	ArrayList<PathPaletteGroup> palettes = new ArrayList<PathPaletteGroup>();
	
	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	FontMetrics fontMetr;
	
	public void addBlockToPalette(BlockBean block, PathPaletteGroup palette) {
		int inputsNo =  block.getInputList().size();
		int outputsNo = block.getOutputList().size();
		int height = 20 + 20 *inputsNo;
		
		int width = 20 + fontMetr.stringWidth(block.toString());
		mxGeometry geo = new mxGeometry(0,0,width,height);
		mxCell cell = new mxCell(block, geo, "");
		cell.setConnectable(false);
		cell.setVertex(true);
		for(int i=0; i<inputsNo; i++) {
			double pos = (double)(i+1)/(inputsNo+1.0);
			mxGeometry geo1 = new mxGeometry(0, pos, PORT_DIAMETER,
					PORT_DIAMETER);
			geo1.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
			geo1.setRelative(true);
			mxCell port = new mxCell(block.getInput(i), geo1, "portConstraint=west;deletable=0;labelPosition=left;labelWidth=20;labelPadding=10");
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
				if(!list[i].isFile()) continue;
				if(XMLparse.getType(list[i]).equals("block")) {
					BlockBean block;
					try {
						block = XMLparse.parseXMLBlock(list[i]);
						addBlockToPalette(block, palette);
					} catch (Exception e) {
					}
					
				}
					
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

	public DiagramBean createDiagram() {
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
				if(n >= 0) b.setName(b.getName() + "." + n);
			}
		}
		{
			ArrayList<StringInt> diagInputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagInputs.size(); i++) {
				DiagramInputBean b = diagInputs.get(i);
				int n = checkList(diagInputsNames,b.getName());
				if(n >= 0) b.setName(b.getName() + "." + n);
			}
		}
		{
			ArrayList<StringInt> diagOutputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagOutputs.size(); i++) {
				DiagramOutputBean b = diagOutputs.get(i);
				int n = checkList(diagOutputsNames,b.getName());
				if(n >= 0) b.setName(b.getName() + "." + n);
			}	
		}
		
		
		//link outputs with inputs
		for(int i=0; i<allOutputCells.size(); i++) {
			mxCell outcell = allOutputCells.get(i);
			OutputBean outputNode = (OutputBean) outcell.getValue();
			
			for(int j=0; j<outcell.getEdgeCount(); j++) {
				mxCell edge = (mxCell) outcell.getEdgeAt(j);
				
				if(edge != null) {
					Object value = edge.getTarget().getValue();
					if(value != null) {
						InputBean target = (InputBean) value;
						outputNode.addLink(target);
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
			mxCell port = new mxCell(input.getName(), geo1, "portConstraint=east;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
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
			mxCell port = new mxCell(output.getName(), geo1, "portConstraint=west;deletable=0;labelPosition=right;labelWidth=20;labelPadding=10;noLabel=true");
			port.setVertex(true);
			
			cell.insert(port);
			p.addTemplate("Wyjście", null, cell);
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
		EditorPalette palette = this.insertPalette("Domyslne");
		
		palettes.add(new PathPaletteGroup(palette, "xmls"));
		fillAllPalettes();
		ioPalette(io);
		
	}

}
