package tekai.language;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import tekai.Expression;
import tekai.Parselet;
import tekai.Parser;
import tekai.standard.AtomParselet;
import tekai.standard.BeforeMiddleAfterParselet;

public class TekaiLang {

    private Parser parser;
    private int precedence = 1;
    private final int ATOM = precedence++;
    private final int EXPRESSION = precedence++;
    private final int REGULAR_EXPRESSION = precedence++;
    private final int DEFINITION = precedence++;

    private LinkedList<Expression> parsed = new LinkedList<Expression>();
    private Parser language;

    public TekaiLang() {
        parser = new Parser();
        parser.register(new BeforeMiddleAfterParselet(DEFINITION, "([\\w\\-]+) *?=", " *", null , "Definition"));
        parser.register(new AtomParselet(REGULAR_EXPRESSION, "\\/(.*?)\\/", "RegularExpression"));
        parser.register(new AtomParselet(EXPRESSION, "<([\\w\\-]+)>", "Expression"));
        parser.register(new AtomParselet(ATOM, "([^ ]+)\\.{3}", "MultipleSymbols"));
        parser.register(new AtomParselet(ATOM, "[^ ]+", "Symbol"));

        language = new Parser();
    }

    public void define(String string) {
        parsed.add(parser.parse(string));
    }

    public Expression parse(String string) {
        Iterator<Expression> iter = parsed.descendingIterator();
        int x = parsed.size() - 1;
        while (iter.hasNext())
            build(x++, iter.next());

        return language.parse(string);
    }

    public Expression debugLastExpression() {
        return parsed.getLast();
    }

    /**
     * YEAHH! This IS functional! No objects here.
     */
    private void build(int precedence, Expression expression) {
        if (expression.isType("Definition")) {
            buildDefinition(precedence, expression);
        } else {
            throw new RuntimeException("No build rule for \"" + expression + "\"");
        }
    }

    private void buildDefinition(final int precedence, final Expression expression) {
        assert precedence >= 0;
        assert expression != null;

        final String type = expression.getValue();
        MarkAndRest markAndRest = separateFirstMark(expression.getChildren());
        final boolean startsWithMark = markAndRest.startsWithMark;
        final Expression firstMark = markAndRest.mark;
        final List<Expression> expressions = markAndRest.rest;

        language.register(new Parselet() {

            @Override
            public int getPrecedence() {
                return precedence;
            }

            @Override
            public boolean isPrefixParselet() {
                return startsWithMark;
            }

            @Override
            public String startingRegularExpression() {
                return value(firstMark);
            }

            @Override
            protected Expression parse() {
                Expression result = new Expression(type, lastMatch());

                Iterator<Expression> exps = expressions.iterator();

                if (notPrefixParselet() && exps.hasNext() && isTypeExpression(exps.next()))
                    result.addChildren(left());

                if (notPrefixParselet() && isTypeMultiple(firstMark)) {
                    do {
                        result.addChildren(nextExpression(getPrecedence() + 1));
                    } while (canConsume(value(firstMark)));
                }

                while (exps.hasNext()) {
                    Expression exp = exps.next();
                    if (isTypeExpression(exp))
                        result.addChildren(nextExpression());
                    else if (isTypeMultiple(exp))
                        while (canConsume(value(exp)))
                            result.addChildren(nextExpression());
                    else if (isTypeMark(exp))
                        consumeIf(value(exp));
                    else
                        throw new RuntimeException("Not a recognizable expression:" + expression);
                }

                return result;
            }
        });
    }

    private String value(Expression expression) {
        assert expression != null;
        if (expression.isType("Symbol"))
            return Pattern.quote(expression.getValue());
        if (expression.isType("MultipleSymbols"))
            return Pattern.quote(expression.getValue());
        if (expression.isType("RegularExpression"))
            return expression.getValue();
        else
            throw new RuntimeException("Not a Symbol or RegularExpression: " + expression);
    }

    private boolean isTypeMark(Expression expression) {
        assert expression != null;
        return expression.isType("Symbol")
            || expression.isType("MultipleSymbols")
            || expression.isType("RegularExpression");
    }

    private boolean isTypeMultiple(Expression expression) {
        return expression.isType("MultipleSymbols");
    }

    private boolean isTypeExpression(Expression expression) {
        assert expression != null;
        return expression.isType("Expression");
    }

    private MarkAndRest separateFirstMark(List<Expression> all) {
        MarkAndRest result = new MarkAndRest();
        boolean found = false;
        for(Expression exp : all) {
            if (!found && isTypeMark(exp)) {
                result.mark = exp;
                if (result.rest.isEmpty()) result.startsWithMark = true;
                found = true;
            } else {
                result.rest.add(exp);
            }
        }
        return result;
    }

    private static class MarkAndRest {
        public boolean startsWithMark = false;
        public Expression mark = null;
        public List<Expression> rest = new LinkedList<Expression>();
    }
}
