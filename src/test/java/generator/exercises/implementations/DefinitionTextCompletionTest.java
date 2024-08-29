package generator.exercises.implementations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fdl.exceptions.DslException;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.exceptions.ModelException;
import generator.types.GenerationTask;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DefinitionTextCompletionTest extends FileWritingTest {

  @Test
  public void testDefinitionTextCompletion()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
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
    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS21", "Schüttgut",
        "Schuettgut_Def");
    dummyTask.initMissingWithDefaults();
    dummyTask.getParameters().put("difficulty", 6);
    FdlNode node = c.getFdlnodes().get("Schuettgut_Def");

    DefinitionTextCompletion exercise = new DefinitionTextCompletion(node, dummyTask);
    exercise.generateAbstractExercise();
    exercise.generatePartHtmlExercise(cfg);
    exercise.generatePartHtmlSolution(cfg);

    String htmlExercise = dummyTask.getPartHtmlExercise();
    String htmlSolution = dummyTask.getPartHtmlSolution();

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
  public void testDefinitionTextCompletionSolutionMap()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
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
    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS21", "Schüttgut",
        "Schuettgut_Def");
    dummyTask.initMissingWithDefaults();
    dummyTask.getParameters().put("difficulty", 10);
    FdlNode node = c.getFdlnodes().get("Schuettgut_Def");

    DefinitionTextCompletion exercise = new DefinitionTextCompletion(node, dummyTask);
    exercise.generateAbstractExercise();
    exercise.generatePartHtmlExercise(cfg);
    exercise.generatePartHtmlSolution(cfg);
    exercise.generateJsonSolution();

    String htmlExercise = dummyTask.getPartHtmlExercise();
    String htmlSolution = dummyTask.getPartHtmlSolution();
    String jsonSolution = dummyTask.getJsonSolution();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> map = mapper.readValue(jsonSolution, new TypeReference<>() {
    });

    assertThat("jsonSolution is not null", jsonSolution, notNullValue());
    assertThat("map is not null", map, notNullValue());
    assertThat("jsonSolution is not empty", jsonSolution, not(""));
    assertThat("map is not empty", !map.isEmpty());
    assertThat("SOLUTION_FORMAT is defined in map", map.get("SOLUTION_FORMAT"), is("STRING"));
    assertThat("There is a solution in the map", map.size(), greaterThan(1));

    // todo: ordentliche file checks
    writeIntoFile("DefinitionTextCompletion_exercise", htmlExercise);
    writeIntoFile("DefinitionTextCompletion_exercise", htmlSolution);
  }

}
