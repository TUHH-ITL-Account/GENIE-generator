package generator.exercises.inputs.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.types.GenerationTask.EXERCISE_TYPE;
import generator.types.GenerationTask.SOLUTION_TYPE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractExercise {

  protected final FdlNode fdlNode;
  protected final GenerationTask task;
  /**
   * Prefix to be used when giving html input-elements ids and names
   */
  protected final String HTML_ID_NAME_PREFIX = "ANSWER";
  private final boolean kennen;
  private final boolean koennen;
  private final boolean verstehen;
  private final FdlTypes requiredFdlType;
  protected String template;
  protected String title;
  protected List<String> texts;
  protected List<String> additionalElements;

  /**
   * Setting enabled to false will exclude the template from being chosen if not set explicitly by
   * the parameter "template_actual". Note that you need to override isEnabled() to return false to
   * achieve disabling.
   **/
  protected boolean enabled = true;

  /**
   * A map object indicating the exercise solutions per input field, depending on the exercise type.
   * The key is the generated id/name of the html element. For input-fields of arbitrary text the
   * value is the solution. E.g.: { "ANSWER1" -> "420.5" } For checkboxes and radio buttons as per
   * multiple/single choice exercises, only the correct answer(s) will be added to the map with a
   * value of "TRUE". E.g.: { "ANSWER2" -> "TRUE" }
   */
  protected Map<String, Object> solutionMap;

  public AbstractExercise(boolean kennen, boolean koennen, boolean verstehen, FdlNode fdlNode,
      FdlTypes requiredFdlType, GenerationTask task) {
    if (!(kennen || koennen || verstehen)) {
      throw new IllegalArgumentException("Exercise types must fulfill at least 1 KKV dimension.");
    }
    this.kennen = kennen;
    this.koennen = koennen;
    this.verstehen = verstehen;
    this.title = "";
    this.texts = new ArrayList<>();
    this.fdlNode = fdlNode;
    this.requiredFdlType = requiredFdlType;
    this.task = task;
    additionalElements = new ArrayList<>();
  }

  public boolean isKennen() {
    return kennen;
  }

  public boolean isKoennen() {
    return koennen;
  }

  public boolean isVerstehen() {
    return verstehen;
  }

  public FdlNode getFdlNode() {
    return fdlNode;
  }

  public boolean hasSymbol() {
    return this.fdlNode != null;
  }

  public FdlTypes getRequiredFdlType() {
    return requiredFdlType;
  }

  public String getTitle() {
    return title;
  }

  public List<String> getTexts() {
    return texts;
  }

  public String getTemplate() {
    return template;
  }

  protected abstract void fillTitle();

  protected abstract void fillText();

  public abstract void generateAbstractExercise() throws Exception;

  protected abstract void generateSolutionMap();

  public void generateFromTask(Configuration cfg) throws JsonProcessingException {
    if (task.getExType() == EXERCISE_TYPE.FULL || task.getExType() == EXERCISE_TYPE.FULL_HTML) {
      generateFullHtmlExercise(cfg);
    }
    if (task.getExType() == EXERCISE_TYPE.FULL || task.getExType() == EXERCISE_TYPE.PART_HTML) {
      generatePartHtmlExercise(cfg);
    }
    //if(task.getExType() == EXERCISE_TYPE.FULL || task.getExType() == EXERCISE_TYPE.TEX) { } // STUB
    if (task.getSolType() == SOLUTION_TYPE.FULL || task.getSolType() == SOLUTION_TYPE.FULL_HTML) {
      generateFullHtmlSolution(cfg);
    }
    if (task.getSolType() == SOLUTION_TYPE.FULL || task.getSolType() == SOLUTION_TYPE.PART_HTML) {
      generatePartHtmlSolution(cfg);
    }
    //if(task.getSolType() == SOLUTION_TYPE.FULL || task.getSolType() == SOLUTION_TYPE.TEX) { } // STUB
    if (task.getSolType() == SOLUTION_TYPE.FULL || task.getSolType() == SOLUTION_TYPE.JSON) {
      generateJsonSolution();
    }
  }

  public void generatePartHtmlSolution(Configuration cfg) {
    this.task.setPartHtmlSolution(generateHtml(cfg, true, false));
  }

  public void generatePartHtmlExercise(Configuration cfg) {
    this.task.setPartHtmlExercise(generateHtml(cfg, false, false));
  }

  public void generateFullHtmlSolution(Configuration cfg) {
    this.task.setFullHtmlSolution(generateHtml(cfg, true, true));
  }

  public void generateFullHtmlExercise(Configuration cfg) {
    this.task.setFullHtmlExercise(generateHtml(cfg, false, true));
  }

  public void generateJsonSolution() throws JsonProcessingException {
    if (solutionMap == null) {
      generateSolutionMap();
    }
    ObjectMapper mapper = new ObjectMapper();
    task.setJsonSolution(mapper.writeValueAsString(solutionMap));
  }

  private String generateHtml(Configuration cfg, boolean isSolution, boolean fullHtml) {
    Map<String, Object> data = new HashMap<>();
    data.put("exercise", this);
    data.put("isSolution", isSolution);
    data.put("fullHtml", fullHtml);

    String templateName;
    if (this.template != null) {
      templateName = this.template;
    } else {
      String[] split = this.getClass().getName().split("\\.");
      templateName = split[split.length - 1];
    }
    return applyTemplate(cfg, templateName, data);
  }

  private String generateTex(Configuration cfg, boolean isSolution) {
    Map<String, Object> data = new HashMap<>();
    data.put("exercise", this);
    data.put("isSolution", isSolution);

    String templateName;
    if (this.template != null) {
      templateName = this.template;
    } else {
      String[] split = this.getClass().getName().split("\\.");
      templateName = split[split.length - 1] + "_TEX";
    }
    return applyTemplate(cfg, templateName, data);
  }

  private String applyTemplate(Configuration cfg, String templateName, Map<String, Object> data) {
    Template template;
    try {
      template = cfg.getTemplate(templateName + ".ftl");
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(baos);
    try {
      template.process(data, out);
    } catch (TemplateException | IOException e) {
      e.printStackTrace();
    }

    return baos.toString();
  }

  public List<String> getAdditionalElements() {
    return additionalElements;
  }

  /**
   * @return difficulty parameter or 5
   */
  public int getDifficultyOrDefault() {
    return getDifficultyOrDefault(5);
  }

  /**
   * Checks if difficulty parameter is present and returns it. If not the given value is added as
   * difficulty before returning.
   *
   * @param defaultDifficulty set difficulty value if parameter is absent
   * @return difficulty parameter value
   */
  public int getDifficultyOrDefault(int defaultDifficulty) {
    Integer diff = (Integer) task.getParameters().get("difficulty");
    if (diff == null) {
      task.getParameters().put("difficulty", defaultDifficulty);
      return defaultDifficulty;
    }
    return diff;
  }

  /**
   * Overwrite this method if your template has special requirements towards the FDLs.
   *
   * @param sym A symbol to test compatibility on
   * @return A boolean
   */
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return FdlDslSymbolHelper.getFdlType(sym) == requiredFdlType;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
