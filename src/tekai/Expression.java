package tekai;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Expression {

    private final String identifier;
    private final List<String> data;
    private final List<Expression> children = new LinkedList<Expression>();

    // == Construction

    public Expression(String identifier, String... data) {
        this.identifier = identifier;
        this.data = Arrays.asList(data);
    }

    public void addChildren(Expression... expressions) {
        for(Expression expression : expressions)
            children.add(expression);
    }

    // == Accessors ==

    public List<String> getData() {
        return data;
    }

    public String getDataFirstElement() {
        return data.get(0);
    }

    // == Helpers ==

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return data + ":" + identifier;
        } else {
            return "(" + data + ":" + identifier + " " + joinChildren() + ")";
        }
    }

    public String joinChildren() {
        if (children == null || children.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        Iterator<Expression> iterator = children.iterator();
        result.append(iterator.next().toString());

        while(iterator.hasNext()) {
            Expression element = iterator.next();
            result.append(" ");
            result.append(element.toString());
        }

        return result.toString();
    }
}
