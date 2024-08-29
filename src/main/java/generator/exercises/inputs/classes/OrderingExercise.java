package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OrderingExercise extends AbstractExercise {

  public final String SOLUTION_FORMAT = "MULTI_STRING";

  protected List<String> order; // the steps in the correct order
  protected Map<String, List<Integer>> multiOccurrences = new HashMap<>(); // e.g. multiOcc[2] -> {2,7}
  protected List<Integer> randomizedOrder;
  protected List<Integer> givenIndexes = new ArrayList<>();

  public OrderingExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    this.order = new ArrayList<>();
    this.randomizedOrder = new ArrayList<>();
  }

  protected abstract void fillOrder();

  protected abstract void fillRandomizedOrder();

  public void generateAbstractExercise() {
    this.fillTitle();
    this.fillText();
    this.fillOrder();
    this.fillRandomizedOrder();
  }

  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    for (int i = 0; i < randomizedOrder.size(); i++) {
      if (givenIndexes.contains(i)) {
        continue;
      }
      int curr = randomizedOrder.get(i);
      List<String> answers = new ArrayList<>();
      List<Integer> sames = multiOccurrences.get(String.valueOf(curr));
      if (sames != null) {
        for (Integer j : sames) {
          answers.add(String.valueOf(j + 1));
        }
      } else {
        answers.add(String.valueOf(curr + 1));
      }
      solutionMap.put(HTML_ID_NAME_PREFIX + i, answers);
    }
  }

  public List<String> getOrder() {
    return order;
  }

  public List<Integer> getRandomizedOrder() {
    return randomizedOrder;
  }

  public List<Integer> getGivenIndexes() {
    return givenIndexes;
  }

  public Map<String, List<Integer>> getMultiOccurrences() {
    return multiOccurrences;
  }
}
