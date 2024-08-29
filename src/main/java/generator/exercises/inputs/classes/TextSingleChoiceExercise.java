package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class TextSingleChoiceExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "CHOICE";
  protected final List<String> wrongOptions;
  protected String correctOption;
  protected List<String> options;

  public TextSingleChoiceExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    this.correctOption = "";
    this.wrongOptions = new ArrayList<>();
    this.options = new ArrayList<>();
  }

  protected abstract void fillCorrectOption() throws UnfulfillableException;

  protected abstract void fillWrongOptions();

  /**
   * Merges copies of correct and wrong options and shuffles resulting concatenation
   */
  public void calcOptions() {
    this.options = new ArrayList<>();
    this.options.add(this.correctOption);
    this.options.addAll(new ArrayList<>(this.wrongOptions));
    Collections.shuffle(this.options); //todo: überlegen ob seed abhängigkeit nötig
  }

  public void generateAbstractExercise() throws Exception {
    this.fillTitle();
    this.fillText();
    this.fillCorrectOption();
    this.fillWrongOptions();
    this.calcOptions();
  }

  public String getCorrectOption() {
    return correctOption;
  }

  // todo: Diskussion mit Max
  public List<String> getWrongOptions() {
    return wrongOptions;
  }

  public List<String> getOptions() {
    return options;
  }

  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    solutionMap.put(HTML_ID_NAME_PREFIX, HTML_ID_NAME_PREFIX + options.indexOf(correctOption));
  }
}
