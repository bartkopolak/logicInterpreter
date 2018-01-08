package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

public class FuncBlockWizard extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane_inp;
	private JScrollPane scrollPane_out;
	private JTable table;
	private JTextField textField;
	private JTextField funcField;
	private JSpinner spinnerInput;
	private JSpinner spinnerOutput;
	private JTable table_1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FuncBlockWizard frame = new FuncBlockWizard();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	String[] inputNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
	String[] outputData = new String[32];
	private JTextField textField_1;
	
	ArrayList<JRadioButton> outputList = new ArrayList<JRadioButton>();
	
	public JPanel createInputList(int inputsCount) {
		JPanel panel_Inputs = new JPanel();
		panel_Inputs.setLayout(new BoxLayout(panel_Inputs, BoxLayout.Y_AXIS));
		panel_Inputs.removeAll();
		panel_Inputs.invalidate();
		for(int i=0; i<inputsCount; i++) {
			JTextField field = new JTextField();
			int pos = i;
			field.setText(inputNames[i]);
			field.setColumns(8);
			field.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					inputNames[pos] = field.getText();
					createTruthTable(inputsCount);
					
				}
			});
			panel_Inputs.add(field);
			
			panel_Inputs.repaint();
		}
		return panel_Inputs;
	}
	public void createOutputList() {
		ButtonGroup group = new ButtonGroup();
		Integer outputsCount = (Integer) spinnerOutput.getModel().getValue();
		for(int i=0; i<outputsCount; i++) {
			
		}
	}
	
	public String[] getInputNames() {
		int inputColCount = table.getColumnCount() - 1;
		String names[] = new String[inputColCount];
		for(int i=0; i<inputColCount; i++) {
			names[i] = table.getColumnName(i);
		}
		return names;
	}
	
	public int[] getValuesFromTruthTable() {
		int rowCount = table.getRowCount();
		int lastColIndex = table.getColumnCount() - 1;
		int[] data = new int[rowCount];
		for(int i=0;i<rowCount;i++) {
			String val = (String) table.getModel().getValueAt(i, lastColIndex);
			if(val == "0") data[i] = 0;
			else if(val == "1") data[i] = 1;
			else if(val == "x") data[i] = 2;
		}
		return data;
	}
	
	public void createTruthTable(Integer noOfInputs) {
		
		String[] columnNames = new String[noOfInputs+1];
		boolean[] editable = new boolean[noOfInputs+1];
		Class[] types = new Class[noOfInputs+1];
		for(int i=0;i<noOfInputs;i++) {
			columnNames[i] = inputNames[i];
			editable[i] = false;
			types[i] = Integer.class;
		}
		columnNames[noOfInputs] = "out";
		editable[noOfInputs] = false;
		types[noOfInputs] = String.class;
		
		int rowCount = (int) Math.pow(2, noOfInputs);
		String[][] data = new String[rowCount][noOfInputs+1];
		for(int i=0;i<rowCount; i++) {
			for(int j=0;j<noOfInputs;j++) {
				int pow2 = (int) Math.pow(2, noOfInputs-j-1);
				data[i][j] = new Integer((int)Math.signum(i & pow2)).toString();
			}
			data[i][noOfInputs] = "0";
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
				boolean[] columnEditables = editable;
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
		int minwidth = table.getColumnModel().getColumn(0).getMinWidth();
		FontMetrics metrics = table
                .getFontMetrics(table.getFont());
		for(int i=0;i<noOfInputs;i++) {
		table.getColumnModel().getColumn(i).setPreferredWidth(minwidth + metrics.stringWidth(table.getColumnName(i)));
		table.getColumnModel().getColumn(i).setResizable(false);
		}
		
		
		InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke Key1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0);
        KeyStroke Key0 = KeyStroke.getKeyStroke(KeyEvent.VK_0, 0);
        KeyStroke KeyX = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0);
        
        im.put(enterKey, "Action.enter");
        im.put(Key1, "Action.1");
        im.put(Key0, "Action.0");
        im.put(KeyX, "Action.x");
        am.put("Action.enter", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            	if(table.getSelectedRow() < rowCount-1)
            		table.changeSelection(table.getSelectedRow() + 1, noOfInputs, false, false);
            	
            }
        });
        am.put("Action.1", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            	int[] rows = table.getSelectedRows();
            	for(int i=0; i<rows.length; i++) {
            		table.getModel().setValueAt("1", rows[i], noOfInputs);
            	}
            	int lastrow = rows[rows.length-1];
            	if(lastrow < rowCount-1)
            		table.changeSelection(lastrow + 1, noOfInputs, false, false);
            	
            }
        });
        am.put("Action.0", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            	int[] rows = table.getSelectedRows();
            	for(int i=0; i<rows.length; i++) {
            		table.getModel().setValueAt("0", rows[i], noOfInputs);
            	}
            	int lastrow = rows[rows.length-1];
            	if(lastrow < rowCount-1)
            		table.changeSelection(lastrow + 1, noOfInputs, false, false);
            	
            	
            }
        });
        am.put("Action.x", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
            	int[] rows = table.getSelectedRows();
            	for(int i=0; i<rows.length; i++) {
            		table.getModel().setValueAt("x", rows[i], noOfInputs);
            	}
            	int lastrow = rows[rows.length-1];
            	if(lastrow < rowCount-1)
            		table.changeSelection(lastrow + 1, noOfInputs, false, false);
            	
            	
            }
        });
        
        table.changeSelection(0, noOfInputs, false, false);
        scrollPane_inp.setViewportView(createInputList(noOfInputs));
	}

	/**
	 * Create the frame.
	 */
	public FuncBlockWizard() {
		setMinimumSize(new Dimension(520, 200));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530, 445);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		contentPane.add(panel, BorderLayout.NORTH);
		
		JLabel lblNewLabel_2 = new JLabel("Nazwa bloku");
		panel.add(lblNewLabel_2);
		
		textField_1 = new JTextField();
		panel.add(textField_1);
		textField_1.setColumns(16);
		
		JLabel lblNewLabel = new JLabel("Ilość wejść");
		panel.add(lblNewLabel);
		
		spinnerInput = new JSpinner();
		spinnerInput.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				createTruthTable((Integer)spinnerInput.getModel().getValue());
				
			}
		});
		spinnerInput.setModel(new SpinnerNumberModel(4, 1, 16, 1));
		panel.add(spinnerInput);
		
		JLabel lblNewLabel_1 = new JLabel("Ilość wyjść");
		panel.add(lblNewLabel_1);
		
		spinnerOutput = new JSpinner();
		spinnerOutput.setModel(new SpinnerNumberModel(1, 1, 32, 1));
		panel.add(spinnerOutput);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_1.add(splitPane, BorderLayout.CENTER);
		
		scrollPane_inp = new JScrollPane();
		scrollPane_inp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		splitPane.setLeftComponent(scrollPane_inp);
		
		scrollPane_out = new JScrollPane();
		splitPane.setRightComponent(scrollPane_out);
		
		JPanel panel_8 = new JPanel();
		panel_1.add(panel_8, BorderLayout.SOUTH);
		
		JButton btnNewButton_1 = new JButton("New button");
		panel_8.add(btnNewButton_1);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_5.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_3.add(panel_5, BorderLayout.NORTH);
		
		JLabel lblNazwa = new JLabel("Nazwa wyjścia");
		panel_5.add(lblNazwa);
		
		textField = new JTextField();
		panel_5.add(textField);
		textField.setColumns(8);
		
		JPanel panel_6 = new JPanel();
		panel_3.add(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JLabel lblFunkcja = new JLabel(" Funkcja  ");
		panel_6.add(lblFunkcja, BorderLayout.WEST);
		
		funcField = new JTextField();
		funcField.setEditable(false);
		funcField.setMaximumSize(new Dimension(2147483647, 20));
		funcField.setMinimumSize(new Dimension(6, 15));
		panel_6.add(funcField, BorderLayout.CENTER);
		funcField.setColumns(20);
		
		JButton btnNewButton = new JButton("Oblicz");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String func = FuncMinimizer.minimize(getValuesFromTruthTable(), getInputNames());
				funcField.setText(func);
			}
		});
		panel_6.add(btnNewButton, BorderLayout.EAST);
		
		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_4.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		
		scrollPane.setViewportView(table);
		createTruthTable((Integer)spinnerInput.getModel().getValue());
	}

}
