package tekai;

import static org.junit.Assert.assertEquals;
import static tekai.Expression.e;

import java.util.List;

import org.junit.Test;

public class TranformationTest {

    @Test
    public void rename() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Expression result = renameTransformation().applyOn(expression);

        assertEquals("([SUBSTR]:FUNCTION ['Some text']:STRING [0]:NUMBER [4]:NUMBER)", result.toString());
    }

    @Test
    public void changeParameterOrder() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Expression result = changeParameterOrderTransformation().applyOn(expression);

        assertEquals("([SUBSTRING]:FUNCTION ['Some text']:STRING [4]:NUMBER [0]:NUMBER)", result.toString());
    }

    @Test
    public void remove() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Expression result = removeTransformation().applyOn(expression);

        assertEquals("([SUBSTRING]:FUNCTION [0]:NUMBER [4]:NUMBER)", result.toString());
    }

    /**
     * Converte a Função CAST(campo as VARCHAR) do Postgres para Oracle
     * o VARCHAR:DATATYPE do Postgres deve passar para VARCHAR2:DATATYPE
     */
    @Test
    public void cast(){

       Expression e = e("CAST", "FUCNTION",
               e("campo", "IDENTIFIER"),
               e("VARCHAR", "DATATYPE"));

       assertEquals("([CAST]:FUNCTION [campo]:IDENTIFIER [VARCHAR2]:DATATYPE)", null );
    }

    private Transformation removeTransformation() {
        return new Transformation() {
            @Override
            public boolean when(Expression expression) {
                return expression.isType("STRING");
            }

            @Override
            public Expression then(String value, String type, List<Expression> children) {
                return null;
            }
        };
    }

    // == Helpers ==

    private Transformation renameTransformation() {
        return new Transformation() {
            @Override
            public boolean when(Expression expression) {
                return expression.isType("FUNCTION") && expression.hasValue("SUBSTRING");
            }

            @Override
            public Expression then(String value, String type, List<Expression> children) {
                return e("SUBSTR", type, children);
            }
        };
    }

    private Transformation changeParameterOrderTransformation() {
        return new Transformation() {
            @Override
            public boolean when(Expression expression) {
                return expression.isType("FUNCTION") && expression.hasValue("SUBSTRING");
            }

            @Override
            public Expression then(String value, String type, List<Expression> children) {
                return e(value, type, children.get(0), children.get(2), children.get(1));
            }
        };
    }
}
