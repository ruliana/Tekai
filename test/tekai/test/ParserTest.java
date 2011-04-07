package tekai.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import tekai.Expression;
import tekai.Parselet;
import tekai.Parser;

public class ParserTest {

    @Test
    public void justAnAtom() {
        assertParsing("Just a number", "[1]:NUMBER", "1");
        assertParsing("Just an identifier", "[abc]:IDENTIFIER", "abc");
    }

    @Test
    public void simpleExpression() {
        assertParsing("Simple infix", "([+]:PLUS [1]:NUMBER [2]:NUMBER)", "1 + 2");
        assertParsing("Double infix (left associativity)", "([+]:PLUS ([+]:PLUS [1]:NUMBER [2]:NUMBER) [3]:NUMBER)", "1 + 2 + 3");
        assertParsing("Double infix with parenthesis", "([+]:PLUS [1]:NUMBER ([+]:PLUS [2]:NUMBER [3]:NUMBER))", "1 + (2 + 3)");
    }

    @Test
    public void functions() {
        assertParsing("[abc]:FUNCTION", "abc()");
        assertParsing("([abc]:FUNCTION [1]:NUMBER)", "abc(1)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER)", "abc(1, 2)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER [3]:NUMBER)", "abc(1, 2, 3)");
        assertParsing("([+]:PLUS ([abc]:FUNCTION [4]:NUMBER) ([def]:FUNCTION [3]:NUMBER [2]:NUMBER))", "abc(4) + def(3, 2)");
        assertParsing("([abc]:FUNCTION ([+]:PLUS ([+]:PLUS [2]:NUMBER [1]:NUMBER) [3]:NUMBER))", "abc((2 + 1) + 3)");
        assertParsing("([+]:PLUS ([+]:PLUS ([+]:PLUS [1]:NUMBER ([abc]:FUNCTION ([+]:PLUS [2]:NUMBER [3]:NUMBER) [4]:NUMBER)) [5]:NUMBER) [6]:NUMBER)", "(1 + abc(2 + 3, 4) + 5) + 6");
        assertParsing("([abc]:FUNCTION ([def]:FUNCTION [1]:NUMBER) ([ghi]:FUNCTION [2]:NUMBER))", "abc(def(1), ghi(2))");
    }

    @Test
    public void selectFrom() {
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [campo1]:IDENTIFIER [campo2]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER [tabela2]:IDENTIFIER))", "SELECT campo1, campo2 FROM tabela, tabela2");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT * FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER ([ INNER JOIN]:JOIN [outra_tabela]:IDENTIFIER [xxx]:IDENTIFIER)))", "SELECT * FROM tabela INNER JOIN outra_tabela ON xxx");
        
    }

    @Test
    public void selectWithWhere(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([+]:PLUS [campo]:IDENTIFIER [2]:NUMBER)))", "SELECT  * FROM tabela WHERE campo + 2");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [campo]:IDENTIFIER [2]:NUMBER) ([=]:OPERATOR [id]:IDENTIFIER [3]:NUMBER)))", "SELECT * FROM tabela WHERE campo = 2 AND id = 3");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [campo]:IDENTIFIER [2]:NUMBER) ([=]:OPERATOR [id]:IDENTIFIER [3]:NUMBER) ([=]:OPERATOR [campo]:IDENTIFIER [5.5]:NUMBER)))","SELECT * FROM tabela WHERE campo = 2 AND id = 3 OR campo = 5.5");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [campo]:IDENTIFIER [2]:NUMBER) ([=]:OPERATOR [id]:IDENTIFIER [35.89]:NUMBER) ([=]:OPERATOR [campo]:IDENTIFIER [5]:NUMBER)))","SELECT * FROM tabela WHERE (campo = 2) AND id = 35.89 OR (campo = 5)");
    }

    @Test
    public void selectWithAlias(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [tb.campo1]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT tb.campo1 FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([AS]:ALIAS [campo]:IDENTIFIER [nome]:IDENTIFIER)) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT campo AS nome FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([AS]:ALIAS [tb.campo1]:IDENTIFIER [nome]:IDENTIFIER)) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT tb.campo1 AS nome FROM tabela");
    }

    @Test
    public void selectWithConcat(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [campo1]:IDENTIFIER [campo2]:IDENTIFIER)) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT campo1 || campo2 FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [campo1]:IDENTIFIER [campo2]:IDENTIFIER [campo3]:IDENTIFIER)) ([FROM]:FROM [tabela]:IDENTIFIER))",
                "SELECT campo1 || campo2 || campo3 FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [campo1]:IDENTIFIER [campo2]:IDENTIFIER ([abc]:FUNCTION [campo3]:IDENTIFIER [campo4]:IDENTIFIER))) ([FROM]:FROM [tabela]:IDENTIFIER))",
                "SELECT campo1 || campo2 || abc(campo3, campo4) FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [campo1]:IDENTIFIER ['string']:STRING ([abc]:FUNCTION [campo3]:IDENTIFIER [campo4]:IDENTIFIER))) ([FROM]:FROM [tabela]:IDENTIFIER))",
                "SELECT campo1 || 'string' || abc(campo3, campo4) FROM tabela");
    }

    @Test
    public void selectWithLasers(){
        assertParsing("", " SELECT C120.idcomercial, "
            + "        C120.idnome, "
            + "        X040.razsoc, "
            + "        X040.docto1 AS cnpj,  "
            + "        X030.nomcid AS municipio,  "
            + "        X030.uf AS uf, "
            + "        chave_acesso = '                              ', "
            + "        data_acesso = '00/00/0000 00:00:00', "
            + "        X040.docto2 AS inscricao "
            + " FROM ACT12000 AS C120 "
            + " INNER JOIN AXT04000 AS X040 ON X040.idnome = C120.idnome "
            + "   INNER JOIN AXT02000 AS X020A ON X020A.idparametro = C120.sitsis "
            + "   INNER JOIN AXT02000 AS X020B ON X020B.idparametro = C120.sitcom "
            + "   INNER JOIN AXT02000 AS X020C ON X020C.idparametro = C120.sitlas "
            + "   INNER JOIN AXT03000 AS X030  ON X030.idcidade     = X040.idcidade ");
     }

     @Test
    public void selectOrder(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [campo]:IDENTIFIER [2]:NUMBER)) ([ORDER BY]:ORDER [campo2]:IDENTIFIER))", "SELECT  * FROM tabela WHERE campo = 2 ORDER BY campo2");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [campo]:IDENTIFIER [2]:NUMBER)) ([ORDER BY]:ORDER [campo2]:IDENTIFIER [campo3]:IDENTIFIER [campo4]:IDENTIFIER))", "SELECT  * FROM tabela WHERE campo = 2 ORDER BY campo2, campo3, campo4");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER) ([ORDER BY]:ORDER [campo2]:IDENTIFIER [campo3]:IDENTIFIER [campo4]:IDENTIFIER))", "SELECT  * FROM tabela ORDER BY campo2, campo3, campo4");


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
        final int OPERATOR = x++;
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
                Expression result = new Expression("SQL", "SQL");

                Expression fields = new Expression("SELECT", "SELECT");
                do {
                    
                    fields.addChildren(nextExpression());
                } while (canConsume("\\,"));

                consumeIf("FROM");

                Expression from = new Expression("FROM", "FROM");
                do{
                    from.addChildren(nextExpression());
                }while(canConsume("\\,"));
                

                if (canConsume("INNER( OUTER|RIGHT)? JOIN")) {
                    Expression join = new Expression("JOIN", lastMatchTrimmed());
                    join.addChildren(nextExpression());
                    consumeIf("ON");
                    join.addChildren(nextExpression());
                    from.addChildren(join);
                }
                result.addChildren(fields, from);
                
                if(canConsume("WHERE")){
                    Expression where = new Expression("WHERE", "WHERE");
                    do{
                        where.addChildren(nextExpression());
                    }while(canConsume("AND|\\bOR\\b"));
                   result.addChildren(where);
                }

                if(canConsume("(ORDER BY)")){
                    Expression order = new Expression("ORDER", "ORDER BY");
                    do{
                        order.addChildren(nextExpression());
                    }while(canConsume("\\,"));
                    result.addChildren(order);
                }

                return result;
            }
        });

        // NUMBER
        parser.register(new Parselet(ATOM) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "\\d+";
            }

            @Override
            public Expression parse() {
                if(canConsume("\\."))
                    return new Expression("NUMBER", originalMatchTrimmed().concat("." + right().getValue()));
                else
                    return new Expression("NUMBER", originalMatchTrimmed());
            }
        });

        //STRING
        parser.register(new Parselet(ATOM) {

            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "\\'([^\\']+)?\\'";
            }

            @Override
            protected Expression parse() {
                return new Expression("STRING", originalMatchTrimmed());
            }
        });

        //ALIAS
        parser.register(new Parselet(ATOM) {

            @Override
            public boolean isPrefixParselet() {
                return false;
            }

            @Override
            public boolean isLeftAssociativity(){
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "AS";
            }

            @Override
            protected Expression parse() {
                Expression result = new Expression("ALIAS", "AS");
                result.addChildren(left(), right());
                return result;
            }
        });

        //CONCAT
        parser.register(new Parselet(ATOM) {

            @Override
            public boolean isPrefixParselet() {
                return false;
            }

            @Override
            public boolean isLeftAssociativity(){
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "\\|\\|";
            }

            @Override
            protected Expression parse() {
                Expression result = new Expression("CONCAT", originalMatchTrimmed());
                result.addChildren(left());
                do{
                    result.addChildren(nextExpression());
                }while(canConsume("\\|\\|"));
                return result;
            }
        });

        // PLUS
         parser.register(new Parselet(OPERATOR) {
            @Override
            protected boolean isLeftAssociativity() {
                return true;
            }

            @Override
            public boolean isPrefixParselet() {
                return false;
            }

            @Override
            public String startingRegularExpression() {
                return "\\+";
            }

            @Override
            public Expression parse() {
                Expression result = new Expression("PLUS", originalMatchTrimmed());
                result.addChildren(left(), right());
                return result;
            }
        });

        // OPERATOR
        parser.register(new Parselet(OPERATOR) {
            @Override
            protected boolean isLeftAssociativity() {
                return true;
            }

            @Override
            public boolean isPrefixParselet() {
                return false;
            }

            @Override
            public String startingRegularExpression() {
                return "\\=";
                
            }

            @Override
            public Expression parse() {
                Expression result = new Expression("OPERATOR", originalMatchTrimmed());
                result.addChildren(left(), right());
                return result;
            }
        });

        // GROUPING (parenthesis)
        parser.register(new Parselet(FUNCTION) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "\\(";
            }

            @Override
            public Expression parse() {
                Expression result = right();
                consumeIf("\\)");
                return result;
            }
        });


        // IDENTIFIER
        parser.register(new Parselet(ATOM) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

             @Override
            protected boolean isLeftAssociativity() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "\\w+|\\*";
            }

            @Override
            public Expression parse() {
                if(canConsume("\\."))
                    return new Expression("IDENTIFIER", originalMatchTrimmed().concat("."+right().getValue()));
                else
                    return new Expression("IDENTIFIER", originalMatchTrimmed());
            }
        });

        // FUNCTION
        parser.register(new Parselet(FUNCTION) {
            @Override
            public boolean isPrefixParselet() {
                return false;
            }

            @Override
            public String startingRegularExpression() {
                return "\\(";
            }

            @Override
            public Expression parse() {
                Expression result = new Expression("FUNCTION", left().getValue());

                if (canConsume("\\)")) return result;

                do {
                    result.addChildren(nextExpression());
                } while (canConsume(","));

                consumeIf("\\)");

                return result;
            }
        });
        
    }
}