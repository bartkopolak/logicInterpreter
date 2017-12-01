package logicInterpreter.Exceptions;

public class NoInputFoundException extends Exception {

	public NoInputFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NoInputFoundException(String diagramName, String inputName) {
		super("W diagramie "+ diagramName +" nie znaleziono wejścia " + inputName + ", do którego próbowano się połączyć");
		// TODO Auto-generated constructor stub
	}

}
