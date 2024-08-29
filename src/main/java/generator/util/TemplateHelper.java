package generator.util;

import generator.caching.FdlNode;
import generator.exercises.inputs.classes.AbstractExercise;
import generator.types.GenerationTask;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class TemplateHelper {

  /**
   * Returns a dummy instance of a template or null if it is not a subclass of AbstractExercise or
   * an error occurred.
   *
   * @param template class of the template
   * @return AbstractExercise instance or null
   */
  public static AbstractExercise getDummyInstanceFromClass(Class<?> template) {
    AbstractExercise ret = null;
    try {
      Object inst = template.getConstructor().newInstance();
      if (inst instanceof AbstractExercise) {
        ret = (AbstractExercise) inst;
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public static AbstractExercise getInstanceFromClass(Class<?> template, FdlNode node,
      GenerationTask task) {
    AbstractExercise ret = null;
    try {
      Object inst = template.getConstructor(FdlNode.class, GenerationTask.class)
          .newInstance(node, task);
      if (inst instanceof AbstractExercise) {
        ret = (AbstractExercise) inst;
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public static String[] getAdditionalFdls(Map<String, Object> parameters) {
    String specificDistractor = (String) parameters.get("additional_fdls");
    if (specificDistractor == null) {
      return null;
    }
    specificDistractor = specificDistractor.replaceAll("\\s", "");
    return specificDistractor.split(",");
  }

}
