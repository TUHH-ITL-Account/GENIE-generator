package generator.caching;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import generator.exceptions.UnfulfillableException;
import generator.exercises.implementations.SelectExamplesFromMC;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;

public class TemplateTest {

  @Disabled
  public void testTemplate() throws UnfulfillableException {
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

    //test exercise
    SelectExamplesFromMC exercise = new SelectExamplesFromMC();
    exercise.generateAbstractExercise(); // todo fix

    //data model
    Map<String, Object> data = new HashMap<>();
    data.put("exercise", exercise);

    Template template;
    try {
      template = cfg.getTemplate("SelectExamplesFromMC.ftl");
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    Writer out = new OutputStreamWriter(System.out);
    try {
      template.process(data, out);
    } catch (TemplateException | IOException e) {
      e.printStackTrace();
    }

    //String output = out.toString();
  }

}
