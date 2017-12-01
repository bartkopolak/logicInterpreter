package logicInterpreter.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Tools.AlteraSimItems.RedLED;
import logicInterpreter.Tools.AlteraSimItems.Switch;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTabbedPane;
import java.awt.Dimension;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.awt.event.ActionEvent;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class AlteraSim extends JFrame {

	private JPanel contentPane;
	DiagramBean diagram;
	List<JComboBox<JCheckBox>> selectedInputs = new ArrayList<JComboBox<JCheckBox>>();
	List<JComboBox<JCheckBox>> selectedOutputs = new ArrayList<JComboBox<JCheckBox>>();
	JTabbedPane tabbedPane;
	final List<Switch> switchesList = new ArrayList<Switch>();
	final List<RedLED> redLEDsList = new ArrayList<RedLED>();
	final JCheckBox falseCB  = new JCheckBox() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public String toString() {
			return getName();
		}
		
	};
	final JCheckBox trueCB = new JCheckBox() {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return getName();
		}
		
	};
	final JCheckBox noCB = new JCheckBox() {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return getName();
		}
		
	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AlteraSim frame = new AlteraSim();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	AbstractAction inputChanged = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox source = (JCheckBox) e.getSource();
			boolean connectedInputWasChanged = false;
			for(JComboBox<JCheckBox> input : selectedInputs) {
				JCheckBox inputCB = (JCheckBox) input.getSelectedItem();
				if(inputCB.equals(source)) {
					connectedInputWasChanged = true;
					break;
				}
					
			}
			if(connectedInputWasChanged)
				evaluate();
			
		}
	};
	boolean isUpdating = false;
	
	private void setOutputComboBoxes(JComboBox source) {

		for (JComboBox<JCheckBox> cb : selectedOutputs) {
			if (cb != null && !source.getName().equals(cb.getName())) {
				int selItem = cb.getSelectedIndex();
				cb.setModel(new DefaultComboBoxModel<JCheckBox>(getOutputsList()));
				if(source.getSelectedIndex() == selItem && selItem > 1) selItem = 0;
				isUpdating = true;
				cb.setSelectedIndex(selItem);
				isUpdating = false;
				System.out.println("zmieniono! dla " + cb.getName());
			} else {

			}
		}
		evaluate();
	}
		
	ItemListener outputListListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED && !isUpdating) {
				setOutputComboBoxes((JComboBox)e.getSource());
			}
			
		}
	};
	AbstractAction inputListListener = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			evaluate();
			
		}
	};
	
	private Vector<JCheckBox> getInputsList(){
		Vector<JCheckBox> list = new Vector<JCheckBox>();
		list.add(falseCB);
		list.add(trueCB);
		list.addAll(switchesList);
		return list;	
	}
	
	private Vector<JCheckBox> getOutputsList(){
		Vector<JCheckBox> list = new Vector<JCheckBox>();
		list.add(noCB);
		list.addAll(redLEDsList);
		return list;	
	}
	
	
	private void evaluate() {
		try {
			for(int i = 0; i<diagram.getInputList().size(); i++){
				JComboBox<JCheckBox> inputComboBox = selectedInputs.get(i);
				JCheckBox input = (JCheckBox) inputComboBox.getSelectedItem();
				diagram.getInput(i).setState(input.isSelected());
			}
			diagram.evaluate();
			for(JCheckBox output : getOutputsList()) {
				output.setSelected(false);
			}
			for(int i = 0; i<diagram.getOutputList().size(); i++){
				JComboBox<JCheckBox> outputComboBox = selectedOutputs.get(i);
				JCheckBox output = (JCheckBox) outputComboBox.getSelectedItem();
				output.setSelected(diagram.getOutput(i).getState());
			}
		} catch (RecurrentLoopException e) {
			e.printStackTrace();
		}
		

	}

	private void openDiagram() {
		String sciezka = "";
		boolean fileSelected = true;
		try{
			JFileChooser dialog = new JFileChooser(); //stworzenie okienka dialogowego do wyboru pliku do przeszukania
			FileNameExtensionFilter filterXML = new FileNameExtensionFilter("Plik diagramu XML", "xml"); //filtr
			    dialog.setFileFilter(filterXML); //dodanie filtru do dialogu
			    int returnVal = dialog.showOpenDialog(this); //wyśw. okno dialogowe
			    if(returnVal == JFileChooser.APPROVE_OPTION) { //jeśli uzytkownik otworzy plik (wciśnie przycisk otwórz)
			    	sciezka = dialog.getSelectedFile().getAbsolutePath(); //pobierz ściezke do pliku i zapisz ją do stringa
			    }
			    else
			    	fileSelected = false;
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this, "Nie udało się otworzyć pliku.");
		}
		if(fileSelected) {
			try {
				diagram = XMLparse.parseXMLDiagram(new File(sciezka));
				loadSettings();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(this, "Niepoprawny format diagramu: " + e.getMessage());
			}
			
			
		}
	}
	
	private void loadSettings() {
		if(diagram != null) {
			tabbedPane.removeAll();
			selectedInputs.clear();
			selectedOutputs.clear();
			JPanel inputsPanel = new JPanel();
			JPanel outputsPanel = new JPanel();
			JPanel inputsPanelInner = new JPanel();
			JPanel outputsPanelInner = new JPanel();
			
			inputsPanel.setLayout(new BorderLayout());
			outputsPanel.setLayout(new BorderLayout());
			inputsPanelInner.setLayout(new GridLayout(0, 1, 0, 0));
			outputsPanelInner.setLayout(new GridLayout(0, 1, 0, 0));
			
			this.setTitle("AlteraSim - " + diagram.getName());
			
			for(int i=0; i<diagram.getInputList().size(); i++) {
				DiagramInputBean input = diagram.getInput(i);
				JPanel inpUnitPanel = new JPanel();
				JLabel inputLabel = new JLabel(input.getName() + ":");
				JComboBox<JCheckBox> inputList = new JComboBox<JCheckBox>();
				selectedInputs.add(inputList);
				inputList.addActionListener(inputListListener);
				inputList.setModel(new DefaultComboBoxModel<JCheckBox>(getInputsList()));
				inputList.setName(input.getName());
				inpUnitPanel.add(inputLabel);
				inpUnitPanel.add(inputList);
				inputsPanelInner.add(inpUnitPanel);
			}
			inputsPanel.add(inputsPanelInner, BorderLayout.WEST);
			
			for(int i=0; i<diagram.getOutputList().size(); i++) {
				DiagramOutputBean output = diagram.getOutput(i);
				JPanel outUnitPanel = new JPanel();
				JLabel outputLabel = new JLabel(output.getName() + ":");
				JComboBox<JCheckBox> outputList = new JComboBox<JCheckBox>();
				selectedOutputs.add(outputList);
				outputList.addItemListener(outputListListener);
				outputList.setModel(new DefaultComboBoxModel<JCheckBox>(getOutputsList()));
				outputList.setName(output.getName());
				outUnitPanel.add(outputLabel);
				outUnitPanel.add(outputList);
				outputsPanelInner.add(outUnitPanel);
			}
			outputsPanel.add(outputsPanelInner, BorderLayout.WEST);
			
			tabbedPane.addTab("Wejścia", null, inputsPanel, null);
			tabbedPane.addTab("Wyjścia", null, outputsPanel, null);
		}
		
	}
	
	/**
	 * Create the frame.
	 */
	public AlteraSim() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		falseCB.setSelected(false);
		falseCB.setName("stan logiczny 0");

		trueCB.setSelected(true);
		trueCB.setName("stan logiczny 1");

		noCB.setName("brak wyjścia");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 686, 531);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnPlik = new JMenu("Plik");
		menuBar.add(mnPlik);
		
		JMenuItem mntmOtwrz = new JMenuItem("Otwórz");
		mntmOtwrz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openDiagram();
			}
		});
		mnPlik.add(mntmOtwrz);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel switchLedPanel = new JPanel();
		panel_1.add(switchLedPanel, BorderLayout.WEST);
		switchLedPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		switchLedPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		JPanel optionsPanel = new JPanel();
		contentPane.add(optionsPanel, BorderLayout.NORTH);
		optionsPanel.setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		optionsPanel.add(tabbedPane);
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(600, 10));
		contentPane.add(panel, BorderLayout.WEST);
		panel.setLayout(null);
		
		
		for(int i = 0; i<18; i++) {
			
			JPanel switchLedPanelUnit = new JPanel();
			switchLedPanelUnit.setLayout(new BorderLayout(10,10));
			
			
		
			
			RedLED redLED = new RedLED();
			redLED.setName("LEDR" + String.valueOf(17-i));
			redLED.setToolTipText("LEDR" + String.valueOf(17-i));
			switchLedPanelUnit.add(redLED, BorderLayout.NORTH);
			redLEDsList.add(redLED);
			
			
			Switch switchComp = new Switch();
			switchComp.setName("SW" + String.valueOf(17-i));
			switchComp.setToolTipText("SW" + String.valueOf(17-i));
			switchComp.addActionListener(inputChanged);
			switchLedPanelUnit.add(switchComp, BorderLayout.CENTER);
			switchesList.add(switchComp);
			
			switchLedPanel.add(switchLedPanelUnit);
		}
			
		
		//TEST
		try {
			diagram = XMLparse.parseXMLDiagram(new File("xmls/diagram.xml"));
			loadSettings();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
