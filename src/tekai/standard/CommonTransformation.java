package tekai.standard;

import java.util.List;

import tekai.Expression;
import tekai.Transformation;

public class CommonTransformation extends Transformation {

    private final From from;
    private final To to;

    /**
     * Only possible to construct via static constructors "from...".
     */
    private CommonTransformation(From from, To to) {
        this.from = from;
        this.to = to;
    }

    // == When this

    public static CommonTransformation from(String matchValue, String matchType) {
        return new CommonTransformation(new From(matchValue, matchType), new To());
    }

    public static CommonTransformation fromValue(String matchValue) {
        return from(matchValue, null);
    }

    public static CommonTransformation fromType(String matchType) {
        return from(null, matchType);
    }

    // == Transform to this

    public CommonTransformation toValue(String value) {
        to.setValue(value);
        return this;
    }

    public CommonTransformation toType(String type){
        to.setType(type);
        return this;
    }

    public CommonTransformation toParamOrder(int... paramOrder) {
        to.setParamOrder(paramOrder);
        return this;
    }

    public CommonTransformation toNothing() {
        to.setNothing();
        return this;
    }

    // == Helper classes

    public static class From {
        private final String matchValue;
        private final String matchType;

        private From(String matchValue, String matchType) {
            this.matchValue = matchValue;
            this.matchType = matchType;
        }

        private boolean matches(Expression expression) {
            return (matchValue == null || expression.hasValue(matchValue))
                && (matchType == null  || expression.isType(matchType));
        }
    }

    public static class To {

        private String value;
        private String type;
        private int[] paramOrder;
        private boolean toNothing;

        private void setValue(String value) {
            this.value = value;
        }

        public void setParamOrder(int[] paramOrder) {
            this.paramOrder = paramOrder;
        }

        public void setNothing() {
            this.toNothing = true;
        }

        public void setType(String type){
            this.type = type;
        }

        private Expression makes(String oldValue, String oldType, List<Expression> oldChildren) {

            if (toNothing) return null;

            Expression result = null;
            if (value == null && type == null)
                result = new Expression(oldType, oldValue);
            else if(value != null && type != null)
                result = new Expression(type, value);
            else if(type == null)
                result = new Expression(oldType, value);
            else if(value == null)
                result = new Expression(type, oldValue);

            if (paramOrder == null)
                result.addChildren(oldChildren);
            else
                for(int i : paramOrder)
                    result.addChildren(oldChildren.get(i - 1));

            return result;
        }
    }

    // == Implementation

    @Override
    public boolean when(Expression expression) {
        return from.matches(expression);
    }

    @Override
    public Expression then(String value, String type, List<Expression> children) {
        return to.makes(value, type, children);
    }
}
