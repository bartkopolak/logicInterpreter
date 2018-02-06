package logicInterpreter.DiagramEditor.editor;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.BasicGraphEditor;
import logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.EditorPopupMenu;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.PinBind;
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

public class DigitalCircuitEditor extends BasicGraphEditor {

	public class PathPaletteGroup{
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
	
	public class AdditionalEdgeForOutputNode{
		private OutputBean node;
		private mxCell additionalEdge;
	
		
		public AdditionalEdgeForOutputNode(OutputBean node, mxCell extraCell) {
			this.node = node;
			additionalEdge = extraCell;
		}


		public OutputBean getNode() {
			return node;
		}

		public mxCell getAdditionalEdge() {
			return additionalEdge;
		}

		
	}
	
	private List<PinBind> alteraSimPinBinds = null;
	
	public List<PinBind> getAlteraSimPinBinds() {
		return alteraSimPinBinds;
	}

	public void setAlteraSimPinBinds(List<PinBind> alteraSimPinBinds) {
		this.alteraSimPinBinds = alteraSimPinBinds;
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
	
	public static mxCell getParent(mxCell trgCell,ArrayList<mxCell> visitedEdges) {
		mxCell source;
		for(int i=0; i<trgCell.getEdgeCount(); i++) {
			
			mxCell trgEdge = (mxCell) trgCell.getEdgeAt(i);	//pobierz polaczenie pomiedzy pinami
			if(visitedEdges.contains(trgEdge)) continue;
			mxCell trgEdgeTarget = (trgEdge.getSource().equals(trgCell)) ? 	//pobierz cel polaczenia
					(mxCell)trgEdge.getTarget() : (mxCell)trgEdge.getSource();
			if(trgEdgeTarget != null) {
				if(trgEdgeTarget.getValue() instanceof OutputBean || trgEdgeTarget.getValue() instanceof DiagramInputBean) {	//jesli polaczono z wyjsciem, wejscie zajęte
					return trgEdgeTarget;
				}
				if(trgEdgeTarget.getValue() instanceof InputBean) {     //jesli polaczono z wejsciem, sprawdz, czy owe wejscie nie ma polaczenia z wyjsciem
					visitedEdges.add(trgEdge);
					return getParent(trgEdgeTarget, visitedEdges);
				}
			}
		}
		return null;
	}
	
	public ArrayList<PathPaletteGroup> palettes = new ArrayList<PathPaletteGroup>();
	public ArrayList<String> paletteNames = new ArrayList<String>();
	
	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	FontMetrics fontMetr;
	
	private String diagramName = "";
	
	
	public String getDiagramName() {
		return diagramName;
	}

	public void setDiagramName(String diagramName) {
		this.diagramName = diagramName;
	}

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
		
		int width = 20*(maxNSpins);
		block.setBaseCellRect(new Rectangle(width, height));
		mxGeometry geo = new mxGeometry(0,0,width + fontMetr.stringWidth(block.toString()) ,height);
		mxCell cell = new mxCell(block, geo, "");
		cell.setConnectable(false);
		cell.setVertex(true);
		
		int nindex=0, windex=0,sindex=0;
		
		for(int i=0; i<inputsNo; i++) {
			BlockInputBean inputNode = block.getInput(i);
			String posAttr = inputNode.getPosition();
			double pos;
			mxGeometry geo1 = null;
			mxCell port = null;
			if(posAttr.equals("west") || posAttr.equals("") || posAttr == null) {

				pos = (double)(windex+1)/(wcount+1.0);
				windex++;
				geo1 = new mxGeometry(0, pos, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
				geo1.setRelative(true);
				port = new mxCell(inputNode, geo1, "portConstraint=west;deletable=0;labelPosition=left;labelWidth=20;labelPadding=10");
			}
			else if(posAttr.equals("north")) {
				pos = (double)(nindex+1)/(ncount+1.0);
				nindex++;
				geo1 = new mxGeometry(pos, 0, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_DIAMETER));
				geo1.setRelative(true);
				port = new mxCell(inputNode, geo1, "portConstraint=north;deletable=0;labelPosition=center;verticalLabelPosition=top;labelWidth=20;labelPadding=10");
			}
			else if(posAttr.equals("south")) {
				pos = (double)(sindex+1)/(scount+1.0);
				sindex++;
				geo1 = new mxGeometry(pos, 1, PORT_DIAMETER,
						PORT_DIAMETER);
				geo1.setOffset(new mxPoint(-PORT_RADIUS, 0));
				geo1.setRelative(true);
				port = new mxCell(inputNode, geo1, "portConstraint=south;deletable=0;labelPosition=center;verticalLabelPosition=top;labelWidth=20;labelPadding=10");
			}
			
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
		palette.getPalette().repaint();
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
	public int defPalettesCount;
	String palettesPath="palettes.xml";
	
	public void addPalette(String palName, String palPath) {
		paletteNames.add(palName);
		EditorPalette palette = this.insertPalette(palName);
		palettes.add(new PathPaletteGroup(palette, palPath));
		fillAllPalettes();
	}
	
	public void removePalette(String path) {
		int index = -1;
		for(int i=0; i<palettes.size(); i++) {
			PathPaletteGroup ppg = palettes.get(i);
			if(ppg.getPath().equals(path)) {
				index = i;
				break;
			}
		}
		if(index >0) {
			palettes.remove(index);
			paletteNames.remove(index);
			getLibraryPane().remove(defPalettesCount+index-1);
			
		}
	}
	
	public void loadPalettes() throws SAXException, IOException, ParserConfigurationException {
		paletteNames.add("Bramki logiczne");
		paletteNames.add("Domyslne");
		EditorPalette gatespalette = this.insertPalette(paletteNames.get(0));
		EditorPalette oth = this.insertPalette(paletteNames.get(1));
		palettes.add(new PathPaletteGroup(gatespalette, "xmls/gates"));
		palettes.add(new PathPaletteGroup(oth, "xmls"));
		defPalettesCount = palettes.size();
		File f = new File(palettesPath);
		if(!f.exists())
			try {
				saveCustomPalettes();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = DiagFileUtils.getDocument(f);
		doc.getDocumentElement().normalize();
		int paletteCount = doc.getElementsByTagName("palette").getLength();
		for(int i=0; i<paletteCount;i++) {
			Element paletteElem = (Element) doc.getElementsByTagName("palette").item(i);
			String palName = paletteElem.getAttribute("name");
			String palPath = paletteElem.getAttribute("path");
			addPalette(palName,palPath);
		}
	}
	public void saveCustomPalettes() throws SAXException, IOException, ParserConfigurationException, TransformerException {
		
		File f = new File(palettesPath);
		if(!f.exists()) f.createNewFile();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("palettes");
		doc.appendChild(rootElement);
		for(int i=defPalettesCount; i<palettes.size(); i++) {
			PathPaletteGroup pg = palettes.get(i);
			String pname = paletteNames.get(i);
			Element palElem = doc.createElement("palette");
			palElem.setAttribute("name", pname);
			palElem.setAttribute("path", pg.getPath());
			rootElement.appendChild(palElem);
		}
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(f)));	
		
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

	private String getBasename(String name) {
		String basename[] = name.split("[_]");
		if(basename.length>1)
			basename = Arrays.copyOfRange(basename, 0, basename.length-1);
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<basename.length; i++)
			sb.append(basename[i]);
		return sb.toString();
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
	
	private ArrayList<AdditionalEdgeForOutputNode> additionalEdgesList = new ArrayList<AdditionalEdgeForOutputNode>();
	
	public ArrayList<AdditionalEdgeForOutputNode> getAdditionalEdgesList() {
		return additionalEdgesList;
	}

	public void linkInputToInputConnections(mxCell srcCell, OutputBean outputNode) {
		AdditionalEdgeForOutputNode extraEdge = null;
		for(int k=0; k<srcCell.getEdgeCount(); k++) {
			mxCell inputEdge = (mxCell) srcCell.getEdgeAt(k);		//pobierz połączenie wychodzące z pinu wejścia
			mxCell inputEdgeTarget = (inputEdge.getSource().equals(srcCell)) 
					? (mxCell) inputEdge.getTarget() : (mxCell) inputEdge.getSource();	//zidentyfikuj cel 
			Object ieVal = inputEdgeTarget.getValue();				//pobierz obiekt układu logicznego reprezentowany przez cel połączenia
			if(!inputsLinkedWithInputEdges.contains(inputEdge) && ieVal instanceof InputBean) {	//jeśli dane wejście jest połączone z innym wejściem
				InputBean tietNode = (InputBean) ieVal;
				inputsLinkedWithInputEdges.add(inputEdge);
				extraEdge = new AdditionalEdgeForOutputNode(outputNode, inputEdge);
				outputNode.addLink(tietNode);	
				linkInputToInputConnections(inputEdgeTarget, outputNode);
			}
			if(extraEdge != null)
				additionalEdgesList.add(extraEdge);
		}
		
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
				((BlockOutputBean) value).setState(ThreeStateBoolean.UNKNOWN);
				blOutputs.add((BlockOutputBean) value);
				allOutputCells.add((mxCell) cellsList.get(i));
				
			}
			else if(value instanceof DiagramInputBean) {
				if(!(((mxCell) cellsList.get(i)).getChildCount() == 0)) {
					((DiagramInputBean) value).setState(ThreeStateBoolean.UNKNOWN);
					diagInputs.add((DiagramInputBean) value);
					mxCell pin = (mxCell) ((mxCell) cellsList.get(i)).getChildAt(0);
					pin.setValue((DiagramInputBean) value);
					allOutputCells.add(pin);
				}
				
			}
			else if (value instanceof DiagramOutputBean) {
				if(!(((mxCell) cellsList.get(i)).getChildCount() == 0)) {
					((DiagramOutputBean) value).setState(ThreeStateBoolean.UNKNOWN);
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
				int n = checkList(blockNames, getBasename(b.getName()));		
				if(n >= 0) b.setName(getBasename(b.getName()) + "_" + n);
				b.setName(b.getName().replaceAll("[.]", ""));
			}
		}
		{
			ArrayList<StringInt> diagInputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagInputs.size(); i++) {
				DiagramInputBean b = diagInputs.get(i);
				int n = checkList(diagInputsNames,getBasename(b.getName()));
				if(n >= 0) b.setName(getBasename(b.getName()) + "_" + n);
				else b.setName(getBasename(b.getName()));
				b.setName(b.getName().replaceAll("[.]", ""));
			}
		}
		{
			ArrayList<StringInt> diagOutputsNames = new ArrayList<StringInt>();
			for(int i=0; i<diagOutputs.size(); i++) {
				DiagramOutputBean b = diagOutputs.get(i);
				int n = checkList(diagOutputsNames,getBasename(b.getName()));
				if(n >= 0) b.setName(getBasename(b.getName()) + "_" + n);
				b.setName(b.getName().replaceAll("[.]", ""));
			}	
		}
		
		inputsLinkedWithInputEdges.clear();
		//link outputs with inputs
		for(int i=0; i<allOutputCells.size(); i++) {
			mxCell srcCell = allOutputCells.get(i);											//obiekt diagramu - pin wyjscia
			OutputBean outputNode = (OutputBean) srcCell.getValue();						//pin wyjściowy układu logicznego
			outputNode.resetLinks();														//zresetuj połączenia wyjścia
			for(int j=0; j<srcCell.getEdgeCount(); j++) {									//dla każdego połaczenia wychodzącego w diagramie z wyjścia
				mxCell edge = (mxCell) srcCell.getEdgeAt(j);								//obiekt połączenia w diagramie
				if(edge != null) {
					mxCell targetCell = (mxCell) edge.getTarget();							//obiekt diagramu - cel połączenia
					Object value = targetCell.getValue();									//obiekt układu który reprezentowany jest przez wyzej zdefiniowany obiekt diagramu
					if(value != null) {
						if(!(value instanceof InputBean)){									//jeśli wartość obiektu diagramu nie jest pinem wejścia
							targetCell = (mxCell) edge.getSource();							//to moze znaczyć, że połączenie pomiędzy pinami jest odwrotnie przypisane
							value = targetCell.getValue();
						}
						if(value instanceof InputBean){										//jeśli cel połączenia to pin wejścia
							InputBean target = (InputBean) value;							//pin wejściowy układu logicznego
							if(checkCellBusy(targetCell, new ArrayList<mxCell>()) > 1) 
								throw new MultipleOutputsInInputException(".", target.toString());	//sprawdza czy do pinu nie połączono więcej niż 1 wyjście
							outputNode.addLink(target);										//dodaj połączenie do tego wejścia
							//stwórz połączenie do wejść, które poł;aćzone są z innymi wejściami
							//(gdy wejście B połączone jest z wejściem A, a A połączone jest z wyjściem X to wejście B ma stan wyjścia X)
							//FIXME: rekurencyjnie lącz pin wejściowy z pinami wejsciowymi,warunek wyzej!
							linkInputToInputConnections(targetCell, outputNode);
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
		EditorPopupMenu menu = new EditorPopupMenu(DigitalCircuitEditor.this);
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
	
	public DigitalCircuitEditor(String appTitle, mxGraphComponent component) {
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
		try {
			loadPalettes();
			fillAllPalettes();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ioPalette(io);
		
	}

}
