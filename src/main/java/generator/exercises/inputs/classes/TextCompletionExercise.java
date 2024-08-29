package generator.exercises.inputs.classes;

import generator.caching.FdlNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class TextCompletionExercise extends FillOutExercise {

  public final String SOLUTION_FORMAT = "STRING";
  // splice := word or any coherent non-word, e.g. ["I"," ", "am", " 42 ", "years", " ", "old", "."]
  protected final List<String> textSplices = new ArrayList<>();
  protected final List<Integer> wordSplices = new ArrayList<>();
  protected final List<Integer> blankSplices = new ArrayList<>();
  protected String originalText = "";

  public TextCompletionExercise(boolean kennen, boolean koennen, boolean verstehen,
      FdlNode symbol, FdlTypes requiredType, GenerationTask task) {
    super(kennen, koennen, verstehen, symbol, requiredType, task);
  }

  protected abstract void fillOriginalText();

  protected abstract void fillBlankIndexes();

  public void calculateSplices() {
    int lowerStart = 'a';
    int lowerEnd = 'z';
    int upperStart = 'A';
    int upperEnd = 'Z';
    int[] umlaute = {'ü', 'ä', 'ö', 'Ü', 'Ä', 'Ö', 'ß'};

    StringBuilder currentSplice = new StringBuilder(originalText.length());
    boolean spliceIsWord = true;
    for (int i = 0; i < originalText.length(); i++) {
      char currChar = originalText.charAt(i);

      if (((int) currChar >= lowerStart && (int) currChar <= lowerEnd) || (
          (int) currChar >= upperStart && (int) currChar <= upperEnd) ||
          (Arrays.stream(umlaute).anyMatch(j -> j == (int) currChar))) {
        if (!spliceIsWord) {
          textSplices.add(currentSplice.toString());
          currentSplice.delete(0, currentSplice.length());
          spliceIsWord = true;
        }
      } else {
        if (spliceIsWord) {
          textSplices.add(currentSplice.toString());
          wordSplices.add(textSplices.size() - 1);
          currentSplice.delete(0, currentSplice.length());
          spliceIsWord = false;
        }
      }
      currentSplice.append(currChar);
    }
    if (!currentSplice.isEmpty()) {
      textSplices.add(currentSplice.toString());
      if (spliceIsWord) {
        wordSplices.add(textSplices.size() - 1);
      }
    }
  }

  @Override
  public void generateAbstractExercise() {
    fillTitle();
    fillText();
    fillOriginalText();
    calculateSplices();
    fillBlankIndexes();
  }

  public String getOriginalText() {
    return originalText;
  }

  public List<String> getTextSplices() {
    return textSplices;
  }

  public List<Integer> getBlankSplices() {
    return blankSplices;
  }

  @Override
  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    for (int i = 0; i < blankSplices.size(); i++) {
      solutionMap.put(HTML_ID_NAME_PREFIX + i, textSplices.get(blankSplices.get(i)));
    }
  }
}
