package tekai.standard;

import tekai.Expression;
import tekai.Parselet;

public class PrefixParselet extends Parselet {

    private final String startingRegularExpression;
    private final String type;

    public PrefixParselet(int precedence, String startingRegularExpression, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.type = type;
    }

    @Override
    public boolean isPrefixParselet() {
        return true;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = new Expression(type, lastMatchTrimmed());
        result.addChildren(right());
        return result;
    }

}
