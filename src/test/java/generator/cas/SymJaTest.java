package generator.cas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.math.MathException;

public class SymJaTest {


  // Test "Volumenstrom bei Förderung in Einzelgefäßen" Vs = (V/la)*v
  // Vs=25, V=?, la=50, v=5 -- expected: 25/5 * 50 = 250
  @Test
  public void testSymjaIntegration() {
    try {
      ExprEvaluator util = new ExprEvaluator();
      IExpr result = util.eval("Solve(25==V/50*5, V)");
      assertThat("output is expected 250", result.toString(), is("{{V->250}}"));
    } catch (SyntaxError se) {
      // catch Symja parser errors here
      se.printStackTrace();
    } catch (MathException me) {
      // catch Symja math errors here
      me.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRegexOnResultFormat() {
    String result = "{{V->250}}";
    assertThat("String format is matched by fitting regex",
        result.matches("\\{\\{(\\w)+->\\d+(\\.\\d+)?}}"));
    String solution = result.substring(2, result.length() - 2);
    assertThat("curly brackets are removed", solution, is("V->250"));
    assertThat("250 is properly extracted", solution.split("->")[1], is("250"));
  }

}
