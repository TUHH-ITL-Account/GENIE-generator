package generator.exercises.implementations;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.types.GenerationTask;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class SelectUnitForVariableFromSCTest extends FileWritingTest {

  @Test
  public void SelectUnitForVariableFromSC()
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
    dummyTask.getParameters().put("difficulty", 5);
    FdlNode node = c.getFdlnodes().get("Massestrom_Eq1");

    SelectUnitForVariableFromSC exercise = new SelectUnitForVariableFromSC(node, dummyTask);
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
}
