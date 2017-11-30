package logicInterpreter.Exceptions;

public class NoSuchTypeException extends Exception {

	public NoSuchTypeException() {
		// TODO Auto-generated constructor stub
	}

	public NoSuchTypeException(String arg0) {
		super("Nie znaleziono podanego typu: " + arg0);
		// TODO Auto-generated constructor stub
	}
}
