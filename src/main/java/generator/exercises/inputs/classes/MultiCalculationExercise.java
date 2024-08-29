package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MultiCalculationExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "NUMBER";
  protected List<CalculationExercise> subExercises;
  protected List<List<String>> texts;
  protected List<String> answers;

  public MultiCalculationExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode node, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, node, requiredType, task);
    answers = new ArrayList<>();
    texts = new ArrayList<>();
  }

  protected abstract void fillAnswers();

  protected abstract void fillTexts();

  protected abstract void setupExercise() throws Exception;

  protected abstract void fillAdditionalElements();

  public void generateAbstractExercise() throws Exception {
    setupExercise();
    fillTitle();
    fillTexts();
    fillAnswers();
    fillAdditionalElements();
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
