package tekai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import tekai.standard.AtomParselet;
import tekai.standard.BeforeMiddleAfterParselet;
import tekai.standard.GroupingParselet;
import tekai.standard.InfixParselet;
import tekai.standard.PrefixParselet;

public class ParserTest {

    @Test
    public void justAnAtom() {
        assertParsing("Just a number", "[1]:NUMBER", "1");
        assertParsing("Just an identifier", "[abc]:IDENTIFIER", "abc");
    }

    @Test
    public void simpleExpression() {
        assertParsing("Simple infix", "([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER)", "1 + 2");
        assertParsing("Double infix (left associativity)", "([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER) [3]:NUMBER)", "1 + 2 + 3");
        assertParsing("Double infix with parenthesis", "([+]:ARITHMETIC [1]:NUMBER ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER))", "1 + (2 + 3)");
    }

    @Test
    public void functions() {
        assertParsing("[abc]:FUNCTION", "abc()");
        assertParsing("([abc]:FUNCTION [1]:NUMBER)", "abc(1)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER)", "abc(1, 2)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER [3]:NUMBER)", "abc(1, 2, 3)");
        assertParsing("([+]:ARITHMETIC ([abc]:FUNCTION [4]:NUMBER) ([def]:FUNCTION [3]:NUMBER [2]:NUMBER))", "abc(4) + def(3, 2)");
        assertParsing("([abc]:FUNCTION ([+]:ARITHMETIC ([+]:ARITHMETIC [2]:NUMBER [1]:NUMBER) [3]:NUMBER))", "abc((2 + 1) + 3)");
        assertParsing("([+]:ARITHMETIC ([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER ([abc]:FUNCTION ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER) [4]:NUMBER)) [5]:NUMBER) [6]:NUMBER)", "(1 + abc(2 + 3, 4) + 5) + 6");
        assertParsing("([abc]:FUNCTION ([def]:FUNCTION [1]:NUMBER) ([ghi]:FUNCTION [2]:NUMBER))", "abc(def(1), ghi(2))");
    }

    @Test
    public void selectFrom() {
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [campo1]:IDENTIFIER [campo2]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT campo1, campo2 FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT * FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER ([INNER JOIN]:JOIN [outra_tabela]:IDENTIFIER [xxx]:IDENTIFIER)))", "SELECT * FROM tabela INNER JOIN outra_tabela ON xxx");
    }

    @Test
    public void exceptions() {
        // TODO Launch specific exception to specific problems
        // TODO Add more and more contextual information to error messages
        try {
            parse("1 +");
            fail("Expected not able to parse an incomplete expression \"1 +\"");
        } catch (Exception e) {
            // success
        }
    }

    // == Helpers ==

    private void assertParsing(String expected, String source) {
        assertParsing(null, expected, source);
    }

    private void assertParsing(String message, String expected, String source) {
        Expression expression = parse(source);
        assertEquals(message, expected, expression.toString());
    }

    private Expression parse(String source) {
        Parser parser = new Parser(source);
        configureParser(parser);
        Expression expression = parser.parse();
        return expression;
    }

    private void configureParser(Parser parser) {
        // PRECEDENCE (What to parse first. Higher numbers means more precedence)
        int x = 1;
        final int ATOM = x++;
        final int OR = x++;
        final int AND = x++;
        final int NOT = x++;
        final int EQUALS = x++;
        final int MULTIPLY = x++;
        final int SUM = x++;
        final int GROUPING = x++;
        final int FUNCTION = x++;
        final int SELECT = x++;

        // SQL
        parser.register(new Parselet(SELECT) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "SELECT";
            }

            @Override
            public Expression parse() {
                Expression fields = new Expression("SELECT", "SELECT");
                do {
                    fields.addChildren(nextExpression());
                } while (canConsume(","));

                consumeIf("FROM");

                Expression from = new Expression("FROM", "FROM");
                from.addChildren(nextExpression());

                if (canConsume("INNER( OUTER|RIGHT)? JOIN")) {
                    Expression join = new Expression("JOIN", lastMatchTrimmed());
                    join.addChildren(nextExpression());
                    consumeIf("ON");
                    join.addChildren(nextExpression());
                    from.addChildren(join);
                }

                Expression result = new Expression("SQL", "SQL");
                result.addChildren(fields, from);
                return result;
            }
        });

        // BOOLEAN
        parser.register(new InfixParselet(OR, "\\b((?i)OR)\\b", "BOOLEAN"));
        parser.register(new InfixParselet(AND, "\\b((?i)AND)\\b", "BOOLEAN"));
        parser.register(new PrefixParselet(NOT, "\\b((?i)NOT)\\b", "BOOLEAN"));

        // ARITHMETIC
        parser.register(new InfixParselet(MULTIPLY, "(\\*|/|%)", "ARITHMETIC"));
        parser.register(new InfixParselet(SUM, "(\\+|-)", "ARITHMETIC"));

        //EQUALS (OPERATOR)
        parser.register(new InfixParselet(EQUALS, "=", "OPERATOR"));

        // GROUPING (parenthesis)
        parser.register(new GroupingParselet(GROUPING, "\\(", "\\)"));

        // FUNCTION
        parser.register(new BeforeMiddleAfterParselet(FUNCTION, "(\\w+)\\s*\\(", ",", "\\)", "FUNCTION"));

        //NUMBER
        parser.register(new AtomParselet(ATOM, "\\d+(?:\\.\\d+)?", "NUMBER"));

        //STRING
        parser.register(new AtomParselet(ATOM, "\\'[^\\']*?\\'", "STRING"));

        //IDENTIFIER
        parser.register(new AtomParselet(ATOM, "(\\w+\\.\\w+|\\w+|\\*)", "IDENTIFIER"));
    }
}