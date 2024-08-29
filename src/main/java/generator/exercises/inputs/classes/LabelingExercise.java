package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents exercises where a learner has to label certain points in a given image.
 * [Deutsch: Beschriftung]
 */
public abstract class LabelingExercise extends FillOutExercise {

  // label -> coordinates on image
  // in future: multiple coords possible?
  protected Map<String, Integer[]> labelMap;
  protected String imageSource;

  public LabelingExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
    this.labelMap = new HashMap<>();
    this.imageSource = "";
  }

  protected abstract void fillImageSource();

  protected abstract void fillLabelMap();

  public void generateAbstractExercise() throws IOException {
    this.fillTitle();
    this.fillText();
    this.fillImageSource();
    this.fillLabelMap();
  }

  public Map<String, Integer[]> getLabelMap() {
    return labelMap;
  }

  public String getImageSource() {
    return imageSource;
  }

  public Set<String> getLabels() {
    return labelMap.keySet();
  }

  public List<String> getRandomizedLabels() {
    List<String> ret = new ArrayList<>(labelMap.keySet());
    Collections.shuffle(ret, task.getRandomInstance());
    return ret;
  }
}
