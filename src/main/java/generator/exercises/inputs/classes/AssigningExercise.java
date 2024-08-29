package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents exercises where "categories" and terms which (might) belong to them are
 * given. The learner must then assign the terms to the terms accordingly. [Deutsch: Zuordnung]
 */
public abstract class AssigningExercise extends AbstractExercise {

  //protected final List<String> categories;

  // category -> list of terms for that category
  protected final Map<String, List<String>> termMap;

  public AssigningExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    //this.categories = new ArrayList<>();
    this.termMap = new HashMap<>();
  }

  protected abstract void fillCategories();

  protected abstract void fillTerms();

  public void generateAbstractExercise() {
    this.fillTitle();
    this.fillText();
    this.fillCategories();
    this.fillTerms();
  }

  public Map<String, List<String>> getTermMap() {
    return termMap;
  }
}
