package generator;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fdl.exceptions.DslException;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import generator.exercises.implementations.SelectSolutionForEquationFromSC;
import generator.types.GenerationTask;
import generator.types.GenerationTask.SOLUTION_TYPE;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GeneratorTest {

  @Test
  public void testGeneratingFromFdl()
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException, IOException, DslException, ModelException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask task = new GenerationTask("TechnischeLogistikSS21", "Stückgut",
        "Massestrom_Eq1");
    task.initMissingWithDefaults();
    task.getParameters().put("koennen", true);
    task = generator.generateFromFdl("TechnischeLogistikSS21", "Stückgut", "Massestrom_Eq1", task);
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), notNullValue());
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), not(""));
  }

  @Test
  public void testGeneratingFromFdlWithTemplate()
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException, IOException, DslException, ModelException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask task = new GenerationTask("TechnischeLogistikSS21", "Stückgut",
        "Massestrom_Eq1");
    task.initMissingWithDefaults();
    task.getParameters().put("koennen", true);
    task.getParameters().put("template_actual", "InputMissingEquationValue");
    task = generator.generateFromFdl("TechnischeLogistikSS21", "Stückgut", "Massestrom_Eq1", task);
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), notNullValue());
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), not(""));
  }

  @Test
  public void testGeneratingFromTopic()
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, DslException, UnfulfillableException, IOException, ModelException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask task = new GenerationTask("TechnischeLogistikSS21", "Stückgut");
    task.initMissingWithDefaults();
    task.getParameters().put("koennen", true);
    task = generator.generateFromTopic("TechnischeLogistikSS21", "Stückgut", task);
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), notNullValue());
    assertThat("Exercise was generated.", task.getPartHtmlExercise(), not(""));
  }

  @Test
  public void testGeneratingFromCourse()
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, DslException, UnfulfillableException, IOException, ModelException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    for (int i = 0; i < 2000; i++) {
      GenerationTask task = new GenerationTask("TechnischeLogistikSS21");
      task.initMissingWithDefaults();
      try {
        task = generator.generateFromCourse("TechnischeLogistikSS21", task);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println(task.getParameters());
      }
      if (task.getPartHtmlExercise() == null) {
        System.out.println(task.getParameters());
      }
      assertThat("Exercise was generated.", task.getPartHtmlExercise(), notNullValue());
      assertThat("Exercise was generated.", task.getPartHtmlExercise(), not(""));
    }
  }

  @Test
  public void testJsonSolution()
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException, IOException, DslException, ModelException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    GenerationTask task = new GenerationTask("TechnischeLogistikSS21", "Stückgut",
        "Massestrom_Eq1");
    task.setSolType(SOLUTION_TYPE.JSON);
    task.initMissingWithDefaults();
    task.getParameters().put("koennen", true);
    task = generator.generateFromFdlWithClass("Massestrom_Eq1",
        SelectSolutionForEquationFromSC.class, task);
    assertThat("Solution was generated.", task.getJsonSolution(), notNullValue());
    assertThat("Solution was generated.", task.getJsonSolution(), not(""));
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> solMap = mapper.readValue(task.getJsonSolution(), new TypeReference<>() {
    });
    assertThat("Converted JSON contains solution.", solMap.get("ANSWER") != null);
  }

  @Test
  public void testReloadModel()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException {
    Generator generator = new Generator();
    generator.cacheModel("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    generator.generateFromCourse("TechnischeLogistikSS21", null);
    generator.reloadModel("TechnischeLogistikSS21", true);
    GenerationTask task = generator.generateFromCourse("TechnischeLogistikSS21", null);
    assertThat("Solution was generated.", task.getFullHtmlSolution(), notNullValue());
  }

}
