package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.DiagramEditor.editor.DigitalCircuitEditor;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.GreenLED;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.PinBind;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.PushButton;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.RedLED;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.SevenSegmentDisplay;
import logicInterpreter.DiagramEditor.editor.Tools.AlteraSimItems.Switch;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.LogicElementsModels.CircuitSchemaBean;
import logicInterpreter.LogicElementsModels.Nodes.BlockInputBean;
import logicInterpreter.LogicElementsModels.Nodes.BlockOutputBean;
import logicInterpreter.LogicElementsModels.Nodes.DiagramInputBean;
import logicInterpreter.LogicElementsModels.Nodes.DiagramOutputBean;
import logicInterpreter.LogicElementsModels.Nodes.GNDNode;
import logicInterpreter.LogicElementsModels.Nodes.VCCNode;
import logicInterpreter.Tools.DiagFileUtils;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;

public class AlteraSim extends JFrame {

	private LogSimDebugger debugger;
	private JPanel contentPane;
	CircuitSchemaBean diagram;
	List<JComboBox<JCheckBox>> selectedInputs = new ArrayList<JComboBox<JCheckBox>>();	//wybrana metoda wejscia na plytce z listy combobox
	List<JComboBox<JCheckBox>> selectedOutputs = new ArrayList<JComboBox<JCheckBox>>(); //wybrana metoda wyjscia na plytce z listy combobox
	JTabbedPane tabbedPane;
	JSlider clockSpeedSlider;
	JCheckBox onOffClock;
	JPanel clockSettingsPanel;
	private DigitalCircuitEditor editor;
	
	final List<Switch> switchesList = new ArrayList<Switch>();
	final List<PushButton> pushButtonList = new ArrayList<PushButton>();
	final List<RedLED> redLEDsList = new ArrayList<RedLED>();
	final List<GreenLED> greenLEDsList = new ArrayList<GreenLED>();
	final List<SevenSegmentDisplay> HEXDisplayList = new ArrayList<SevenSegmentDisplay>();
	
	private boolean falseIsHighState = false;
	
	private ArrayList<DiagramInputBean> inputList = new ArrayList<DiagramInputBean>();
	
	final JCheckBox falseCB  = new JCheckBox() { //wejscie logiczne 0

		private static final long serialVersionUID = 1L;
		
		@Override
		public String toString() {
			return getName();
		}
		
	};
	final JCheckBox trueCB = new JCheckBox() {	//wejscie logiczne 1

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return getName();
		}
		
	};
	
	final JCheckBox clockCB = new JCheckBox() {	//wejscie logiczne zegara

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return getName();
		}
		
	};
	
	final JCheckBox noCB = new JCheckBox() { //wejscie logiczne puste - void

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return getName();
		}
		
	};
	
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
				cb.setModel(new DefaultComboBoxModel<JCheckBox>(getBoardOutputsList()));
				if(source.getSelectedIndex() == selItem && selItem > 0) selItem = 0;
				isUpdating = true;
				cb.setSelectedIndex(selItem);
				isUpdating = false;
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
	

	
	private void setClockPanelVisibility() {
		boolean show = false;
		for(JComboBox<JCheckBox> inputCB : selectedInputs) {
			JComboBox<JCheckBox> source = inputCB;
			JCheckBox cb = (JCheckBox) source.getSelectedItem();
			if(cb.equals(clockCB)) {
				show = true;
			}
			
		}
		clockSettingsPanel.setVisible(show);
	}
	
	AbstractAction inputListListener = new AbstractAction() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			setClockPanelVisibility();
			evaluate();
		}
	};
	

	boolean clockSpeedSliderChanging = false;	

	MouseAdapter sliderMouseAdapter = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			clockSpeedSliderChanging = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			clockSpeedSliderChanging = false;
		}
		
	};
	
	MouseAdapter buttonPressed = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			JCheckBox src = (JCheckBox) e.getSource();
			src.setSelected(true);
			evaluate();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			JCheckBox src = (JCheckBox) e.getSource();
			src.setSelected(false);
			evaluate();
			
		}
		
	};
	
	private void clock() {
		try {

			if(onOffClock != null && !clockSpeedSliderChanging) {
				if(onOffClock.isSelected()) {	
					clockCB.setSelected(!clockCB.isSelected());
					//System.out.println(clockCB.isSelected());
					evaluate();
					
				}
			}
			Thread.sleep((10 * clockSpeedSlider.getValue())/2);
		}
		catch (InterruptedException e) {
				
		}
		catch(NullPointerException e) {
			
		}

	}
	boolean simRunning = true;
	Thread clockThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while(simRunning) {
				clock();
			}	
		}
	});

	private JLabel hertzLabel;
	/**
	 * Zbierz wszystkie wejścia płytki do 1 listy
	 * @return
	 */
	private Vector<JCheckBox> getBoardInputsList(){
		Vector<JCheckBox> list = new Vector<JCheckBox>();
		list.add(falseCB);
		list.add(trueCB);
		list.add(clockCB);
		list.addAll(switchesList);
		list.addAll(pushButtonList);
		return list;	
	}
	/**
	 * Zbierz wszystkie wyjścia płytki do 1 listy
	 * @return
	 */
	private Vector<JCheckBox> getBoardOutputsList(){
		Vector<JCheckBox> list = new Vector<JCheckBox>();
		list.add(noCB);
		list.addAll(redLEDsList);
		list.addAll(greenLEDsList);
		for(SevenSegmentDisplay hex: HEXDisplayList) {
			list.addAll(hex.getSegmentList());
		}
		
		return list;	
	}
	
	/**
	 * Przetwórz układ, których stany wejść są stanami CheckBoxów powiązanych z danym wejściem<br>
	 * Następnie ustaw stany wyjść płytki powiązanych z wyjściami układu.
	 */
	private void evaluate() {
		try {
			for(int i = 0; i<inputList.size(); i++){
				JComboBox<JCheckBox> inputComboBox = selectedInputs.get(i);
				JCheckBox input = (JCheckBox) inputComboBox.getSelectedItem();
				
				diagram.getInput(inputList.get(i).getName()).setState(new ThreeStateBoolean(input.isSelected()));
			}
			diagram.evaluate();
			debugger.colorEdges();
			for(JCheckBox output : getBoardOutputsList()) {
				output.setSelected(false);
			}
			for(int i = 0; i<diagram.getOutputList().size(); i++){
				JComboBox<JCheckBox> outputComboBox = selectedOutputs.get(i);
				JCheckBox output = (JCheckBox) outputComboBox.getSelectedItem();
				output.setSelected(diagram.getOutput(i).getState().toBoolean() ^ falseIsHighState);
			}
		} catch (RecurrentLoopException e) {
			e.printStackTrace();
		}
		

	}

	private String lastPath = "";
	
	private void loadPinBinds(List<PinBind> listOfBinds) {
		if(listOfBinds == null) {
			JOptionPane.showMessageDialog(this, "Nie znaleziono wcześniej zapisanych ustawień pinów!");
			setTitle("AlteraSim - brak zapisanych ustawień pinów");
			return;
		}
		for(PinBind bind : listOfBinds) {
			String[] pinname = bind.getNodeName().split("[.]");
			if(pinname[0].equals("inputs")) {
				JComboBox<JCheckBox> inputComboBox = null;
				for(int i=0; i<inputList.size(); i++) {
					DiagramInputBean input= inputList.get(i);
					if(input.getName().equals(pinname[1])) {
						inputComboBox = selectedInputs.get(i);
						break;
					}
				}
				if(inputComboBox != null) {
					inputComboBox.setSelectedIndex(bind.getBoardElemIndex());
				}
			}
			if(pinname[0].equals("outputs")) {
				JComboBox<JCheckBox> outputComboBox = null;
				for(int i=0; i<diagram.getOutputList().size(); i++) {
					DiagramOutputBean output = diagram.getOutputList().get(i);
					if(output.getName().equals(pinname[1])) {
						outputComboBox = selectedOutputs.get(i);
						break;
					}
				}
				if(outputComboBox != null) {
					outputComboBox.setSelectedIndex(bind.getBoardElemIndex());
				}
			}
		}
		setTitle("AlteraSim - wczytano zapisane ustawienia pinów");
		
	}
	
	private ArrayList<PinBind> savePinBinds(){
		ArrayList<PinBind> list = new ArrayList<PinBind>();
		for(int i = 0; i<inputList.size(); i++){
			JComboBox<JCheckBox> inputComboBox = selectedInputs.get(i);
			DiagramInputBean input= inputList.get(i);
			PinBind bind = new PinBind(input.toString(), inputComboBox.getSelectedIndex());
			list.add(bind);
		}
		for(int i = 0; i<diagram.getOutputList().size(); i++){
			JComboBox<JCheckBox> outputComboBox = selectedOutputs.get(i);
			DiagramOutputBean output = diagram.getOutputList().get(i);
			PinBind bind = new PinBind(output.toString(), outputComboBox.getSelectedIndex());
			list.add(bind);
		}
		return list;
	}
	
	private void loadSettings() {
		if(diagram != null) {
			this.setTitle("AlteraSim");
			tabbedPane.removeAll();
			selectedInputs.clear();
			selectedOutputs.clear();
			JPanel inputsPanel = new JPanel();
			JPanel outputsPanel = new JPanel();
			JPanel inputsPanelInner = new JPanel();
			JPanel outputsPanelInner = new JPanel();
			
			inputsPanel.setLayout(new BorderLayout());
			outputsPanel.setLayout(new BorderLayout());
			
			Dimension maxSize = new Dimension(0,220);
			
			JScrollPane inputsScrollPane = new JScrollPane();
			inputsScrollPane.setViewportView(inputsPanelInner);
			inputsScrollPane.setPreferredSize(maxSize);
			JScrollPane outputsScrollPane = new JScrollPane();
			outputsScrollPane.setViewportView(outputsPanelInner);
			outputsScrollPane.setPreferredSize(maxSize);
			inputsPanelInner.setLayout(new GridBagLayout());
			outputsPanelInner.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTHWEST;
			c.weightx = 1;
			c.gridx = 0;
			c.weighty = 0;
			
			for(int i=0; i<diagram.getInputList().size(); i++) {
				DiagramInputBean input = diagram.getInput(i);
				if(!(input instanceof VCCNode) && !(input instanceof GNDNode)) {
					this.inputList.add(input);
					JPanel inpUnitPanel = new JPanel();
					inpUnitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
					JLabel inputLabel = new JLabel(input.getName() + ":");
					JComboBox<JCheckBox> inputList = new JComboBox<JCheckBox>();
					selectedInputs.add(inputList);
					inputList.addActionListener(inputListListener);
					inputList.setModel(new DefaultComboBoxModel<JCheckBox>(getBoardInputsList()));
					inputList.setName(input.getName());
					inpUnitPanel.add(inputLabel);
					inpUnitPanel.add(inputList);
					inputsPanelInner.add(inpUnitPanel, c);
				}
				
			}
			
			
			clockSettingsPanel = new JPanel();
			clockSettingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			onOffClock = new JCheckBox("Włącz zegar");
			clockSpeedSlider = new JSlider();
			clockSpeedSlider.setMaximum(500);	//najwieksza szybkosc - 10ms
			clockSpeedSlider.setMinimum(1); //majmniejsza szybkosc ma byc 5s
			clockSpeedSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					hertzLabel.setText(String.valueOf((double)((10*clockSpeedSlider.getValue())/1000.0)) + " Hz");
				}	
			});
			clockSpeedSlider.addMouseListener(sliderMouseAdapter);
			hertzLabel = new JLabel("");
			hertzLabel.setText(String.valueOf((double)((10*clockSpeedSlider.getValue())/1000.0)) + " Hz");
			clockSettingsPanel.add(onOffClock);
			clockSettingsPanel.add(clockSpeedSlider);
			clockSettingsPanel.add(hertzLabel);
			inputsPanelInner.add(clockSettingsPanel, c);
			clockSettingsPanel.setVisible(false);
			
			c.weighty = 1;
			inputsPanelInner.add(Box.createGlue(), c);
			c.weighty = 0;
			inputsPanel.add(inputsScrollPane, BorderLayout.CENTER);
			
			for(int i=0; i<diagram.getOutputList().size(); i++) {
				DiagramOutputBean output = diagram.getOutput(i);
				JPanel outUnitPanel = new JPanel();
				outUnitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				JLabel outputLabel = new JLabel(output.getName() + ":");
				JComboBox<JCheckBox> outputList = new JComboBox<JCheckBox>();
				selectedOutputs.add(outputList);
				outputList.addItemListener(outputListListener);
				outputList.setModel(new DefaultComboBoxModel<JCheckBox>(getBoardOutputsList()));
				outputList.setName(output.getName());
				outUnitPanel.add(outputLabel);
				outUnitPanel.add(outputList);
				outputsPanelInner.add(outUnitPanel,c);
			}
			outputsPanel.add(outputsScrollPane, BorderLayout.CENTER);
			c.weighty = 1;
			outputsPanelInner.add(Box.createGlue(), c);
			c.weighty = 0;
			tabbedPane.addTab("Wejścia", null, inputsPanel, null);
			tabbedPane.addTab("Wyjścia", null, outputsPanel, null);
		}
		
	}
	
	private void close() {
		simRunning = false;
		debugger.close();
		dispose();
	}
	/**
	 * Create the frame.
	 */
	public AlteraSim() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} 
		
		falseCB.setSelected(false);
		falseCB.setName("stan logiczny 0");

		trueCB.setSelected(true);
		trueCB.setName("stan logiczny 1");

		noCB.setName("brak wyjścia");
		clockCB.setName("zegar");

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				close();
			}
			
		});
		setBounds(100, 100, 686, 531);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnPlik = new JMenu("Plik");
		menuBar.add(mnPlik);
		
		JMenuItem mntmSaveSett = new JMenuItem("Zapisz ustawienia pinów");
		mntmSaveSett.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				editor.setAlteraSimPinBinds(savePinBinds());
				editor.setModified(true);
				setTitle("AlteraSim - wczytano zapisane ustawienia pinów");
			}
		});
		mnPlik.add(mntmSaveSett);
		JMenuItem mntmLoadSett = new JMenuItem("Wczytaj ustawienia pinów");
		mntmLoadSett.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadPinBinds(editor.getAlteraSimPinBinds());
			}
		});
		mnPlik.add(mntmLoadSett);
		mnPlik.addSeparator();
		JMenuItem mntmClearSett = new JMenuItem("Skasuj ustawienia pinów");
		mntmClearSett.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retVal = JOptionPane.showConfirmDialog(null, "Czy na pewno chcesz skasować zapisane ustawienia pinów?", "AlteraSim", JOptionPane.YES_NO_OPTION);
				if(retVal == JOptionPane.YES_OPTION)
					editor.setAlteraSimPinBinds(null);
					editor.setModified(true);
					setTitle("AlteraSim - brak zapisanych ustawień pinów");
			}
		});
		mnPlik.add(mntmClearSett);
		
		JMenu mnSettings = new JMenu("Ustawienia");
		menuBar.add(mnSettings);
		
		JCheckBoxMenuItem mntmSett = new JCheckBoxMenuItem("Konwencja ujemna", false);
		mntmSett.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				falseIsHighState = mntmSett.isSelected();
				evaluate();
			}
		});
		mnSettings.add(mntmSett);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel switchLedPanelLeft = new JPanel();
		panel_1.add(switchLedPanelLeft, BorderLayout.WEST);
		switchLedPanelLeft.setBorder(new EmptyBorder(15, 15, 15, 15));
		switchLedPanelLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		JPanel switchLedPanelRight = new JPanel();
		panel_1.add(switchLedPanelRight, BorderLayout.EAST);
		switchLedPanelRight.setBorder(new EmptyBorder(15, 15, 15, 15));
		switchLedPanelRight.setLayout(new BorderLayout(0, 0));
		
		JPanel hexAndLedsPanel = new JPanel();
		panel_1.add(hexAndLedsPanel, BorderLayout.NORTH);
		hexAndLedsPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel hexPanel = new JPanel();
		hexPanel.setBorder(new EmptyBorder(15, 15, 0, 15));
		hexAndLedsPanel.add(hexPanel, BorderLayout.WEST);
		for(int i=7; i>=0; i--) {
			SevenSegmentDisplay HEX = new SevenSegmentDisplay();
			HEX.setName("HEX"+String.valueOf(i));
			HEX.setToolTipText(HEX.getName());
			hexPanel.add(HEX);
			HEXDisplayList.add(HEX);
			if(i == 6){ //separator
				hexPanel.add(Box.createRigidArea(new Dimension(20,5)));
			}
			if(i == 4){ //zielona dioda led
				hexPanel.add(Box.createRigidArea(new Dimension(5,5)));
				GreenLED greenLED = new GreenLED();
				greenLED.setName("LEDG8");
				greenLED.setToolTipText("LEDG8");
				hexPanel.add(greenLED);
				greenLEDsList.add(greenLED);
				hexPanel.add(Box.createRigidArea(new Dimension(5,5)));
			}
		}		
		
		JPanel optionsPanel = new JPanel();
		contentPane.add(optionsPanel, BorderLayout.NORTH);
		optionsPanel.setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		optionsPanel.add(tabbedPane);
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(600, 10));
		contentPane.add(panel, BorderLayout.WEST);
		panel.setLayout(null);
		
		//panel 
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
			
			switchLedPanelLeft.add(switchLedPanelUnit);
		}
		JPanel greenLedsPanel = new JPanel();
		greenLedsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 13, 2));
		switchLedPanelRight.add(greenLedsPanel, BorderLayout.NORTH);
		for(int i= 7; i>=0; i--){
			GreenLED greenLED = new GreenLED();
			greenLED.setName("LEDG" + String.valueOf(i));
			greenLED.setToolTipText("LEDG" + String.valueOf(i));
			greenLedsPanel.add(greenLED);
			greenLEDsList.add(greenLED);
		}
		
		JPanel pushButtonPanel = new JPanel();
		pushButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		switchLedPanelRight.add(pushButtonPanel, BorderLayout.CENTER);
		for(int i = 3; i>=0; i--){
			PushButton pushButton = new PushButton();
			pushButton.setName("KEY" + String.valueOf(i));
			pushButton.setToolTipText("KEY" + String.valueOf(i));
			pushButton.addMouseListener(buttonPressed);
			pushButtonList.add(pushButton);
			pushButtonPanel.add(pushButton);
			
		}
			
		
		//TEST
		clockThread.start();
		

	}
	
	public AlteraSim(DigitalCircuitEditor ge) throws MultipleOutputsInInputException {
		this();
		diagram = ge.createDiagram();
		debugger = new LogSimDebugger(ge, diagram);
		editor = ge;
		loadSettings();
		if(ge.getAlteraSimPinBinds() != null) {
			loadPinBinds(ge.getAlteraSimPinBinds());
		}
		else {
			setTitle("AlteraSim - brak zapisanych ustawień pinów");
		}
	}
}
