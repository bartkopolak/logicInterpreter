package logicInterpreter.DiagramEditor.com.mxgraph.swing.editor;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.EditorActions.*;

public class EditorToolBar extends JToolBar
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8015443128436394471L;

	/**
	 * 
	 * @param frame
	 * @param orientation
	 */
	private boolean ignoreZoomChange = false;

	/**
	 * 
	 */
	public EditorToolBar(final BasicGraphEditor basicGraphEditor, int orientation)
	{
		super(orientation);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(3, 3, 3, 3), getBorder()));
		setFloatable(false);
		add(basicGraphEditor.bind("New", new NewAction(), 
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/new.gif"));
		add(basicGraphEditor.bind("Open", new OpenAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/open.gif"));
		add(basicGraphEditor.bind("Save", new SaveAction(false),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/save.gif"));

		addSeparator();

		add(basicGraphEditor.bind("Print", new PrintAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/print.gif"));

		addSeparator();

		add(basicGraphEditor.bind("Cut", TransferHandler.getCutAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/cut.gif"));
		add(basicGraphEditor.bind("Copy", TransferHandler.getCopyAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/copy.gif"));
		add(basicGraphEditor.bind("Paste", TransferHandler.getPasteAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/paste.gif"));

		addSeparator();

		add(basicGraphEditor.bind("Delete", mxGraphActions.getDeleteAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/delete.gif"));

		addSeparator();

		add(basicGraphEditor.bind("Undo", new HistoryAction(true),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/undo.gif"));
		add(basicGraphEditor.bind("Redo", new HistoryAction(false),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/redo.gif"));

		addSeparator();

		// Gets the list of available fonts from the local graphics environment
		// and adds some frequently used fonts at the beginning of the list
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		List<String> fonts = new ArrayList<String>();
		fonts.addAll(Arrays.asList(new String[] { "Helvetica", "Verdana",
				"Times New Roman", "Garamond", "Courier New", "-" }));
		fonts.addAll(Arrays.asList(env.getAvailableFontFamilyNames()));

		final JComboBox fontCombo = new JComboBox(fonts.toArray());
		fontCombo.setEditable(true);
		fontCombo.setMinimumSize(new Dimension(120, 0));
		fontCombo.setPreferredSize(new Dimension(120, 0));
		fontCombo.setMaximumSize(new Dimension(120, 100));
		add(fontCombo);

		fontCombo.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				String font = fontCombo.getSelectedItem().toString();

				if (font != null && !font.equals("-"))
				{
					mxGraph graph = basicGraphEditor.getGraphComponent().getGraph();
					graph.setCellStyles(mxConstants.STYLE_FONTFAMILY, font);
				}
			}
		});

		final JComboBox sizeCombo = new JComboBox(new Object[] { "6pt", "8pt",
				"9pt", "10pt", "12pt", "14pt", "18pt", "24pt", "30pt", "36pt",
				"48pt", "60pt" });
		sizeCombo.setEditable(true);
		sizeCombo.setMinimumSize(new Dimension(65, 0));
		sizeCombo.setPreferredSize(new Dimension(65, 0));
		sizeCombo.setMaximumSize(new Dimension(65, 100));
		add(sizeCombo);

		sizeCombo.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				mxGraph graph = basicGraphEditor.getGraphComponent().getGraph();
				graph.setCellStyles(mxConstants.STYLE_FONTSIZE, sizeCombo
						.getSelectedItem().toString().replace("pt", ""));
			}
		});

		addSeparator();

		add(basicGraphEditor.bind("Bold", new FontStyleAction(true),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/bold.gif"));
		add(basicGraphEditor.bind("Italic", new FontStyleAction(false),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/italic.gif"));

		addSeparator();

		add(basicGraphEditor.bind("Left", new KeyValueAction(mxConstants.STYLE_ALIGN,
				mxConstants.ALIGN_LEFT),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/left.gif"));
		add(basicGraphEditor.bind("Center", new KeyValueAction(mxConstants.STYLE_ALIGN,
				mxConstants.ALIGN_CENTER),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/center.gif"));
		add(basicGraphEditor.bind("Right", new KeyValueAction(mxConstants.STYLE_ALIGN,
				mxConstants.ALIGN_RIGHT),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/right.gif"));

		addSeparator();

		final mxGraphView view = basicGraphEditor.getGraphComponent().getGraph()
				.getView();
		final JComboBox zoomCombo = new JComboBox(new Object[] { "400%",
				"200%", "150%", "100%", "75%", "50%", mxResources.get("actualSize") });
		zoomCombo.setEditable(true);
		zoomCombo.setMinimumSize(new Dimension(75, 0));
		zoomCombo.setPreferredSize(new Dimension(75, 0));
		zoomCombo.setMaximumSize(new Dimension(75, 100));
		zoomCombo.setMaximumRowCount(9);
		add(zoomCombo);

		// Sets the zoom in the zoom combo the current value
		mxIEventListener scaleTracker = new mxIEventListener()
		{
			/**
			 * 
			 */
			public void invoke(Object sender, mxEventObject evt)
			{
				ignoreZoomChange = true;

				try
				{
					zoomCombo.setSelectedItem((int) Math.round(100 * view
							.getScale())
							+ "%");
				}
				finally
				{
					ignoreZoomChange = false;
				}
			}
		};

		// Installs the scale tracker to update the value in the combo box
		// if the zoom is changed from outside the combo box
		view.getGraph().getView().addListener(mxEvent.SCALE, scaleTracker);
		view.getGraph().getView().addListener(mxEvent.SCALE_AND_TRANSLATE,
				scaleTracker);

		// Invokes once to sync with the actual zoom value
		scaleTracker.invoke(null, null);

		zoomCombo.addActionListener(new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				mxGraphComponent graphComponent = basicGraphEditor.getGraphComponent();

				// Zoomcombo is changed when the scale is changed in the diagram
				// but the change is ignored here
				if (!ignoreZoomChange)
				{
					String zoom = zoomCombo.getSelectedItem().toString();

					
					if (zoom.equals(mxResources.get("actualSize")))
					{
						graphComponent.zoomActual();
					}
					else
					{
						try
						{
							zoom = zoom.replace("%", "");
							double scale = Math.min(16, Math.max(0.01,
									Double.parseDouble(zoom) / 100));
							graphComponent.zoomTo(scale, graphComponent
									.isCenterZoom());
						}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(basicGraphEditor, ex
									.getMessage());
						}
					}
				}
			}
		});
	}
}
