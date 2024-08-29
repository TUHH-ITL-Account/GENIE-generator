package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TextMultipleChoiceExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "EXISTENCE";

  protected final Set<String> correctSet;
  protected final Set<String> wrongSet;
  protected final Set<String> ownCorrect;
  protected final List<String> correctOptions;
  protected final List<String> wrongOptions;
  protected List<String> options;

  public TextMultipleChoiceExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    correctSet = new HashSet<>();
    wrongSet = new HashSet<>();
    ownCorrect = new HashSet<>();
    correctOptions = new ArrayList<>();
    wrongOptions = new ArrayList<>();
    options = new ArrayList<>();
  }

  protected abstract void fillCorrectSet() throws UnfulfillableException;

  protected abstract void fillWrongSet() throws UnfulfillableException;

  protected abstract void fillCorrectOptions() throws UnfulfillableException;

  protected abstract void fillWrongOptions() throws UnfulfillableException;

  /**
   * Merges copies of correct and wrong options and shuffles resulting concatenation
   */
  public void calcOptions() {
    options = new ArrayList<>(this.correctOptions);
    options.addAll(new ArrayList<>(this.wrongOptions));
    Collections.shuffle(this.options); //todo: überlegen ob seed abhängigkeit nötig
  }

  public void generateAbstractExercise() throws UnfulfillableException {
    fillTitle();
    fillText();
    fillCorrectSet();
    fillWrongSet();
    fillCorrectOptions();
    fillWrongOptions();
    calcOptions();
  }

  public List<String> getCorrectOptions() {
    return correctOptions;
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
    for (String correct : correctOptions) {
      solutionMap.put(HTML_ID_NAME_PREFIX + options.indexOf(correct), "true");
    }
  }
}
