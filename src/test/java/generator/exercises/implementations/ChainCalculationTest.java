package generator.exercises.implementations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.types.GenerationTask;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ChainCalculationTest extends FileWritingTest {

  /**
   * Test with giving additional_fdls
   *
   * @throws Exception todo
   */
  @Test
  public void testExplicitChainCalculation()
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
    dummyTask.setCacheReference(c);
    dummyTask.initMissingWithDefaults();
    //dummyTask.getParameters().put("difficulty", 6);
    // SOLL Kette: Massestrom(Eq1)=Massestrom(Eq2) - Volumenstrom(Eq2)=Volumenstrom(Eq3)
    dummyTask.getParameters()
        .put("additional_fdls", "Massestrom_Eq2,Volumenstrom_Eq");
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq1");

    ChainCalculation exercise = new ChainCalculation(node, dummyTask);
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


  /**
   * Test without giving additional_fdls, fitting equation to chain with should be found
   *
   * @throws Exception todo
   */
  @Test
  public void testOpenChainCalculation()
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
        "Massestrom_Eq2");
    dummyTask.setCacheReference(c);
    dummyTask.initMissingWithDefaults();
    //dummyTask.getParameters().put("difficulty", 6);
    // SOLL Kette: Volumenstrom(Eq2)=Volumenstrom(Eq3)
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq2");

    ChainCalculation exercise = new ChainCalculation(node, dummyTask);
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
    writeIntoFile(exercise.getClass().getSimpleName() + "_exercise_open", htmlExercise);
    writeIntoFile(exercise.getClass().getSimpleName() + "_solution_open", htmlSolution);
  }

  @Test
  public void testChainCalculationJsonSolution()
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
        "Massestrom_Eq2");
    dummyTask.initMissingWithDefaults();
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq2");

    ChainCalculation exercise = new ChainCalculation(node, dummyTask);
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
