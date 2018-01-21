package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.RecurrentLoopException;

import java.awt.GridBagLayout;

public class BlockInfo extends JDialog {

	private JPanel contentPane;
	private JTextField textField;
	JPanel infoPanel;
	GridBagConstraints c;
	BlockBean block;
	
	private void loadInfo() {
		textField.setText(block.getName());
		if(block.getType().equals("formula")) {
			
			JScrollPane pane = new JScrollPane();
			JTable table = new JTable();
			int noOfInputs = block.getInputList().size();
			int noOfOutputs = block.getOutputList().size();
			String[] columnNames = new String[noOfInputs+noOfOutputs];
			Class[] types = new Class[noOfInputs+noOfOutputs];
			for(int i=0;i<noOfInputs;i++) {
				columnNames[i] = block.getInput(i).getName();
				types[i] = Integer.class;
			}
			for(int i=0;i<noOfOutputs;i++) {
				columnNames[noOfInputs+i] = block.getOutput(i).getName();
				types[noOfInputs+i] = Integer.class;
			}
			
			int rowCount = (int) Math.pow(2, noOfInputs);
			String[][] data = new String[rowCount][noOfInputs+noOfOutputs];
			for(int i=0;i<rowCount; i++) {
				for(int j=0;j<noOfInputs;j++) {
					int pow2 = (int) Math.pow(2, noOfInputs-j-1);
					data[i][j] = new Integer((int)Math.signum(i & pow2)).toString();
				}	
			}
			for(int i=0; i<noOfOutputs; i++) {
				try {
					int outputs[] = block.getTruthTable(block.getOutput(i));
					for(int j=0; j<outputs.length; j++) {
						data[j][noOfInputs+i] = new Integer(outputs[j]).toString();
					}
				}
				catch(RecurrentLoopException e) {}
				
			}
			
			table.setModel(new DefaultTableModel(
					data,
					columnNames
				) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
					Class[] columnTypes = types;
					public Class getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				});
			int minwidth = table.getColumnModel().getColumn(0).getMinWidth();
			FontMetrics metrics = table
	                .getFontMetrics(table.getFont());
			for(int i=0;i<noOfInputs+noOfOutputs;i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(minwidth + metrics.stringWidth(table.getColumnName(i)));
			table.getColumnModel().getColumn(i).setResizable(false);
			}
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			pane.setViewportView(table);
			infoPanel.add(pane, BorderLayout.CENTER);
		}
		else if(block.getType().equals("diagram")){
			JPanel diagramInfoPanel = new JPanel();
			GridBagLayout gbl_infoPanel = new GridBagLayout();
			c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.weightx = 1;
			c.gridx = 0;
			c.weighty = 0;
			diagramInfoPanel.setLayout(gbl_infoPanel);
			DiagramBean diagram = block.getDiagram();
			JLabel diagramName = new JLabel(""+diagram.getName());
			JLabel templateName = new JLabel("Wzorzec: "+ block.getTemplateBlock().getName() + " (" + block.getTemplateBlock().getFile().getAbsolutePath() +")");
			diagramInfoPanel.add(diagramName,c);
			diagramInfoPanel.add(templateName,c);
			c.weighty = 1;
			diagramInfoPanel.add(Box.createGlue(),c);
			c.weighty=0;
			
			infoPanel.add(diagramInfoPanel, BorderLayout.CENTER);
		}
	}
	/**
	 * Create the frame.
	 */
	public BlockInfo(BlockBean block) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setBounds(100, 100, 394, 340);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel, BorderLayout.NORTH);
		
		JLabel lblNazwa = new JLabel("Nazwa:");
		panel.add(lblNazwa);
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(25);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.EAST);
		
		JButton btnNewButton = new JButton("OK");
		panel_2.add(btnNewButton);
		
		infoPanel = new JPanel();
		infoPanel.setBorder(new TitledBorder(null, "Informacje", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(infoPanel, BorderLayout.CENTER);
		infoPanel.setLayout(new BorderLayout());
		
		this.block = block;
		loadInfo();
	}
}
