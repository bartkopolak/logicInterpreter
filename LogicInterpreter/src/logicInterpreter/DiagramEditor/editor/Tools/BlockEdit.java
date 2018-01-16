package logicInterpreter.DiagramEditor.editor.Tools;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class BlockEdit extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BlockEdit frame = new BlockEdit();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BlockEdit() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 198, 210);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnEdytuj = new JButton("Edytuj");
		btnEdytuj.setBounds(10, 59, 89, 23);
		contentPane.add(btnEdytuj);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Tablica prawdy", "Diagram"}));
		comboBox.setBounds(10, 11, 116, 20);
		contentPane.add(comboBox);
		
		JButton btnKolor = new JButton("Kolor");
		btnKolor.setBounds(10, 93, 89, 23);
		contentPane.add(btnKolor);
	}
}
