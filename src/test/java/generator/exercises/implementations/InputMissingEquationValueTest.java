package generator.exercises.implementations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fdl.exceptions.DslException;
import fdl.fdl.FdlTool;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.FdlArtifactSymbol;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import generator.types.GenerationTask;
import generator.util.EquationSymbolsHelper.ValueFiller;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class InputMissingEquationValueTest extends FileWritingTest {

  @Test
  public void testInputMissingEquationValue()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException, UnfulfillableException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
    try {
      cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);

    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS21", "Stückgut",
        "Massestrom_Eq1");
    dummyTask.initMissingWithDefaults();
    dummyTask.getParameters().put("difficulty", 6);
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq1");

    InputMissingEquationValue exercise = new InputMissingEquationValue(node, dummyTask);
    exercise.generateAbstractExercise();
    exercise.generateFullHtmlExercise(cfg);
    exercise.generateFullHtmlSolution(cfg);

    String htmlExercise = dummyTask.getFullHtmlExercise();
    String htmlSolution = dummyTask.getFullHtmlSolution();

    assertThat("htmlExercise is not null", htmlExercise, notNullValue());
    assertThat("htmlSolution is not null", htmlSolution, notNullValue());
    assertThat("htmlExercise is not empty", htmlExercise, not(""));
    assertThat("htmlSolution is not empty", htmlSolution, not(""));
    assertThat("exercise is not equal to solution", htmlExercise, not(htmlSolution));

    // todo: ordentliche file checks
    writeIntoFile(exercise.getClass().getSimpleName() + "_exercise", htmlExercise);
    writeIntoFile(exercise.getClass().getSimpleName() + "_solution", htmlSolution);
  }

  @Test
  public void testValueFiller() throws DslException {
    FdlTool cli = new FdlTool();
    FdlArtifactSymbol a = cli.fullParse(
        "./src/test/resources/knowledgemodels/TechnischeLogistikSS21/fdls/Fachlast_Eq.fdl");
    EquationSymbol sym = (EquationSymbol) a.getLocalSymbol();
    Map<String, String> valMap = new HashMap<>();
    valMap.put("Q_{AB}", "10");
    valMap.put("TL_{BW}", "2");
    Map<String, List<String>> listMap = new HashMap<>();
    listMap.put("Q_{LE}", new ArrayList<>());
    listMap.get("Q_{LE}").add("11");
    listMap.get("Q_{LE}").add("22");
    listMap.get("Q_{LE}").add("33");

    ValueFiller vf = new ValueFiller(valMap, listMap, sym);
    sym.getRootExpression().accept(vf);
    assertThat("Sum was unfolded as expected.", vf.getExpressionString(), is("==(11+22+33+10)*2"));
  }

  @Test
  public void testInputMissingEquationValueJsonSolution()
      throws Exception {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
    try {
      cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);

    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS21", "Stückgut",
        "Massestrom_Eq1");
    dummyTask.initMissingWithDefaults();
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq1");

    InputMissingEquationValue exercise = new InputMissingEquationValue(node, dummyTask);
    exercise.generateAbstractExercise();
    exercise.generateJsonSolution();

    String jsonSolution = dummyTask.getJsonSolution();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> map = mapper.readValue(jsonSolution, new TypeReference<>() {
    });

    assertThat("jsonSolution is not null", jsonSolution, notNullValue());
    assertThat("map is not null", map, notNullValue());
    assertThat("jsonSolution is not empty", jsonSolution, not(""));
    assertThat("map is not empty", !map.isEmpty());
    assertThat("SOLUTION_FORMAT is defined in map", map.get("SOLUTION_FORMAT"), is("NUMBER"));
    assertThat("There is a solution in the map", map.size(), greaterThan(1));
  }
}
