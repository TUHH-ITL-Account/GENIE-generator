package generator.util;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.types.containers.Reference;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

public class EquationSymbolHelperTest {

  @Test
  public void testGenerateVariablesWithRefs() throws Exception {
    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);

    FdlNode node = c.getFdlnodes().get("Gegengewicht_Kippsicherheit_Eq");
    Map<String, Reference> refs = ((EquationSymbol) node.getSymbol()).getReferenceMap();
    for (int i = 0; i < 100; i++) {
      Map<String, String> res = EquationSymbolsHelper.generateVariablesWithRefs(
          (EquationSymbol) node.getSymbol(), refs.keySet().stream().toList().get(0), null,
      null);
      for (Entry<String, String> entry : res.entrySet()) {
        double varVal = Double.parseDouble(entry.getValue());
        assertThat("Var " + entry.getKey() + " is within refs.", varVal,
            allOf(greaterThanOrEqualTo(refs.get(entry.getKey()).getMin()),
                lessThanOrEqualTo(refs.get(entry.getKey()).getMax())));
      }
    }
  }

}
