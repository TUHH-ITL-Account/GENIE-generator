package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.HashMap;

public abstract class CalculationExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "NUMBER";
  protected String answer;

  public CalculationExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    this.answer = "";
  }

  protected abstract void fillAnswer();

  public void generateAbstractExercise() throws Exception {
    this.fillTitle();
    this.fillText();
    this.fillAnswer();
  }

  public String getAnswer() {
    return answer;
  }

  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    solutionMap.put(HTML_ID_NAME_PREFIX + 0, answer);
  }

}
