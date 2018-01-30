package logicInterpreter.BoolInterpret;

public final class ThreeStateBoolean implements java.io.Serializable {
		
		public static enum State {
			TRUE, FALSE, UNKNOWN
		};
	
		private final static String trueStr = "true";
		private final static String falseStr = "false";
		private final static String unknownStr = "unknown";
		
		/**
		 * The {@code Boolean} object corresponding to the primitive value
		 * {@code true}.
		 */
		public static final ThreeStateBoolean TRUE = new ThreeStateBoolean(true);

		/**
		 * The {@code Boolean} object corresponding to the primitive value
		 * {@code false}.
		 */
		public static final ThreeStateBoolean FALSE = new ThreeStateBoolean(false);

		/**
		 * The {@code Boolean} object corresponding to the primitive value
		 * {@code false}.
		 */
		public static final ThreeStateBoolean UNKNOWN = new ThreeStateBoolean(null);

		/**
		 * The value of the Boolean.
		 *
		 * @serial
		 */
		private State value;
/*
		public ThreeStateBoolean(State value) {
			if(value == null) 
				this.value = State.UNKNOWN;
			else 
				this.value = value;
		}
		*/
		public ThreeStateBoolean(Boolean value) {
			if(value == null) 
				this.value = State.UNKNOWN;
			else if(value == true)
				this.value = State.TRUE;
			else if(value == false)
				this.value = State.FALSE;
				
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Boolean) {
				Boolean b = (Boolean)obj;
				if(value == State.TRUE && b) return true;
				else if(value == State.FALSE && !b) return true;
				
				else return false;
			}
			else if(obj instanceof ThreeStateBoolean) {
				if(value == ((ThreeStateBoolean)obj).getValue()) return true;
				else return false;
			}
			return false;
		}

		public State getValue() {
			return value;
		}

		@Override
		public String toString() {
			if(value == null)
				return("null");
			else
				if(value == State.FALSE) return falseStr;
				else if(value == State.TRUE) return trueStr;
				else if(value == State.UNKNOWN) return unknownStr;
			return null;
		}
		/**
		 * Wykorzystywane do ustalenia stanu wyjścia np w symulatorze, stan nieustalony będzie stanem niskim.
		 * @return
		 */
		public Boolean toBoolean() {
			if(value == State.FALSE) return false;
			else if(value == State.TRUE) return true;
			else if(value == State.UNKNOWN) return false;
			return false;
		}
		
		public ThreeStateBoolean and(ThreeStateBoolean b) {
			if(value == State.FALSE || b.getValue() == State.FALSE) return FALSE;
			else if(value == State.UNKNOWN || b.getValue() == State.UNKNOWN) return UNKNOWN;
			else if(value == State.TRUE && b.getValue() == State.TRUE) return TRUE;
			return UNKNOWN;
		}
		
		public ThreeStateBoolean or(ThreeStateBoolean b) {
			if(value == State.TRUE || b.getValue() == State.TRUE) return TRUE;
			else if(value == State.UNKNOWN || b.getValue() == State.UNKNOWN) return UNKNOWN;
			else if(value == State.FALSE && b.getValue() == State.FALSE) return FALSE;
			return UNKNOWN;
		}
		
		public ThreeStateBoolean xor(ThreeStateBoolean b) {
			if(value == State.UNKNOWN || b.getValue() == State.UNKNOWN) return UNKNOWN;
			else if(value == State.FALSE && b.getValue() == State.FALSE) return FALSE;
			else if(value == State.TRUE && b.getValue() == State.TRUE) return FALSE;
			else if(value == State.FALSE && b.getValue() == State.TRUE) return TRUE;
			else if(value == State.TRUE && b.getValue() == State.FALSE) return TRUE;
			return UNKNOWN;
		}
		
		public ThreeStateBoolean not() {
			if(value == State.TRUE ) return FALSE;
			else if(value == State.UNKNOWN) return UNKNOWN;
			else if(value == State.FALSE) return TRUE;
			return UNKNOWN;
		}
		
		public static boolean parseThreeStateBoolean(String s) {
			if(s!=null) {
				if(s.equalsIgnoreCase(trueStr) || s.equalsIgnoreCase(falseStr) || s.equalsIgnoreCase(unknownStr)) return true;
			}
			return false;
		}
		
		public static ThreeStateBoolean valueOf(String s) {
			if(s!=null) {
				if(s.equalsIgnoreCase(trueStr) ) return TRUE;
				else if(s.equalsIgnoreCase(trueStr) ) return FALSE;
				else if(s.equalsIgnoreCase(trueStr) ) return UNKNOWN;
			}
			return UNKNOWN;
		}
		
		

}
