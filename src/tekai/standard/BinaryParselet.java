package tekai.standard;

import tekai.Expression;
import tekai.Parselet;

public class BinaryParselet extends Parselet {

    private final String startingRegularExpression;
    private final String type;

    public BinaryParselet(int precedence, String startingRegularExpression, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.type = type;
    }

    @Override
    public boolean isLeftAssociativity() {
      return true;
    }

    @Override
    public boolean isPrefixParselet() {
        return false;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = new Expression(type, lastMatchTrimmed());
        result.addChildren(left(), right());
        return result;
    }
}
