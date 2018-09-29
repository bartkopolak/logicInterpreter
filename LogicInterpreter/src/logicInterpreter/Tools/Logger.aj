package logicInterpreter.Tools;



public aspect Logger {
	pointcut log(): execution(public * * (..))
	&& !within(logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.BasicGraphEditor.*)
	&& !within(logicInterpreter.DiagramEditor.com.mxgraph.swing.editor.BasicGraphEditor.status);
	long startTime = 0;
	before(): log() {
		startTime = System.nanoTime();
	    //System.out.printf("%ts : | enter | (%s) | \n",startTime, thisJoinPoint.getSignature());
	}
	
	after(): log() {
		long endTime = System.nanoTime();
		String msg = String.format("%ts : exit | %s | %ts \n",System.currentTimeMillis() , thisJoinPoint.getSignature(), endTime - startTime);
		LoggerClass.LOGGER.info(msg);
		//System.out.printf(msg);
		
	}
}
