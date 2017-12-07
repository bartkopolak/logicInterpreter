package logicInterpreter.BoolInterpret;

import java.util.Iterator;
import com.fathzer.soft.javaluator.*;
 
/** An example of how to implement an evaluator from scratch.
 */
public class BoolEval extends AbstractEvaluator<Boolean> {
  /** The negate unary operator.*/
  public final static Operator NEGATE = new Operator("'", 1, Operator.Associativity.RIGHT, 4);
  /** The logical AND operator.*/
  private static final Operator AND = new Operator("*", 2, Operator.Associativity.LEFT, 3);
  /** The logical OR operator.*/
  public final static Operator OR = new Operator("+", 2, Operator.Associativity.LEFT, 1);
  /** The logical OR operator.*/
  public final static Operator XOR = new Operator("^", 2, Operator.Associativity.LEFT, 2);
 
  private static final Parameters PARAMETERS;
 
  static {
    // Create the evaluator's parameters
    PARAMETERS = new Parameters();
    // Add the supported operators
    PARAMETERS.add(AND);
    PARAMETERS.add(OR);
    PARAMETERS.add(XOR);
    PARAMETERS.add(NEGATE);
    PARAMETERS.addFunctionBracket(BracketPair.PARENTHESES);
    PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
  }
 
  public BoolEval() {
    super(PARAMETERS);
  }
 
  @Override
  protected Boolean toValue(String literal, Object evaluationContext) {
	if(Boolean.parseBoolean(literal)){
		return Boolean.valueOf(literal);
	}
	else{
		throw new IllegalArgumentException(literal+" is not a boolean");
	}
  }
 
  /**
   * TODO: zaimplementuj logike 3-stanową:
   * http://wiki.c2.com/?ThreeValuedLogic
   * 
   * kazde wyjscie domyslnie jest undefined.
   */
  
  @Override
  protected Boolean evaluate(Operator operator, Iterator<Boolean> operands, Object evaluationContext) {
    if (operator == NEGATE) {
      return !operands.next();
    } else if (operator == OR) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 || o2;
    } else if (operator == AND) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 && o2;
    } else if (operator == XOR) {
        Boolean o1 = operands.next();
        Boolean o2 = operands.next();
        return o1 ^ o2;
      } else {
      return super.evaluate(operator, operands, evaluationContext);
    }
  }
}
 