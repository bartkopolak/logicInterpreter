package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import logicInterpreter.DiagramEditor.editor.DigitalCircuitEditor;
import logicInterpreter.Exceptions.MultipleOutputsInInputException;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.LogicElementsModels.BlockBean;
import logicInterpreter.LogicElementsModels.CircuitSchemaBean;
import logicInterpreter.Tools.DiagFileUtils;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JRadioButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class TemplateBlockWizard extends JFrame {

	
	
	
	private JPanel contentPane;
	private JTextField textFieldNameOfBlock;
	private JButton btnBack;
	private JButton btnNext;
	private CardLayout cl;
	private JList list;
	
	
	private String blockName = "";
	private String pathName = "";
	private String type;
	private JPanel mainpanel;

	private DigitalCircuitEditor editor;
	private CircuitSchemaBean diagram;
	private String palettePath = null;
	/**
	 * Launch the application.
	 */
	private JPanel panelNameOfBlock() {
		JPanel panel_name = new JPanel();
		panel_name.setLayout(null);
		
		JLabel lblNazwaBloku = new JLabel("Nazwa bloku");
		lblNazwaBloku.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNazwaBloku.setBounds(10, 11, 137, 22);
		panel_name.add(lblNazwaBloku);
		
		JLabel lblPodajNazwBloku = new JLabel("Podaj nazwę nowego bloku");
		lblPodajNazwBloku.setBounds(10, 46, 243, 14);
		panel_name.add(lblPodajNazwBloku);
		
		textFieldNameOfBlock = new JTextField();
		textFieldNameOfBlock.setBounds(10, 71, 300, 20);
		panel_name.add(textFieldNameOfBlock);
		textFieldNameOfBlock.setColumns(10);
		if(!editor.getDiagramName().isEmpty())
			textFieldNameOfBlock.setText(editor.getDiagramName());
		textFieldNameOfBlock.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				setNextBtnEnabledState(panel_name);
				
			}
		});
		
		return panel_name;
	}
	
	ArrayList<String> getListModel(){
		ArrayList<String> palNames = editor.paletteNames;
		ArrayList<String> listModel = new ArrayList<String>();
		for(int i=editor.defPalettesCount; i<palNames.size(); i++) {
			String entry = palNames.get(i);
			entry +=" | " + editor.palettes.get(i).getPath();
			listModel.add(entry);
		}
		return listModel;
	}
	
	private String getPalettePath() {
		String value = (String) list.getSelectedValue();
		String path = value.split("[|]")[1].substring(1);
		return path;
	}
	
	private void refreshList() {
		DefaultListModel model = new DefaultListModel<>();
	      model.clear();
	      ArrayList<String> data = getListModel();
	      for(int i=0; i<data.size(); i++) {
	    	  model.addElement(data.get(i));
	      }
	      list.setModel(model);
	    }
	
	private JPanel panelPaletteSelect() {
		JPanel panel_3 = new JPanel();
		
		panel_3.setLayout(null);
		
		JLabel lblDocelowaPaleta = new JLabel("Docelowa paleta");
		lblDocelowaPaleta.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblDocelowaPaleta.setBounds(10, 11, 137, 22);
		panel_3.add(lblDocelowaPaleta);
		
		JLabel lblWybierzPaletW = new JLabel("Wybierz paletę, w którym znajdować ma się nowy blok-wzorzec");
		lblWybierzPaletW.setBounds(10, 46, 414, 14);
		panel_3.add(lblWybierzPaletW);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 72, 414, 159);
		panel_3.add(scrollPane);
		
		list = new JList();
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				setNextBtnEnabledState(panel_3);
				
			}
		});
		scrollPane.setViewportView(list);
		
		JButton btnNewButton = new JButton("Zarządzaj paletami");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PaletteEditor palEdit = new PaletteEditor(editor);
				palEdit.setVisible(true);
				refreshList();
			}
		});
		btnNewButton.setBounds(287, 242, 137, 23);
		panel_3.add(btnNewButton);
		
		return panel_3;
	}
	
	private Component getCurrentPanel() {
		for(Component comp : mainpanel.getComponents()) {
		    if (comp.isVisible()) {
		        return (JPanel)comp;
		    }
		}
		return null;
	}

	private void setBtnsEnabledStates() {
		JPanel currPanel = (JPanel) getCurrentPanel();
		setBackBtnEnabledState(currPanel);
		setNextBtnEnabledState(currPanel);
		
	}
	
	private void setBackBtnEnabledState(JPanel currPanel) {
		if(mainpanel.getComponents()[0].equals(currPanel)) {
			btnBack.setEnabled(false);
		}
		else {
			btnBack.setEnabled(true);
		}
		
	}
	
	private void setNextBtnEnabledState(JPanel currPanel) {
		if(mainpanel.getComponents()[0].equals(currPanel)) {
			btnNext.setText("Dalej");
			if(textFieldNameOfBlock.getText().equals(""))
				btnNext.setEnabled(false);
			else
				btnNext.setEnabled(true);
		}
		else if(mainpanel.getComponents()[1].equals(currPanel)) {
			btnNext.setText("Utwórz");
			if(list.isSelectionEmpty())
				btnNext.setEnabled(false);
			else
				btnNext.setEnabled(true);
		}
		
	}
	
	
	private void backBtnAction() {
		
		cl.previous(mainpanel);
		setBtnsEnabledStates();
	}
	
	private void nextBtnAction() {
		JPanel currPanel = (JPanel) getCurrentPanel();
		if(mainpanel.getComponents()[0].equals(currPanel)) {
			blockName = textFieldNameOfBlock.getText().replaceAll("[.]", "");
			refreshList();
			cl.next(mainpanel);
			setBtnsEnabledStates();
		}
		else if(mainpanel.getComponents()[1].equals(currPanel)) {
			pathName = getPalettePath();
			File f = new File(pathName +"/"+ blockName+".tmpb");
			if(f.exists()) {
				int result = JOptionPane.showConfirmDialog(this, "Blok o nazwie " + blockName + " istnieje w wybranej palecie. Czy chcesz nadpisać ten blok?\nNazwę możesz zmienić w poprzednim kroku, klikając na przycisk Wstecz.", "Kreator bloku", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.NO_OPTION) return;
			}
			panelFinalAction();
		}	
		
		
		
	}
	
	private void cancelBtnAction() {
		int result = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz zamknąć kreatora tworzenia bloku?", "Kreator bloku", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION)
			dispose();
	}
	
	/**
	 * Ostatnia akcja kreatora, po wybraniu palety
	 */
	private void panelFinalAction() {
		BlockBean block = new BlockBean();
		File f = new File(pathName + "/" + blockName + ".tmpb");
		block.setFile(f);
		block.setName(blockName);
		if(type == BlockBean.TYPE_DIAGRAM) {
			if(diagram != null) {
				try {
					FileOutputStream out = new FileOutputStream(f);
					DiagFileUtils.createTemplateBlockFile(null, editor, blockName, out);
					editor.setDiagramName(blockName);
					editor.fillAllPalettes();
					dispose();
				} catch (IOException | ParserConfigurationException | TransformerException e) {
					JOptionPane.showMessageDialog(null, "Błąd zapisu pliku: " + e.getMessage(), "Błąd zapisu", JOptionPane.ERROR_MESSAGE, null);
				} catch (MultipleOutputsInInputException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Błędny diagram", JOptionPane.ERROR_MESSAGE, null);
				}
			}
			else {
				
			}
		}
		else if(type == BlockBean.TYPE_FUNCTION) {
			block.setType(BlockBean.TYPE_FUNCTION);
			FuncBlockEditor funcEditor = new FuncBlockEditor(block, true);
			funcEditor.setVisible(true);
			editor.fillAllPalettes();
			dispose();
		}
		
		
	}

/**
 * Tworzy okno kreatora nowych bloków wzorców.
 * @param type - typ bloku, przyjmuje wartości <code>BlockBean.TYPE_FUNCTION</code> lub <code>BlockBean.TYPE_DIAGRAM</code>
 * @param editor - wymagane, obiekt edytora diagramu, wykorzystywane do pobrania listy palet.
 * @param diagram - obiekt diagramu, uzywane, gdy nowy blok jest tworzony z obecnie otwartego diagram. Kiedy diagram może null,
 * 					<br>gdy tworzony jest blok funkcjonalny lub blok strukturalny z innego pliku diagramu
 * @param palettePath - ściezka palety, do którego ma być zapisany blok wzorzec, gdy podana, jest ona wówczas zaznaczona na liście palet
 * 					<br> gdy jest null, paleta wybierana jest z listy przez użytkownika.
 */
	public TemplateBlockWizard(String type, DigitalCircuitEditor editor, CircuitSchemaBean diagram, String palettePath) {
		this.editor = editor;
		this.diagram = diagram;
		this.palettePath = palettePath;
		this.type = type;
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelBtnAction();
			}
			
		});
		setBounds(100, 100, 450, 356);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		contentPane.add(panel, BorderLayout.SOUTH);
		
		btnBack = new JButton("Wstecz");
		btnBack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				backBtnAction();
				
			}
		});
		panel.add(btnBack);
		
		btnNext = new JButton("Dalej");
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextBtnAction();
				
			}
		});
		panel.add(btnNext);
		
		JButton btnCancel = new JButton("Anuluj");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancelBtnAction();
			}
		});
		panel.add(btnCancel);
		
		mainpanel = new JPanel();
		contentPane.add(mainpanel, BorderLayout.CENTER);
		cl = new CardLayout(0, 0);
		mainpanel.setLayout(cl);
		
		JPanel panel_name = panelNameOfBlock();
		mainpanel.add(panel_name, "name_265444680882581");
		
		JPanel panel_3 = panelPaletteSelect();
		mainpanel.add(panel_3, "name_264585282382230");
		setBtnsEnabledStates();
	}
}
