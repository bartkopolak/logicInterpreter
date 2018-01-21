package logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorActions.HistoryAction;
import logicInterpreter.DiagramEditor.editor.GraphEditor;
import logicInterpreter.DiagramInterpret.BlockBean;
import logicInterpreter.DiagramEditor.com.mxgraph.examples.swing.editor.EditorActions.BlockInfoAction;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxResources;

public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public EditorPopupMenu(BasicGraphEditor basicGraphEditor)
	{
		mxCell selCell = null;
		boolean selected = !basicGraphEditor.getGraphComponent().getGraph()
				.isSelectionEmpty();
		boolean selectedMany = (basicGraphEditor.getGraphComponent().getGraph().getSelectionCount()>1)?true:false;
		if(!selectedMany) {
			
			selCell = (mxCell) basicGraphEditor.getGraphComponent().getGraph().getSelectionCell();
			
			
		}

		add(basicGraphEditor.bind(mxResources.get("undo"), new HistoryAction(true),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/undo.gif"));

		addSeparator();

		add(
				basicGraphEditor.bind(mxResources.get("cut"), TransferHandler
						.getCutAction(),
						"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/cut.gif"))
				.setEnabled(selected);
		add(
				basicGraphEditor.bind(mxResources.get("copy"), TransferHandler
						.getCopyAction(),
						"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/copy.gif"))
				.setEnabled(selected);
		add(basicGraphEditor.bind(mxResources.get("paste"), TransferHandler
				.getPasteAction(),
				"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/paste.gif"));

		addSeparator();

		add(
				basicGraphEditor.bind(mxResources.get("delete"), mxGraphActions
						.getDeleteAction(),
						"/logicInterpreter/DiagramEditor/com/mxgraph/examples/swing/images/delete.gif"))
				.setEnabled(selected);

		addSeparator();

		// Creates the format menu
		JMenu menu;//= (JMenu) add(new JMenu(mxResources.get("format")));

		//EditorMenuBar.populateFormatMenu(menu, editor);

		// Creates the shape menu
		//menu = (JMenu) add(new JMenu(mxResources.get("shape")));

		//EditorMenuBar.populateShapeMenu(menu, editor);

		if(selCell != null && selCell.getValue() instanceof BlockBean)
			add(basicGraphEditor.bind(mxResources.get("blockInfo"),
				new BlockInfoAction((BlockBean) selCell.getValue()))).setSelected(!selectedMany);

			
		//addSeparator();

		//add(basicGraphEditor.bind(mxResources.get("selectVertices"), mxGraphActions
		//		.getSelectVerticesAction()));
		//add(basicGraphEditor.bind(mxResources.get("selectEdges"), mxGraphActions
		//		.getSelectEdgesAction()));

		//addSeparator();

		add(basicGraphEditor.bind(mxResources.get("selectAll"), mxGraphActions
				.getSelectAllAction()));
	}

}
