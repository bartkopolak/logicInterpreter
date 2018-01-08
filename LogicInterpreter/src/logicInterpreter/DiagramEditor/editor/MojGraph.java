package logicInterpreter.DiagramEditor.editor;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.MenuBarUI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.BasicGraphEditor;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.SchemaEditorMenuBar;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

public class MojGraph {

	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	
	

	
	public MojGraph() {
		Document xmlDocument = mxDomUtils.createDocument();
		Element inputNode = xmlDocument.createElement("input");
		Element outputNode = xmlDocument.createElement("output");
		
		mxMultiplicity[] multiplicities = new mxMultiplicity[1];
		multiplicities[0] = new mxMultiplicity(false, "input", null, null, 0,
				"1", Arrays.asList(new String[] { "output" }),
				"Target Must Have 1 Source", "Target Must Connect From Source",
				true);
		
		
		mxGraph graph = new mxGraph();
		graph.setCellsResizable(false);
		graph.setCellsMovable(true);
		graph.setAllowDanglingEdges(false);
		graph.setAllowLoops(false);
		//graph.setConnectableEdges(true);
		graph.setGridEnabled(true);
		graph.setAllowNegativeCoordinates(false);
	    graph.setResetEdgesOnConnect(false);
	    //graph.setResetEdgesOnMove(true);
	    graph.setMultiplicities(multiplicities);
	    
	    
	    mxFastOrganicLayout layout2 = new mxFastOrganicLayout(graph);
	    layout2.setForceConstant(150);
	    layout2.setMinDistanceLimit(5);
	    layout2.execute(graph.getDefaultParent());
		
		Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
		style.put(mxConstants.STYLE_ORTHOGONAL, "true");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ALIGN_BOTTOM);
		style.put(mxConstants.STYLE_BENDABLE, true);
		
		mxGeometry geo = new mxGeometry(0,0,40,60);
		
		mxCell v1 = new mxCell(null, geo, "");
		v1.setConnectable(false);
		v1.setVertex(true);
		mxGeometry geo1 = new mxGeometry(0, 1.0/3, PORT_DIAMETER,
				PORT_DIAMETER);
		// Because the origin is at upper left corner, need to translate to
		// position the center of port correctly
		geo1.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
		geo1.setRelative(true);
		mxCell port1 = new mxCell(inputNode, geo1, "portConstraint=west;deletable=0;noLabel=1");
		port1.setVertex(true);
		
		mxGeometry geo2 = new mxGeometry(0, 2.0/3, PORT_DIAMETER,
				PORT_DIAMETER);

		geo2.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
		geo2.setRelative(true);
		
		mxGeometry geo3 = new mxGeometry(1, 1.0/2, PORT_DIAMETER,
				PORT_DIAMETER);

		geo3.setOffset(new mxPoint(0, -PORT_RADIUS));
		geo3.setRelative(true);
		
		
		mxCell port2 = new mxCell(inputNode, geo2, "portConstraint=west;deletable=0;noLabel=1");
		port2.setVertex(true);
		mxCell port3 = new mxCell(outputNode, geo3, "portConstraint=east;deletable=0;noLabel=1");
		port3.setVertex(true);

		v1.insert(port1);
		v1.insert(port2);
		v1.insert(port3);
		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setGridVisible(true);
		graphComponent.setGridStyle(0);
		graphComponent.setGridColor(Color.BLACK);
		graphComponent.setBackground(Color.white);
		graphComponent.setToolTips(true);
		//graphComponent.setFoldingEnabled(false);
		
		BasicGraphEditor editor = new BasicGraphEditor("Edytor diagramów", graphComponent);
		EditorPalette shapesPalette = editor.insertPalette("schema");
		shapesPalette.addTemplate("Block",null,v1);
		

		SchemaEditorMenuBar menubar = new SchemaEditorMenuBar(editor);
		editor.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		
		
		JFrame mainFrame = editor.createFrame(menubar);
		mainFrame.setVisible(true);
		graph.refresh();
		editor.repaint();
		
		graph.getModel().addListener(mxEvent.CHANGE, new mxIEventListener()
		{
			public void invoke(Object sender, mxEventObject evt)
			{
				graphComponent.validateGraph();
			}
		});
		
	}
	
	public static void main(String[] args) {
		MojGraph m = new MojGraph() ;
		
	}
}
