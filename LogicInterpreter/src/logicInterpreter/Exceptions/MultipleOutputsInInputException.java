package logicInterpreter.Exceptions;

public class MultipleOutputsInInputException extends Exception {

	public MultipleOutputsInInputException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MultipleOutputsInInputException(String diagramName, String inputName) {
		super("Próbowano podłączyć więcej niż jedno połączenie do wejścia " + inputName + " w diagramie " + diagramName);
		// TODO Auto-generated constructor stub
	}

	
}
