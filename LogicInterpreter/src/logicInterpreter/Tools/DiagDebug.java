package logicInterpreter.Tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import logicInterpreter.BoolInterpret.ThreeStateBoolean;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramInterpret.DiagramBean;
import logicInterpreter.Exceptions.RecurrentLoopException;
import logicInterpreter.Nodes.BlockInputBean;
import logicInterpreter.Nodes.BlockOutputBean;
import logicInterpreter.Nodes.DiagramInputBean;
import logicInterpreter.Nodes.DiagramOutputBean;
import logicInterpreter.Nodes.OutputBean;

import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JList;
import java.awt.Dimension;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Point;
import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import javax.swing.JToggleButton;

public class DiagDebug extends JFrame {

	class CircuitCanv extends Canvas {

		class BlockInCanvas {
			private Point location;
			private BlockBean block;
			
			public Point getLocation() {
				return location;
			}
			public BlockBean getBlock() {
				return block;
			}
			
			public BlockInCanvas(BlockBean block, Point location){
				this.block = block;
				this.location = location;
			}
			
			
		}
		
		
		private void drawWire(Graphics g, int x0, int y0, int x1, int y1, int rand){
			boolean dirR = false;	//kierunek prawo
			boolean dirD = false;//kierunek dół
			if(x0<x1) dirR = true;
			if(y0<y1) dirD = true;
			int outputPinLength = 5 + rand;
			int width = Math.abs(x1-x0);
			int height = Math.abs(y1-y0);
			
			if(dirR){
			int halfWidth = width/2 + rand - 2*outputPinLength;
			g.drawLine(x0, y0, x0+outputPinLength, y0);
			
			g.drawLine(x0+outputPinLength, y0, x0+halfWidth, y0);
			g.drawLine(x0+halfWidth, y0, x0+halfWidth, y1);
			g.drawLine(x0+halfWidth, y1, x1-outputPinLength, y1);
			
			g.drawLine(x1-outputPinLength, y1, x1, y1);
			}
			else{
				int h=30+rand;
				g.drawLine(x0, y0, x0+outputPinLength, y0);
				
				g.drawLine(x0+outputPinLength, y0, x0+outputPinLength, y0-h);
				g.drawLine(x0+outputPinLength, y0-h, x1-outputPinLength, y0-h);
				g.drawLine(x1-outputPinLength, y0-h, x1-outputPinLength, y1);
				
				g.drawLine(x1-outputPinLength, y1, x1, y1);
			}
		}
		
		@Override
		public void paint(Graphics g) {
			int cx = 50;
			int cy = 90;
			List<Point> inputsxy = new ArrayList<Point>();
			List<Point> outputsxy = new ArrayList<Point>();
			if(diagram != null){
				int k = 0;
				for(DiagramInputBean input : diagram.getInputList()){
					
					g.drawLine(cx-50, cy+k, cx, cy+k);
					g.drawString(input.getName(), cx-50, cy+k-2);
					inputsxy.add(new Point(cx, cy+k));
					k += 45;
				}
				k = 0;
				cx += 80;
				if(diagram.getFlowList().isEmpty()){
					try {
						diagram.evaluate();
					} catch (RecurrentLoopException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				List<BlockInCanvas> blocks = new ArrayList<BlockInCanvas>();
				for(List<BlockBean> level : diagram.getFlowList()){
					k = 0;
					for(BlockBean block : level){
						g.drawRect(cx, cy+k, 75, 50);
						g.drawString(block.getName(), cx+3, cy+k+10);
						blocks.add(new BlockInCanvas(block, new Point(cx, cy+k)));
						k+=80;
					}
					cx += 170;
				}
				g.setFont(new Font("Arial", Font.PLAIN, 8));
				for(BlockInCanvas block : blocks){
					int l = 5;
					int i = 0;
					for(BlockInputBean in : block.getBlock().getInputList()){
						if(in.getState().toBoolean()) g.setColor(Color.GREEN);
						else  g.setColor(Color.RED);
						OutputBean fromOut = in.getFrom();
						if(fromOut instanceof BlockOutputBean){
							BlockBean parent = ((BlockOutputBean) fromOut).getParent();
							for(BlockInCanvas bl : blocks){
								if(bl.getBlock().equals(parent)){
									Point fromPt = bl.getLocation();
									
									drawWire(g, fromPt.x+75, fromPt.y, block.getLocation().x, block.getLocation().y+l, (i%3)*10);
									g.drawString(in.getName(), block.getLocation().x-5, block.getLocation().y + l);
									l+=15;
									i++;
									break;
								}
							}
							
						}
						if(fromOut instanceof DiagramInputBean){
							Point fromPt = inputsxy.get(diagram.getInputList().indexOf(fromOut));
							drawWire(g, fromPt.x, fromPt.y, block.getLocation().x, block.getLocation().y+l, (i%3)*10);
							g.drawString(in.getName(), block.getLocation().x-5, block.getLocation().y + l);
							l+=15;
							i++;
						}
						
						
					}
				}
				k = 0;
				cx+=10;
				for(DiagramOutputBean output : diagram.getOutputList()){
					g.setColor(Color.black);
					g.drawLine(cx, cy+k, cx+50, cy+k);
					g.drawString(output.getName(), cx+50, cy+k);
					outputsxy.add(new Point(cx, cy+k));
					
					int i = 1;
					OutputBean fromOut = output.getFrom();
					
					if(fromOut.getState().toBoolean()) g.setColor(Color.GREEN);
					else  g.setColor(Color.RED);
					if(fromOut instanceof BlockOutputBean){
						BlockBean parent = ((BlockOutputBean) fromOut).getParent();
						for(BlockInCanvas bl : blocks){
							if(bl.getBlock().equals(parent)){
								
								drawWire(g, bl.getLocation().x+75, bl.getLocation().y+i*15, cx, cy+k, (i%3)*10);
								g.drawString(fromOut.getName(), bl.getLocation().x+78, bl.getLocation().y+i*15);
								i++;
								break;
							}
						}
					}
					k+=45;
				}
				
			}
		}
		
		
	}
	
	private final JPanel contentPanel = new JPanel();
	private JSplitPane splitPane;
	JPanel inputpanel = new JPanel();
	Canvas canvas = new CircuitCanv();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DiagDebug dialog = new DiagDebug();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	DiagramBean diagram;
	JList<BlockBean> list = new JList<BlockBean>();
	List<JCheckBox> inputs = new ArrayList<JCheckBox>();
	
	ItemListener inputListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			JCheckBox src = (JCheckBox) e.getSource();
			diagram.getInput(src.getName()).setState(new ThreeStateBoolean(src.isSelected()));;
			
		}
	};
	
	public void init(){
		try {
			diagram = XMLparse.parseXMLDiagram(new File("xmls/licznikasync.xml"));
			DefaultListModel<BlockBean> model = new DefaultListModel<BlockBean>();
			for(BlockBean block : diagram.getBlocksList())
				model.addElement(block);
			list.setModel(model);
			
			for(DiagramInputBean input : diagram.getInputList()){
				JCheckBox inputBox = new JCheckBox();
				inputBox.setName(input.getName());
				inputBox.setText(input.getName());
				inputBox.addItemListener(inputListener);
				inputs.add(inputBox);
				inputpanel.add(inputBox);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Create the dialog.
	 */
	public DiagDebug() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setBounds(100, 100, 684, 544);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			splitPane = new JSplitPane();
			contentPanel.add(splitPane);
			{
				JPanel panel = new JPanel();
				panel.setPreferredSize(new Dimension(150, 10));
				splitPane.setLeftComponent(panel);
				panel.setLayout(new BorderLayout(0, 0));
				{
					panel.add(list);
				}
			}
			{
				JPanel panel = new JPanel();
				splitPane.setRightComponent(panel);
				panel.setLayout(new BorderLayout(0, 0));
				{
					JSplitPane splitPane_1 = new JSplitPane();
					splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
					panel.add(splitPane_1, BorderLayout.CENTER);
					{
						JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
						splitPane_1.setRightComponent(tabbedPane);
						JPanel panel_1_1 = new JPanel();
						tabbedPane.addTab("New tab", null, panel_1_1, null);
						panel_1_1.setLayout(new BorderLayout(0, 0));
						panel_1_1.add(canvas, BorderLayout.CENTER);
						{
							JPanel panel_1 = new JPanel();
							tabbedPane.addTab("New tab", null, panel_1, null);
						}
					}
					{
						JSplitPane splitPane_2 = new JSplitPane();
						splitPane_1.setLeftComponent(splitPane_2);
						{
							JPanel panel_1 = new JPanel();
							splitPane_2.setLeftComponent(panel_1);
							{
								JButton btnNewButton = new JButton("wykonaj cykl");
								btnNewButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent arg0) {
										try {
											diagram.evaluate();
											canvas.repaint();
										} catch (RecurrentLoopException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
								panel_1.add(btnNewButton);
							}
						}
						{
							
							splitPane_2.setRightComponent(inputpanel);
						}
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
		}

		init();
	}

}
