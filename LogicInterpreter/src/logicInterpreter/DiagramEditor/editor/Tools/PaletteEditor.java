package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import logicInterpreter.DiagramEditor.editor.GraphEditor;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.AbstractListModel;
import javax.swing.Box;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class PaletteEditor extends JDialog {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	JList<String> list;
	GraphEditor editor;
	/**
	 * Create the frame.
	 */
	
	void refreshList() {
		DefaultListModel model = new DefaultListModel<>();
	      model.clear();
	      ArrayList<String> data = getListModel();
	      for(int i=0; i<data.size(); i++) {
	    	  model.addElement(data.get(i));
	      }
	      list.setModel(model);
	    }
	
	void addPalette() {
		;
		JFileChooser chooser = new JFileChooser(); 
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle("Wybierz katalog z wzorcami bloków");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    //
		    // disable the "All files" option.
		    //
		    chooser.setAcceptAllFileFilterUsed(false);
		    //    
		    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		      String path = chooser.getSelectedFile().getAbsolutePath();
		      String name = JOptionPane.showInputDialog("Podaj nazwę");
		      editor.addPalette(name, path);
		      refreshList();
		    }
	}
	
	void removePalette(String value) {
		int result = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć wybraną paletę?", "Pytanie", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			String path = value.split("[|]")[1].substring(1);
			editor.removePalette(path);
			refreshList();
		}
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
	
	public PaletteEditor(GraphEditor ge) {
		setModal(true);
		editor = ge;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(80, 10));
		contentPane.add(panel, BorderLayout.EAST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JButton btnNewButton = new JButton("Dodaj");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				addPalette();
			}
		});
		btnNewButton.setPreferredSize(new Dimension(70, 23));
		btnNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Usuń");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removePalette(list.getSelectedValue());
			}
		});
		btnNewButton_1.setPreferredSize(new Dimension(70, 23));
		btnNewButton_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(btnNewButton_1);
		panel.add(Box.createVerticalGlue());
		JButton btnSave = new JButton("OK");
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.saveCustomPalettes();
					dispose();
				} catch (SAXException | IOException | ParserConfigurationException | TransformerException e1) {
					JOptionPane.showMessageDialog(null, "Błąd zapisu pliku konfiguracyjnego");
				}
				
			}
		});
		panel.add(btnSave);
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);
		
		
		
		list = new JList(getListModel().toArray());
		scrollPane.setViewportView(list);
		
	}

}
