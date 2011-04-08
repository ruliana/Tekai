package tekai.standard;

import tekai.Expression;
import tekai.Parselet;

public class BeforeMiddleAfterParselet extends Parselet {

    private final String startingRegularExpression;
    private final String interpolationRegularExpression;
    private final String endingRegularExpression;
    private final String type;

    public BeforeMiddleAfterParselet(int precedence, String startingRegularExpression, String interpolationRegularExpression, String endingRegularExpression, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.interpolationRegularExpression = interpolationRegularExpression;
        this.endingRegularExpression = endingRegularExpression;
        this.type = type;
    }

    public boolean isLeftAssociativity() {
      return startingRegularExpression == null;
    }

    @Override
    public boolean isPrefixParselet() {
        return startingRegularExpression != null;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression == null
            ? interpolationRegularExpression
            : startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = new Expression(type, lastMatchTrimmed());

        if (endingRegularExpression != null && canConsume(endingRegularExpression)) return result;

        if (startingRegularExpression == null) result.addChildren(left());

        if (interpolationRegularExpression != null) {
            do {
                result.addChildren(nextExpression());
            } while (canConsume(interpolationRegularExpression));
        }

        if (endingRegularExpression != null) consumeIf(endingRegularExpression);

        return result;
    }
}
