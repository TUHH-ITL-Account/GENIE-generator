package generator.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import fdl.exceptions.DslException;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.caching.TopicNode;
import generator.exceptions.ModelException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class KnowledgeModelHelperTest {

  @Test
  public void testFindOtherFdlTo()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    FdlNode origin = c.getFdlnodes().get("Massestrom_Eq1");
    assertThat("node origin exists in model", origin, notNullValue());
    FdlNode toFind = KnowledgeModelHelper.findOtherFdlTo(origin, origin.getFdlType(), 0, null);
    assertThat("node toFind exists in model and was returned", toFind, notNullValue());
  }

  @Test
  public void testGetDistantTopicsDownFrom()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    FdlNode origin = c.getFdlnodes().get("Massestrom_Eq1");
    assertThat("node origin exists in model", origin, notNullValue());
    Map<Integer, List<TopicNode>> map = KnowledgeModelHelper.getDistantTopicsDownFrom(
        origin.getTopicNode(), 4);
    assertThat("map is not empty", map.isEmpty(), is(false));
  }

  @Test
  public void testGetFirstOtherFdlUpTo()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    FdlNode origin = c.getFdlnodes().get("Massestrom_Eq1");
    assertThat("node origin exists in model", origin, notNullValue());
    FdlNode toFind = KnowledgeModelHelper.getFirstOtherFdlUpTo(origin, origin.getFdlType(), -1,
        null);
    assertThat("node toFind exists in model and was returned", toFind, notNullValue());
  }

  @Test
  public void testCutExtension() {
    assertThat("Cut normal file.", KnowledgeModelHelper.cutExtension("Test.yaml"), is("Test"));
    assertThat("Cut of null is null.", KnowledgeModelHelper.cutExtension(null), nullValue());
    assertThat("Cut of extensionless name is name.", KnowledgeModelHelper.cutExtension("Test"),
        is("Test"));
    assertThat("Cut only after last '.'.", KnowledgeModelHelper.cutExtension("Test.final.yaml"),
        is("Test.final"));
  }

}
