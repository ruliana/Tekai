package tekai;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author SFPISA
 */
public abstract class Printer {

    protected String printChildren(List<Expression> e) {
        return printChildren(e, ",");
    }

    protected String printChildren(List<Expression> e, String separator) {
        StringBuilder result = new StringBuilder();

        Iterator<Expression> iterator = e.iterator();
        if (iterator.hasNext())
            result.append(print(iterator.next()));

        while (iterator.hasNext()) {
            result.append(separator);
            result.append(print(iterator.next()));
        }

        return result.toString();
    }
    
    public abstract String print(Expression e);

}
