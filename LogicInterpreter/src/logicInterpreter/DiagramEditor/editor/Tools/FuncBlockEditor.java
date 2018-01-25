package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.Dialog;
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
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.DefaultFileFilter;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Tools.DiagFileUtils;

import javax.swing.JScrollPane;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

public class FuncBlockEditor extends JDialog {

	private JPanel contentPane;
	private JScrollPane scrollPane_inp;
	private JScrollPane scrollPane_out;
	private JTable table;
	private JTextField textField;
	private JTextField funcField;
	private JSpinner spinnerInput;
	private JSpinner spinnerOutput;
	private JTable table_1;
	private BlockBean block = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//BlockBean blok = DiagFileUtils.parseXMLBlock(new File("xmls/binto7sd.xml"));
					FuncBlockEditor frame = new FuncBlockEditor();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	String[] inputNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
	final int maxInputs= 12;
	final int maxOutputs = 32;
	String[] outputNames = new String[maxOutputs];
	String[] outputData = new String[maxOutputs];
	private JTextField textField_1;
	private boolean changeXML = true;
	
	ArrayList<JRadioButton> outputList = new ArrayList<JRadioButton>();
	
	public JPanel createInputList(int inputsCount) {
		JPanel panel_Inputs = new JPanel();
		panel_Inputs.setBorder(new TitledBorder(null, "Wej\u015Bcia", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Inputs.setLayout(new GridBagLayout());
		panel_Inputs.removeAll();
		panel_Inputs.invalidate();
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.gridx = 0;
		c.weighty = 0;
		
		for(int i=0; i<inputsCount; i++) {
			JTextField field = new JTextField();
			int pos = i;
			field.setText(inputNames[i]);
			field.setColumns(8);
			field.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if(field.getText().isEmpty()) {
						showMessage("empty");
						field.setText(inputNames[pos]);
						return;
					}
					for(int i=0; i<(Integer)spinnerInput.getModel().getValue(); i++) {
						if(inputNames[i].equals(field.getText())) {
							
							showMessage("inputnotunique");
							field.setText(inputNames[pos]);
							return;
						}
							
					}
						inputNames[pos] = field.getText();
						createTruthTable(inputsCount);
					
				}
			});
			panel_Inputs.add(field, c);
			
			
		}
		c.weighty = 1;
		panel_Inputs.add(Box.createVerticalGlue(), c);
		c.weighty = 0;
		panel_Inputs.repaint();
		return panel_Inputs;
	}
	
	private int outputEditing = -1;
	public JPanel createOutputList() {
		JPanel panel_Outputs = new JPanel();
		panel_Outputs.setBorder(new TitledBorder(null, "Wyj\u015Bcia", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Outputs.setLayout(new BoxLayout(panel_Outputs, BoxLayout.Y_AXIS));
		panel_Outputs.removeAll();
		panel_Outputs.invalidate();
		ButtonGroup group = new ButtonGroup();
		Integer outputsCount = (Integer) spinnerOutput.getModel().getValue();
		for(int i=0; i<outputsCount; i++) {
			JRadioButton radiobtn = new JRadioButton();
			int pos = i;
			int e = 0;
			if(outputNames[i] == null) {
				String s = "X" + String.valueOf(i);
				if(Arrays.asList(outputNames).contains(s)) {
					do {
					s = "X" + String.valueOf(e++);
					}
					while(Arrays.asList(outputNames).contains(s));
				}
					
				outputNames[i] = s;
			}
				
			radiobtn.setText(outputNames[i]);
			radiobtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					saveTruthTableValues(outputEditing);
					outputEditing = pos;
					createTruthTable((Integer)spinnerInput.getModel().getValue());
					textField.setText(outputNames[outputEditing]);
					funcField.setText("");
					
					
				}
			});
			group.add(radiobtn);
			panel_Outputs.add(radiobtn);
			panel_Outputs.repaint();
			if(outputEditing > outputsCount-1 && i == outputsCount - 1 || outputEditing == -1)
			{
				
				radiobtn.setSelected(true);
				outputEditing = i;
				
			}
				
			if(outputEditing == i) {
				radiobtn.setSelected(true);
			}
				
		}
		return panel_Outputs;
	}
	
	public String[] getInputNames() {
		int inputColCount = table.getColumnCount() - 1;
		String names[] = new String[inputColCount];
		for(int i=0; i<inputColCount; i++) {
			names[i] = table.getColumnName(i);
		}
		return names;
	}
	
	public static int[] getIntValuesFromTruthTable(JTable table) {
		int rowCount = table.getRowCount();
		int lastColIndex = table.getColumnCount() - 1;
		int[] valdata = new int[rowCount];
		for(int i=0;i<rowCount;i++) {
			String val = (String) table.getModel().getValueAt(i, lastColIndex);
			if(val.equals("0")) valdata[i] = 0;
			else if(val.equals("1")) valdata[i] = 1;
			else if(val.equals("x")) valdata[i] = 2;
		}
		return valdata;
	}
	
	public int[] getIntValuesFromOutputDataTable(String data) {
		
		int[] valdata = new int[data.length()];
		for(int i=0;i<data.length();i++) {
			String val = String.valueOf(data.charAt(i));
			if(val.equals("0")) valdata[i] = 0;
			else if(val.equals("1")) valdata[i] = 1;
			else if(val.equals("x")) valdata[i] = 2;
		}
		return valdata;
	}
	
	public void setTruthTableValues(int pos) {
		int rowCount = table.getRowCount();
		int lastColIndex = table.getColumnCount() - 1;
		String data = outputData[pos];
		int dataLen = data.length();
		for(int i=0;i<rowCount;i++) {
			String val;
			if(i < dataLen) val = String.valueOf(data.charAt(i));
			else val = "0";
			table.getModel().setValueAt(val, i, lastColIndex);
		}
	}
	
	
	public void saveTruthTableValues(int pos) {
		int rowCount = table.getRowCount();
		int lastColIndex = table.getColumnCount() - 1;
		String data = new String();
		for(int i=0;i<rowCount;i++) {
			String val = (String) table.getModel().getValueAt(i, lastColIndex);
			data += val;
		}
		outputData[pos] = data;
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
		columnNames[noOfInputs] = outputNames[outputEditing];
		editable[noOfInputs] = false;
		types[noOfInputs] = String.class;
		
		int rowCount = (int) Math.pow(2, noOfInputs);
		String[][] data = new String[rowCount][noOfInputs+1];
		for(int i=0;i<rowCount; i++) {
			for(int j=0;j<noOfInputs;j++) {
				int pow2 = (int) Math.pow(2, noOfInputs-j-1);
				data[i][j] = new Integer((int)Math.signum(i & pow2)).toString();
			}
			//data[i][noOfInputs] = "0";
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
        setTruthTableValues(outputEditing);
	}

	public void showMessage(String code) {
		if(code.equals("inputnotunique")) JOptionPane.showMessageDialog(this, "Wpisz unikalną nazwę wejścia!", "Błąd", JOptionPane.OK_OPTION);
		else if(code.equals("outputnotunique")) JOptionPane.showMessageDialog(this, "Wpisz unikalną nazwę wyjścia!", "Błąd", JOptionPane.OK_OPTION);
		else if(code.equals("empty")) JOptionPane.showMessageDialog(this, "Wpisz nazwę!", "Błąd", JOptionPane.OK_OPTION);
		else if(code.equals("saveError")) JOptionPane.showMessageDialog(this, "Błąd zapisu!", "Błąd", JOptionPane.OK_OPTION);
		else if(code.equals("wrongBlock")) JOptionPane.showMessageDialog(this, "Dany blok nie jest typu funkcyjnego", "Błąd", JOptionPane.OK_OPTION);
	}

	/**
	 * Create the frame.
	 * @wbp.parser.constructor
	 */
	public FuncBlockEditor() {
		setMinimumSize(new Dimension(520, 200));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 530, 445);
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
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
		textField_1.setColumns(14);
		
		JLabel lblNewLabel = new JLabel("Ilość wejść");
		panel.add(lblNewLabel);
		
		spinnerInput = new JSpinner();
		spinnerInput.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				saveTruthTableValues(outputEditing);
				scrollPane_inp.setViewportView(createInputList((Integer)spinnerInput.getModel().getValue()));
				createTruthTable((Integer)spinnerInput.getModel().getValue());
				funcField.setText("");
				
			}
		});
		spinnerInput.setModel(new SpinnerNumberModel(4, 1, maxInputs, 1));
		panel.add(spinnerInput);
		
		JLabel lblNewLabel_1 = new JLabel("Ilość wyjść");
		panel.add(lblNewLabel_1);
		
		spinnerOutput = new JSpinner();
		spinnerOutput.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				saveTruthTableValues(outputEditing);
				scrollPane_inp.setViewportView(createInputList((Integer)spinnerInput.getModel().getValue()));
		        scrollPane_out.setViewportView(createOutputList());
				createTruthTable((Integer)spinnerInput.getModel().getValue());
				textField.setText(outputNames[outputEditing]);
			}
			
		});
		spinnerOutput.setModel(new SpinnerNumberModel(1, 1, maxOutputs, 1));
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
		
		JButton saveBtn = new JButton("Zapisz");
		saveBtn.setActionCommand("OK");
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File p;
				try {
					if(block != null) {
					
					p = block.getFile();
					
					if(!p.exists())
						p.createNewFile();
							
					save(p, changeXML);
					
				}
				else {
					p = new File("xmls/"+textField_1.getText()+".tmpb");
					if(!p.exists()) {
						p.createNewFile();
						save(p, true);
					
					}
					else {
						if(JOptionPane.showConfirmDialog(null, "Blok o podanej nazwie istnieje. Nadpisać?", 
								"Uwaga", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							save(p, true);
						}
					}
				}
					dispose();
			}catch (IOException | ParserConfigurationException | TransformerException e) {
				showMessage("saveError");
				return;
			}catch (MultipleOutputsInInputException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Błędny diagram", JOptionPane.ERROR_MESSAGE, null);
				e.printStackTrace();
				return;
			}
				
				
			}
		});
		panel_8.add(saveBtn);
		
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
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(textField.getText().isEmpty()) {
					showMessage("empty");
					textField.setText(outputNames[outputEditing]);
					return;
				}
				if(!Arrays.asList(outputNames).contains(textField.getText())) {
					outputNames[outputEditing] = textField.getText();
					scrollPane_out.setViewportView(createOutputList());
					createTruthTable((Integer)spinnerInput.getModel().getValue());
				}
				else {
					showMessage("outputnotunique");
				}
				
			}
		});
		panel_5.add(textField);
		textField.setColumns(8);
		
		JPanel panel_6 = new JPanel();
		panel_3.add(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JLabel lblFunkcja = new JLabel(" Funkcja  ");
		panel_6.add(lblFunkcja, BorderLayout.WEST);
		
		funcField = new JTextField();
		funcField.setEditable(false);
		funcField.setMinimumSize(new Dimension(6, 15));
		panel_6.add(funcField, BorderLayout.CENTER);
		funcField.setColumns(20);
		
		JButton btnNewButton = new JButton("Pokaż funkcję");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String func = FuncMinimizer.minimize(getIntValuesFromTruthTable(table), getInputNames(),true);
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
		
		for(int i=0;i<maxOutputs;i++) {
			outputData[i] = "";
		}
		
		scrollPane.setViewportView(table);
		scrollPane_inp.setViewportView(createInputList((Integer)spinnerInput.getModel().getValue()));
		scrollPane_out.setViewportView(createOutputList());
		createTruthTable((Integer)spinnerInput.getModel().getValue());
		textField.setText(outputNames[outputEditing]);
	}
	
	public FuncBlockEditor(BlockBean block, boolean changeTemplate) {
		
		this();
		changeXML = changeTemplate;
		if(block.getType().equals("formula")) {
			this.block = block;
			//nazwy wejsc
			for (int i =0;i<block.getInputList().size();i++) {
				inputNames[i] = block.getInput(i).getName();
			}
			spinnerInput.setValue(block.getInputList().size());
			//ustal tablice prawdy
			
				for (int i =0;i<block.getOutputList().size();i++) {
					try {
						int tt[] = block.getTruthTable(block.getOutput(i));
						String out = "";
						for(int j=0;j<tt.length;j++) {
							out += String.valueOf(tt[j]);
						}
						outputData[i] = out;
					} catch (RecurrentLoopException e) {
						outputData[i] = "xxx";
					}
				}
				setTruthTableValues(0);
			
			
			
			//nazwy wyjsc
			for (int i =0;i<block.getOutputList().size();i++) {
				outputNames[i] = block.getOutput(i).getName();
			}
			
			spinnerOutput.setValue(block.getOutputList().size());
			//nazwa bloku
			textField_1.setText(block.getName());
		}
		else {
			showMessage("wrongBlock");
		}
	}
	
	
	
	public void save(File targetFile, boolean changeXML) throws IOException, ParserConfigurationException, TransformerException, MultipleOutputsInInputException{
		int inputsNo = (Integer)spinnerInput.getModel().getValue();
		int outputsNo = (Integer)spinnerOutput.getModel().getValue();
		saveTruthTableValues(outputEditing);
		if(block == null) {
			block = new BlockBean();
		}
			block.setName(textField_1.getText());
			block.setType("formula");
			block.getInputList().clear();
			block.getOutputList().clear();
			for (int i =0;i<inputsNo;i++) {
				block.addInput(inputNames[i], null);//TODO: okreslanie pozycji pinow
			}
			for (int i =0;i<outputsNo;i++) {
				block.addOutput(outputNames[i], FuncMinimizer.minimize(getIntValuesFromOutputDataTable(outputData[i]), getInputNames(),false));
			}
		
		
		if(changeXML) {
				FileOutputStream out = new FileOutputStream(targetFile);
				DiagFileUtils.createTemplateBlockFile(block, null, block.getName(), out);
			
			
		}
		
	}
	
	


	

}
