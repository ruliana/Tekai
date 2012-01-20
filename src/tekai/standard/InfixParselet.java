package tekai.standard;

import tekai.Expression;
import tekai.Parselet;
import tekai.Parser;

import java.util.LinkedList;

import static java.util.Arrays.asList;

public class InfixParselet extends Parselet {

    private final String startingRegularExpression;
    private final String type;

    public InfixParselet(int precedence, String startingRegularExpression, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.type = type;
    }

    public InfixParselet(int precedence, String startingRegularExpression, String type, Parser leftParser, Parser rightParser) {
        this(precedence, startingRegularExpression, type);
        setLeftParser(leftParser);
        setParser(rightParser);
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
        Expression result = new Expression(type, lastMatch());
        result.addChildren(left(), right());
        return result;
    }
}
