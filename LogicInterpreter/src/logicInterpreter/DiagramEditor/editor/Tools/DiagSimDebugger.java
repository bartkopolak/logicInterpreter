package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.mxgraph.model.mxCell;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.DiagramEditor.editor.GraphEditor;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.GNDNode;
import logicInterpreter.Nodes.OutputBean;
import logicInterpreter.Nodes.VCCNode;

import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;

public class DiagSimDebugger extends JFrame {

	private JPanel contentPane;

	private JPanel panel_inp;
	private JPanel panel_out;
	private JScrollPane scrollPane_inp;
	private JScrollPane scrollPane_out;
	private GraphEditor editor;
	private DiagramBean diagram;
	private boolean evalOnChange = true;
	private ArrayList<JLabel> outputValLabels = new ArrayList<JLabel>();
	
	private ArrayList<mxCell> edges = new ArrayList<mxCell>();
	
	private void evaluate() {
		try {
			diagram.evaluate();
			colorEdges();
		} catch (RecurrentLoopException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void colorEdges() {
		ArrayList<mxCell> outputs = editor.getAllOutputCells();
		edges.clear();
		//link outputs with inputs
				for(int i=0; i<outputs.size(); i++) {
					mxCell outcell = outputs.get(i);
					outcell.setCollapsed(true);
					OutputBean outputNode = ((OutputBean)outcell.getValue());
					ThreeStateBoolean state;
					if(outputNode instanceof BlockOutputBean) {
						String outCellName = outputNode.getName();
						BlockBean block = ((BlockOutputBean) outputNode).getParent();
						BlockBean dblock = diagram.getBlock(block.getName());
						state = dblock.getOutput(outCellName).getState();
					}
					else if(outputNode instanceof DiagramInputBean) {
						state = diagram.getInput(((DiagramInputBean)outputNode).getName()).getState();
					}
					else state = ThreeStateBoolean.UNKNOWN;
					
					
					for(int j=0; j<outcell.getEdgeCount(); j++) {
						mxCell edge = (mxCell) outcell.getEdgeAt(j);
						edges.add(edge);
						if(edge != null) {
							if(state.equals(ThreeStateBoolean.FALSE)) {
								edge.setStyle("strokeColor=red");
							}
							else if(state.equals(ThreeStateBoolean.TRUE)) {
								edge.setStyle("strokeColor=green");
								
							}
							else {
								edge.setStyle("strokeColor=black");
							}
							
						}	  
					}
					
				}
		for(int i=0; i<diagram.getOutputList().size(); i++) {
			JLabel l = outputValLabels.get(i);
			l.setText(diagram.getOutput(i).getState().toString());
		}
		editor.getGraphComponent().refresh();
	}
	
	private void initInputStates() {
		for(int i=0; i<diagram.getInputList().size(); i++) {
			DiagramInputBean input = diagram.getInput(i);
			if(input instanceof VCCNode || input instanceof GNDNode) continue;
			input.setState(new ThreeStateBoolean(false));
		}
	}
	
	public void load() {
		editor.getGraphComponent().refresh();
		outputValLabels.clear();
		JPanel inputsPanelInner = new JPanel();
		JPanel outputsPanelInner = new JPanel();
		scrollPane_inp.setViewportView(inputsPanelInner);
		scrollPane_out.setViewportView(outputsPanelInner);
		
		inputsPanelInner.setLayout(new GridBagLayout());
		outputsPanelInner.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.gridx = 0;
		c.weighty = 0;
		
		for(int i=0; i<diagram.getInputList().size(); i++) {
			DiagramInputBean input = diagram.getInput(i);
			if(input instanceof VCCNode || input instanceof GNDNode) continue;
			JPanel inpUnitPanel = new JPanel();
			inpUnitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JCheckBox inputBox = new JCheckBox(input.getName());
			inputBox.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					input.setState(new ThreeStateBoolean(inputBox.isSelected()));
					if(evalOnChange) {
						evaluate();
					}
				}
			});
			inpUnitPanel.add(inputBox);
			inputsPanelInner.add(inpUnitPanel,c);
			
		}
		c.weighty = 1;
		inputsPanelInner.add(Box.createGlue(), c);
		c.weighty = 0;
		for(int i=0; i<diagram.getOutputList().size(); i++) {
			DiagramOutputBean output = diagram.getOutput(i);
			JPanel outUnitPanel = new JPanel();
			outUnitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel outputLabel = new JLabel(output.getName() + ":");
			JLabel valueLabel = new JLabel("?");
			outputValLabels.add(valueLabel);
			outUnitPanel.add(outputLabel);
			outUnitPanel.add(valueLabel);
			outputsPanelInner.add(outUnitPanel,c);
		}
		c.weighty = 1;
		outputsPanelInner.add(Box.createGlue(), c);
		c.weighty = 0;
		
	}
	
	public void close() {
		for(int i=0; i<edges.size(); i++) {
			mxCell edge = edges.get(i);
			edge.setStyle("strokeColor=#6482b9");
		}
			
		editor.getGraphComponent().refresh();
		editor.getGraphComponent().setEnabled(true);
		dispose();
	}
	
	/**
	 * Create the frame.
	 */
	public DiagSimDebugger(GraphEditor ge, DiagramBean diag) {
		
		editor = ge;
		diagram = diag;
		editor.getGraphComponent().setEnabled(false);
		editor.getGraphComponent().getGraph().selectCells(false, false, false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				close();
				
			}	
		});
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		panel_out = new JPanel();
		panel_2.add(panel_out, BorderLayout.EAST);
		panel_out.setPreferredSize(new Dimension(140, 10));
		panel_out.setBorder(new TitledBorder(null, "Wyj\u015Bcia", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_out.setLayout(new BorderLayout(0, 0));
		
		scrollPane_out = new JScrollPane();
		panel_out.add(scrollPane_out, BorderLayout.CENTER);
		
		panel_inp = new JPanel();
		panel_2.add(panel_inp);
		panel_inp.setBorder(new TitledBorder(null, "Wej\u015Bcia", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_inp.setLayout(new BorderLayout(0, 0));
		
	    scrollPane_inp = new JScrollPane();
		panel_inp.add(scrollPane_inp, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4, BorderLayout.WEST);
		
		JButton evalBtn = new JButton("Wykonaj");
		evalBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				evaluate();
			}
		});
		evalBtn.setVisible(false);
		panel_4.add(evalBtn);
		
		JCheckBox modeChkBox = new JCheckBox("Przetwórz układ przy zmianie wejścia");
		modeChkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(modeChkBox.isSelected()) {
					evalBtn.setVisible(false);
					evalOnChange = true;
				}
				else {
					evalBtn.setVisible(true);
					evalOnChange = false;
				}
			}
		});
		modeChkBox.setSelected(true);
		panel_4.add(modeChkBox);
		
		JPanel panel_5 = new JPanel();
		panel_3.add(panel_5, BorderLayout.EAST);
		
		JButton btnNewButton = new JButton("Zakończ");
		panel_5.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
				
			}
		});
		load();
		initInputStates();
	}

}
