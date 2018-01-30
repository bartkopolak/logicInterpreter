package logicInterpreter.DiagramEditor.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.ArrayList;

import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorPalette;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.SchemaEditorMenuBar;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorActions.SaveAction;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.GNDNode;
import logicInterpreter.Nodes.InputBean;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.VCCNode;
import logicInterpreter.Nodes.Wire;

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.layout.mxFastOrganicLayout;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;


import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

public class MojGraph {

	final int PORT_DIAMETER = 8;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	EditorPalette palette;
	GraphEditor editor;
	JFrame mainFrame;
	private static int framesOpened;
	FontMetrics fontMetrics;
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
		
		@Override
		public String getEdgeValidationError(Object cell, Object src, Object trg) {
			
		
			StringBuffer error = new StringBuffer();

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
							error.append("Wyjście musi być połączone z wejściem.\n");
							return (error.length() > 0) ? error.toString() : null;
						}
						
						int inputOutputConns = GraphEditor.checkCellBusy(trgCell, new ArrayList<mxCell>());
						
						//System.out.println(String.valueOf(inputOutputConns));
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
							mxCell dCell = (mxCell)cell;
							Object value = dCell.getValue();

							if (value instanceof BlockBean)
							{
								BlockBean block = (BlockBean) value;
								block.setName((String)newValue);
								int width = block.getBaseCellRect().width + fontMetrics.stringWidth(block.toString());
								int height = block.getBaseCellRect().height;
								dCell.getGeometry().setWidth(width);
								dCell.getGeometry().setHeight(height);
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
		
	
	
	
	public GraphEditor getEditor() {
		return editor;
	}

	public void exit() {
		mainFrame.dispose();
		framesOpened--;
		if(framesOpened == 0) System.exit(0);
	}
	WindowListener windowListener = new WindowAdapter() {

		@Override
		public void windowClosing(WindowEvent e) {
			if(editor.isModified()) {
				int result = JOptionPane.showConfirmDialog(null, "Czy chcesz zapisać obecny diagram?", "Pytanie", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(result == JOptionPane.YES_OPTION) {
					SaveAction sa = new SaveAction(false);
					sa.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, null));
					if(!sa.isSaveCanceled())
						exit();
					else return;
				}
				else if(result == JOptionPane.NO_OPTION) {
					exit();
				}
				else {
					return;
				}
			}
			else {
				exit();
			}
			
		}
		
	};
	

		
	
	public MojGraph() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mxCodecRegistry.addPackage(Node.class.getPackage().toString()); 
		mxCodecRegistry.addPackage(BlockBean.class.getPackage().toString()); 
		
		mxCodecRegistry.register(new mxObjectCodec(new BlockBean(), new String[] {"file", "templateBlock"}, null, null));
	    mxCodecRegistry.register(new mxObjectCodec(new InputBean(), new String[] {"from"},null,null));
	    mxCodecRegistry.register(new mxObjectCodec(new OutputBean(),new String[] {"wire", "state"}, null, null));
		mxCodecRegistry.register(new mxObjectCodec(new VCCNode()));
		mxCodecRegistry.register(new mxObjectCodec(new GNDNode()));
	    mxCodecRegistry.register(new mxObjectCodec(new DiagramInputBean()));
	    mxCodecRegistry.register(new mxObjectCodec(new DiagramOutputBean()));
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
		fontMetrics = graphComponent.getFontMetrics(new Font("Times",Font.PLAIN, 12));
		//graphComponent.setFoldingEnabled(false);
		
		editor = new GraphEditor("Edytor diagramów", graphComponent);
		

		SchemaEditorMenuBar menubar = new SchemaEditorMenuBar(editor);
		

		
		mainFrame = editor.createFrame(menubar);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(windowListener);
		
		graph.refresh();
		editor.repaint();
		graph.getModel().addListener(mxEvent.CHANGE, new mxIEventListener()
		{
			public void invoke(Object sender, mxEventObject evt)
			{
				graphComponent.validateGraph();
			}
		});
		framesOpened++;
	}
	
	public void view() {
		mainFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		MojGraph m = new MojGraph();
		m.view();
		
	}
}
