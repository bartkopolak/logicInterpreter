package logicInterpreter.DiagramEditor.editor;

import com.mxgraph.view.mxGraph;

public class DiagramBuilder {
	
	public void buildDiagram(mxGraph g) {
		g.getChildVertices(g.getDefaultParent());
	}

}
