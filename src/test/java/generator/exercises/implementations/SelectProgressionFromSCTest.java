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

public class SelectProgressionFromSCTest extends FileWritingTest {

  @Test
  public void testSelectProgressionFromSC()
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
    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS21", "Stetigförderer",
        "Example_Pro");
    dummyTask.initMissingWithDefaults();
    dummyTask.getParameters().put("difficulty", 5);
    FdlNode node = c.getFdlnodes().get("Example_Pro");

    SelectProgressionFromSC exercise = new SelectProgressionFromSC(node, dummyTask);
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

}
