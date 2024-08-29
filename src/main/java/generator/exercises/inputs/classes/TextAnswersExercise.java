package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TextAnswersExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "STRING";
  protected final List<String> answers;

  public TextAnswersExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    this.answers = new ArrayList<>();
  }

  protected abstract void fillAnswer();

  public void generateAbstractExercise() {
    this.fillTitle();
    this.fillText();
    this.fillAnswer();
  }

  public List<String> getAnswers() {
    return answers;
  }

  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    for (int i = 0; i < answers.size(); i++) {
      solutionMap.put(HTML_ID_NAME_PREFIX + i, answers.get(i));
    }
  }
}
