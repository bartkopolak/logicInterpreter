package logicInterpreter.DiagramEditor.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.TextMeasurer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.MenuBarUI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.BasicGraphEditor;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.SchemaEditorMenuBar;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.Wire;
import logicInterpreter.Tools.DiagFileUtils;

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

public class MojGraph {

	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	Document xmlDocument = mxDomUtils.createDocument();
	Element inputNode = xmlDocument.createElement("input");
	Element outputNode = xmlDocument.createElement("output");
	EditorPalette palette;
	
	mxGraph graph = new mxGraph() {

		@Override
		public boolean isValidDropTarget(Object arg0, Object[] arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isSplitEnabled() {
			// TODO Auto-generated method stub
			return true;
		}
		
		

		@Override
		public boolean isCellFoldable(Object arg0, boolean arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		private int checkCellBusy(mxCell trgCell,ArrayList<mxCell> visitedEdges) {
			int count = 0;
			for(int i=0; i<trgCell.getEdgeCount(); i++) {
				
				mxCell trgEdge = (mxCell) trgCell.getEdgeAt(i);	//pobierz polaczenie pomiedzy pinami
				if(visitedEdges.contains(trgEdge)) continue;
				mxCell trgEdgeTarget = (trgEdge.getSource().equals(trgCell)) ? 	//pobierz cel polaczenia
						(mxCell)trgEdge.getTarget() : (mxCell)trgEdge.getSource();
				if(trgEdgeTarget != null) {
					if(trgEdgeTarget.getValue() instanceof OutputBean) {	//jesli polaczono z wyjsciem, wejscie zajęte
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
		
		@Override
		public String getEdgeValidationError(Object cell, Object src, Object trg) {
			
			int outCount = mxGraphModel.getDirectedEdgeCount(model, cell, true);
			int inCount = mxGraphModel.getDirectedEdgeCount(model, cell, false);
			StringBuffer error = new StringBuffer();
			Object value = src;

			if(cell instanceof mxCell) {
				if(src != null && trg != null) {
					mxCell trgCell = (mxCell) trg;
					mxCell srcCell = (mxCell) src;
					if(srcCell.getValue() != null && trgCell.getValue() != null) {
						if(srcCell.getValue() instanceof InputBean && trgCell.getValue() instanceof OutputBean) {
							mxCell temp = srcCell;
							src = trgCell;
							trgCell = temp;
						}
						Object srcVal = srcCell.getValue();
						Object trgVal = trgCell.getValue();
						
						//if(((mxCell)trg).isEdge())System.out.println("edge");
						//else System.out.println(src.toString());
						
						if(srcVal instanceof OutputBean && (trgVal instanceof OutputBean)) {
							error.append("Wyjście może być połączone z wejściem lub wyjściem układu\n");
							return (error.length() > 0) ? error.toString() : null;
						}
						
						int inputOutputConns = checkCellBusy(trgCell, new ArrayList<mxCell>());
						
						System.out.println(String.valueOf(inputOutputConns));
						if(inputOutputConns > 1) {
							error.append("Wejście może mieć tylko 1 zródło.\n");
							return (error.length() > 0) ? error.toString() : null;
						}
						
					
					}
				}
				
				
			}
			return super.getEdgeValidationError(cell,src,trg);
		}
		
			
	
		//*
					// Overrides method to store a cell label in the model
		@Override
					public void cellLabelChanged(Object cell, Object newValue,
							boolean autoSize)
					{
						if (cell instanceof mxCell && newValue != null)
						{
							Object value = ((mxCell) cell).getValue();

							if (value instanceof BlockBean)
							{
								BlockBean block = (BlockBean) value;
								block.setName((String)newValue);
							}
							else if(value instanceof InputBean) {
								InputBean input = (InputBean) value;
								input.setName((String)newValue);
							}
							else if(value instanceof OutputBean) {
								OutputBean output = (OutputBean) value;
								output.setName((String)newValue);
							}
							this.refresh();
						}

						//super.cellLabelChanged(cell, newValue, autoSize);
					}
		//*/
		public String convertValueToString(Object cell)
		{
			if (cell instanceof mxCell)
			{
				Object value = ((mxCell) cell).getValue();

				if (value instanceof BlockBean)
				{
					BlockBean b = (BlockBean) value;
					return b.getName();
				}
				else if(value instanceof InputBean) {
					InputBean input = (InputBean) value;
					return input.getName();
				}
				else if(value instanceof OutputBean) {
					OutputBean output = (OutputBean) value;
					return output.getName();
				}
			}

			return super.convertValueToString(cell);
		}
	};
		
	
	
	
	
	public MojGraph() {

		
		mxMultiplicity[] multiplicities = new mxMultiplicity[1];
		multiplicities[0] = new mxMultiplicity(false, "conntype", "input" , null, 0,
				"1", Arrays.asList(new String[] { "output" }),
				"Target Must Have 1 Source", "Target Must Connect From Source",
				true);
		
		mxCodecRegistry.addPackage(Node.class.getPackage().toString()); 
		mxCodecRegistry.addPackage(BlockBean.class.getPackage().toString()); 
		
		mxCodecRegistry.register(new mxObjectCodec(new BlockBean(), new String[] {"file", "templateBlock"}, null, null));
	    mxCodecRegistry.register(new mxObjectCodec(new InputBean(), new String[] {"from"},null,null));
	    mxCodecRegistry.register(new mxObjectCodec(new OutputBean(),new String[] {"wire", "state"}, null, null));
	    mxCodecRegistry.register(new mxObjectCodec(new logicInterpreter.Nodes.DiagramInputBean()));
	    mxCodecRegistry.register(new mxObjectCodec(new logicInterpreter.Nodes.DiagramOutputBean()));
	    mxCodecRegistry.register(new mxObjectCodec(new BlockInputBean(), null, new String[] {"parent"},null));
	    mxCodecRegistry.register(new mxObjectCodec(new BlockOutputBean(), null ,new String[] {"parent"},null));
	    mxCodecRegistry.register(new mxObjectCodec(new Wire(), new String[] {"id"}, new String[] {"parent", "to"},null));
		mxCodecRegistry.register(new mxObjectCodec(new logicInterpreter.Nodes.Node()));
	    
		
		
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
	    graph.setDefaultOverlap(0);
	    
	    mxFastOrganicLayout layout2 = new mxFastOrganicLayout(graph);
	    layout2.setForceConstant(150);
	    layout2.setMinDistanceLimit(5);
	    layout2.execute(graph.getDefaultParent());
		
		Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
		style.put(mxConstants.STYLE_ORTHOGONAL, "true");
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ALIGN_BOTTOM);
		style.put(mxConstants.STYLE_BENDABLE, true);
		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setGridVisible(true);
		graphComponent.setGridStyle(0);
		graphComponent.setGridColor(Color.BLACK);
		graphComponent.setBackground(Color.white);
		graphComponent.setToolTips(true);
		
		//graphComponent.setFoldingEnabled(false);
		
		GraphEditor editor = new GraphEditor("Edytor diagramów", graphComponent);
		

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