package generator.fdldsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fdl.exceptions.DslException;
import fdl.fdl.FdlTool;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.FdlArtifactSymbol;
import org.junit.jupiter.api.Test;

public class FdlDslTest {

  @Test
  public void parseEquationFdl() throws DslException {
    FdlTool cli = new FdlTool();
    FdlArtifactSymbol artifact = cli.fullParse(
        "src/test/resources/knowledgemodels/TechnischeLogistikSS21/fdls/Massestrom_Eq1.fdl", 1);
    assertNotNull(artifact);
    assertFalse(artifact.getSpannedScope().getLocalEquationSymbols().isEmpty());
    //EquationSymbol symbol = artifact.getSpannedScope().getLocalEquationSymbols().get(0);
    EquationSymbol symbol = (EquationSymbol) artifact.getLocalSymbol();
    assertEquals(symbol.getAliasMap().size(), 4);
    assertEquals(symbol.getSpecificationMap().size(), 4);
  }
}
