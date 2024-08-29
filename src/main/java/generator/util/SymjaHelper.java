package generator.util;

import generator.exceptions.UnfulfillableException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.ComplexNum;
import org.matheclipse.core.interfaces.IExpr;

public class SymjaHelper {

  public static String getResult(IExpr result) throws UnfulfillableException {
    String res = result.toString();
    if (res.equals("{}")) {
      throw new UnfulfillableException("Got an empty result. Was the query unsolvable?");
    } else if (StringUtils.countMatches(res, "{") > 2) {
      //Always return the (assumed) biggest solution
      List<String> solutions = new ArrayList<>();
      Pattern pattern = Pattern.compile("\\{x->(.*?)}");
      Matcher matcher = pattern.matcher(res);
      while(matcher.find()) {
        solutions.add(matcher.group(1));
      }
      return solutions.get(solutions.size()-1);
    }

    // cut curly braces
    String substring = res.substring(2, res.length() - 2);
    if (res.matches("\\{\\{(\\w)+->\\d+(\\.\\d+)?}}") && !res.contains("(")) {
      // split at ->
      String[] split = substring.split("->");
      if (split.length == 2) {
        if(split[1].equals("Indeterminate")) {
          throw new UnfulfillableException("Got an indeterminate result.");
        }
        return split[1];
      }
    } else {
      String resultPart = substring.split("->")[1];
      if(resultPart.equals("Indeterminate")) {
        throw new UnfulfillableException("Got an indeterminate result.");
      }
      String query = "N(" + resultPart + ")";
      ExprEvaluator util = new ExprEvaluator();
      IExpr result2 = util.eval(query);
      if (result2 instanceof ComplexNum) {
        System.out.println("Got an imaginary result for " + query);
        throw new UnfulfillableException("Got an imaginary result");
      }
      return result2.toString();
    }
    // something went wrong
    return null;
  }
}