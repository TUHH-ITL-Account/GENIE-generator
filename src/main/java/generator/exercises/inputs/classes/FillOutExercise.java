package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;

public abstract class FillOutExercise extends AbstractExercise {

  public FillOutExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode fdlNode, FdlTypes requiredFdlType,
      GenerationTask task) {
    super(kennen, koennen, verstehen, fdlNode, requiredFdlType, task);
  }
}
