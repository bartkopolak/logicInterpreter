package logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorActions.*;
import logicInterpreter.DiagramEditor.editor.GraphEditor;
import logicInterpreter.DiagramEditor.editor.Tools.PaletteEditor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

public class SchemaEditorMenuBar extends JMenuBar
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6776304509649205465L;

	@SuppressWarnings("serial")
	public SchemaEditorMenuBar(final BasicGraphEditor editor)
	{
		final mxGraphComponent graphComponent = editor.getGraphComponent();
		final mxGraph graph = graphComponent.getGraph();
		JMenu menu = null;
		JMenu submenu = null;

		// Creates the file menu
		menu = add(new JMenu(mxResources.get("file")));

		menu.add(editor.bind(mxResources.get("new"), new NewAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/new.gif"));
		menu.add(editor.bind(mxResources.get("openFile"), new OpenAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/open.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("save"), new SaveAction(false),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/save.gif"));
		menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(true),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/saveas.gif"));

		menu.addSeparator();
		
		menu.add(editor.bind(mxResources.get("exportToVHDL"), new ExportToVHDLAction((GraphEditor)editor),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/save.gif"));
		
		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("pageSetup"),
				new PageSetupAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/pagesetup.gif"));
		menu.add(editor.bind(mxResources.get("print"), new PrintAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/print.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("exit"), new ExitAction()));

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));

		menu.add(editor.bind(mxResources.get("undo"), new HistoryAction(true),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/undo.gif"));
		menu.add(editor.bind(mxResources.get("redo"), new HistoryAction(false),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/redo.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("cut"), TransferHandler
				.getCutAction(), "/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/cut.gif"));
		menu.add(editor
				.bind(mxResources.get("copy"), TransferHandler.getCopyAction(),
						"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/copy.gif"));
		menu.add(editor.bind(mxResources.get("paste"), TransferHandler
				.getPasteAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/paste.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("delete"), mxGraphActions
				.getDeleteAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/delete.gif"));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("selectAll"), mxGraphActions
				.getSelectAllAction()));
		menu.add(editor.bind(mxResources.get("selectNone"), mxGraphActions
				.getSelectNoneAction()));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("edit"), mxGraphActions
				.getEditAction()));

		// Creates the view menu
		menu = add(new JMenu(mxResources.get("view")));

		JMenuItem item;

		menu.add(new TogglePropertyItem(graphComponent, mxResources
				.get("antialias"), "AntiAlias", true));

		menu.addSeparator();

		menu.add(new ToggleOutlineItem(editor, mxResources.get("outline")));
		menu.add(new ToggleGridItem(editor, mxResources.get("grid")));
		menu.add(new ToggleRulersItem(editor, mxResources.get("rulers")));

		menu.addSeparator();
		
		submenu = (JMenu) menu.add(new JMenu(mxResources.get("grid")));

		submenu.add(editor.bind(mxResources.get("gridSize"),
				new PromptPropertyAction(graph, "Grid Size", "GridSize")));
		submenu.add(editor.bind(mxResources.get("gridColor"),
				new GridColorAction()));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("dashed"), new GridStyleAction(
				mxGraphComponent.GRID_STYLE_DASHED)));
		submenu.add(editor.bind(mxResources.get("dot"), new GridStyleAction(
				mxGraphComponent.GRID_STYLE_DOT)));
		submenu.add(editor.bind(mxResources.get("line"), new GridStyleAction(
				mxGraphComponent.GRID_STYLE_LINE)));
		submenu.add(editor.bind(mxResources.get("cross"), new GridStyleAction(
				mxGraphComponent.GRID_STYLE_CROSS)));
		
		menu.addSeparator();
		
		submenu = (JMenu) menu.add(new JMenu(mxResources.get("zoom")));

		submenu.add(editor.bind("400%", new ScaleAction(4)));
		submenu.add(editor.bind("200%", new ScaleAction(2)));
		submenu.add(editor.bind("150%", new ScaleAction(1.5)));
		submenu.add(editor.bind("100%", new ScaleAction(1)));
		submenu.add(editor.bind("75%", new ScaleAction(0.75)));
		submenu.add(editor.bind("50%", new ScaleAction(0.5)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("custom"), new ScaleAction(0)));

		

		menu.add(editor.bind(mxResources.get("zoomIn"), mxGraphActions
				.getZoomInAction()));
		menu.add(editor.bind(mxResources.get("zoomOut"), mxGraphActions
				.getZoomOutAction()));

		menu.addSeparator();

		menu.add(editor.bind(mxResources.get("actualSize"), mxGraphActions
				.getZoomActualAction()));

		// Creates the diagram menu
		menu = add(new JMenu(mxResources.get("diagram")));

		
		menu.add(editor.bind(mxResources.get("debug"), new DebugAction(((GraphEditor)editor))));
		menu.add(editor.bind(mxResources.get("simulate"), new SimulateAction(((GraphEditor)editor))));
		

		
		menu = add(new JMenu(mxResources.get("palettes")));
		menu.add(editor.bind(mxResources.get("createBlock"), new BlockCreateAction(((GraphEditor)editor),false,null)));
		menu.add(editor.bind(mxResources.get("createBlockDiag"), new BlockCreateAction(((GraphEditor)editor),true,null)));
		
		menu.addSeparator();
		
		menu.add(editor.bind(mxResources.get("palEdit"), new PaletteEditAction(((GraphEditor)editor))));
		menu.add(editor.bind(mxResources.get("palRefresh"), new PaletteRefreshAction(((GraphEditor)editor))));
		
		/*
		menu.addSeparator();

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("selection")));

		submenu.add(editor.bind(mxResources.get("selectPath"),
				new SelectShortestPathAction(false)));
		submenu.add(editor.bind(mxResources.get("selectDirectedPath"),
				new SelectShortestPathAction(true)));

		submenu.addSeparator();

		submenu.add(editor.bind(mxResources.get("selectTree"),
				new SelectSpanningTreeAction(false)));
		submenu.add(editor.bind(mxResources.get("selectDirectedTree"),
				new SelectSpanningTreeAction(true)));


		submenu = (JMenu) menu.add(new JMenu(mxResources.get("stylesheet")));

		submenu
				.add(editor
						.bind(
								mxResources.get("basicStyle"),
								new StylesheetAction(
										"/com/mxgraph/examples/swing/resources/basic-style.xml")));
		submenu
				.add(editor
						.bind(
								mxResources.get("defaultStyle"),
								new StylesheetAction(
										"/com/mxgraph/examples/swing/resources/default-style.xml")));
*/
		// Creates the options menu
		menu = add(new JMenu(mxResources.get("options")));

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("display")));
		item = submenu.add(new TogglePropertyItem(graphComponent, mxResources
				.get("centerPage"), "CenterPage", true, new ActionListener()
		{
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e)
			{
				if (graphComponent.isPageVisible()
						&& graphComponent.isCenterPage())
				{
					graphComponent.zoomAndCenter();
				}
			}
		}));

		submenu.add(new TogglePropertyItem(graphComponent, mxResources
				.get("centerZoom"), "CenterZoom", true));
		submenu.add(new TogglePropertyItem(graphComponent, mxResources
				.get("zoomToSelection"), "KeepSelectionVisibleOnZoom", true));
		
		
		// This feature is not yet implemented
		//submenu.add(new TogglePropertyItem(graphComponent, mxResources
		//		.get("pageBreaks"), "PageBreaksVisible", true));




		// Creates the window menu
		/*
		menu = add(new JMenu(mxResources.get("window")));

		UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();

		for (int i = 0; i < lafs.length; i++)
		{
			final String clazz = lafs[i].getClassName();
			menu.add(new AbstractAction(lafs[i].getName())
			{
				public void actionPerformed(ActionEvent e)
				{
					editor.setLookAndFeel(clazz);
				}
			});
		}
		 */
		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));

		item = menu.add(new JMenuItem(mxResources.get("aboutGraphEditor")));
		item.addActionListener(new ActionListener()
		{
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e)
			{
				editor.about();
			}
		});
	}

}
