package generator.exercises.implementations;

import fdl.fdl._symboltable.FigureSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.LabelingExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class InputFigureLabels extends LabelingExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.FIGURE;

  private BufferedImage img;

  private LinkedHashMap<String, Integer[]> top;
  private LinkedHashMap<String, Integer[]> bottom;
  private LinkedHashMap<String, Integer[]> left;
  private LinkedHashMap<String, Integer[]> right;

  private int imgWidth;
  private int imgHeight;

  private int origImgWidth;
  private int origImgHeight;

  public InputFigureLabels(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "LabelFigures";
    top = new LinkedHashMap<>();
    bottom = new LinkedHashMap<>();
    left = new LinkedHashMap<>();
    right = new LinkedHashMap<>();
  }

  public InputFigureLabels() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof FigureSymbol) && !(((FigureSymbol) sym).getLabelMap().isEmpty());
  }

  @Override
  public void generateAbstractExercise() throws IOException {
    fillLabelMap();
    fillImageSource();
    setup();
    fillTitle();
    fillText();
    setUsesResources();
  }

  protected void setup() throws IOException {
    FigureSymbol sym = (FigureSymbol) fdlNode.getSymbol();
    int difficulty = getDifficultyOrDefault();
    img = ImageIO.read(new File(task.getModeldir() + "/img/" + sym.getFilename()));
    if (img == null) {
      throw new IOException(String.format(
          "No registered ImageReader claims to be able to read the resulting stream from image file '%s' defined in FDL '%s'.",
          sym.getFilename(), fdlNode.getFdlName()));
    }
    origImgWidth = img.getWidth();
    origImgHeight = img.getHeight();
    if (origImgWidth > 960) {
      imgWidth = 900;
      imgHeight = (int) (900d / origImgWidth * (double) origImgHeight);
    } else {
      imgWidth = origImgWidth;
      imgHeight = origImgHeight;
    }
    double fac = 900d / origImgWidth;
    Map<String, Integer> unsortedPreTop = new HashMap<>();
    Map<String, Integer> unsortedPreLeft = new HashMap<>();
    Map<String, Integer> unsortedPreRight = new HashMap<>();
    Map<String, Integer> unsortedPreBottom = new HashMap<>();
    sym.getLabelMap().forEach((k, v) -> {
      if (origImgWidth == imgWidth) {
        switch (getZone(v[0], v[1])) {
          case 0 -> unsortedPreTop.put(k, v[0]);
          case 1 -> unsortedPreRight.put(k, v[1]);
          case 2 -> unsortedPreBottom.put(k, v[0]);
          case 3 -> unsortedPreLeft.put(k, v[1]);
        }
      } else {
        switch (getZone((int) (fac * v[0]), (int) (fac * v[1]))) {
          case 0 -> unsortedPreTop.put(k, v[0]);
          case 1 -> unsortedPreRight.put(k, v[1]);
          case 2 -> unsortedPreBottom.put(k, v[0]);
          case 3 -> unsortedPreLeft.put(k, v[1]);
        }
      }
    });
    Map<String, Integer> sortedPreTop = unsortedPreTop.entrySet().stream()
        .sorted(Map.Entry.comparingByValue()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                LinkedHashMap::new));
    Map<String, Integer> sortedPreLeft = unsortedPreLeft.entrySet().stream()
        .sorted(Map.Entry.comparingByValue()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                LinkedHashMap::new));
    Map<String, Integer> sortedPreRight = unsortedPreRight.entrySet().stream()
        .sorted(Map.Entry.comparingByValue()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                LinkedHashMap::new));
    Map<String, Integer> sortedPreBottom = unsortedPreBottom.entrySet().stream()
        .sorted(Map.Entry.comparingByValue()).collect(
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                LinkedHashMap::new));

    int topSpacing = imgWidth / (sortedPreTop.size() + 1);
    int leftSpacing = imgHeight / (sortedPreLeft.size() + 1);
    int rightSpacing = imgHeight / (sortedPreRight.size() + 1);
    int bottomSpacing = imgWidth / (sortedPreBottom.size() + 1);

    int i;
    i = 50 + topSpacing;
    for (String key : sortedPreTop.keySet()) {
      top.put(key, new Integer[]{i, 40});
      i += topSpacing;
    }
    i = 50 + leftSpacing;
    for (String key : sortedPreLeft.keySet()) {
      left.put(key, new Integer[]{10, i});
      i += leftSpacing;
    }
    i = 50 + rightSpacing;
    for (String key : sortedPreRight.keySet()) {
      right.put(key, new Integer[]{50 + imgWidth + 10, i});
      i += rightSpacing;
    }
    i = 50 + bottomSpacing;
    for (String key : sortedPreBottom.keySet()) {
      bottom.put(key, new Integer[]{i, 50 + imgHeight + 40});
      i += bottomSpacing;
    }
  }

  @Override
  protected void fillTitle() {
    title = "Abbildungen in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    if ((int) task.getParameters().get("difficulty") >= 9) {
      texts.add(
          "Beschriften Sie die verwiesenen Stellen in der Abbildung mit den korrekten Begriffen.");
    } else {
      texts.add(
          "Beschriften Sie die verwiesenen Stellen in der Abbildung mit den nachfolgenden Begriffen:");
      texts.add(String.join(", ", getRandomizedLabels().stream().map(s -> "'" + s + "'").toList()));
    }
  }

  @Override
  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", "STRING");
    int i = 0;
    for (String s : top.keySet()) {
      solutionMap.put("ANSWER" + i, s);
      i++;
    }
    for (String s : left.keySet()) {
      solutionMap.put("ANSWER" + i, s);
      i++;
    }
    for (String s : right.keySet()) {
      solutionMap.put("ANSWER" + i, s);
      i++;
    }
    for (String s : bottom.keySet()) {
      solutionMap.put("ANSWER" + i, s);
      i++;
    }
  }

  @Override
  protected void fillImageSource() {
    FigureSymbol sym = (FigureSymbol) fdlNode.getSymbol();
    imageSource = "genie/" + sym.getFilename();
  }

  @Override
  protected void fillLabelMap() {
    labelMap = ((FigureSymbol) fdlNode.getSymbol()).getLabelMap();
  }

  @SuppressWarnings("unchecked")
  protected void setUsesResources() {
    task.getParameters().putIfAbsent("usesResources", new HashSet<String>());
    ((Set<String>) task.getParameters().get("usesResources")).add(
        ((FigureSymbol) fdlNode.getSymbol()).getFilename());
  }

  // 0 = top, 1 = right, 2 = bot, 3 = left
  private int getZone(int x, int y) {
    int w = imgWidth;
    int h = imgHeight;
    float m = (float) h / (float) w;
    if (-m * x + h < y) {
      if (m * x < y) {
        return 2;
      }
      return 1;
    }
    if (m * x < y) {
      return 3;
    }
    return 0;
  }

  public LinkedHashMap<String, Integer[]> getTop() {
    return top;
  }

  public LinkedHashMap<String, Integer[]> getBottom() {
    return bottom;
  }

  public LinkedHashMap<String, Integer[]> getLeft() {
    return left;
  }

  public LinkedHashMap<String, Integer[]> getRight() {
    return right;
  }

  public int getImgWidth() {
    return imgWidth;
  }

  public int getImgHeight() {
    return imgHeight;
  }

  public int getOrigImgWidth() {
    return origImgWidth;
  }

  public int getOrigImgHeight() {
    return origImgHeight;
  }
}
