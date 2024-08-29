package generator.exercises.implementations;

import fdl.fdl._symboltable.DefinitionSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.TextCompletionExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class DefinitionTextCompletion extends TextCompletionExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.DEFINITION;

  private final List<String> wordPool = new ArrayList<>();

  public DefinitionTextCompletion(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "BasicTextCompletion";
  }

  public DefinitionTextCompletion() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public void generateAbstractExercise() {
    fillTitle();
    fillOriginalText();
    calculateSplices();
    fillBlankIndexes();
    fillWordPool();
    fillText();
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof DefinitionSymbol) && !(((DefinitionSymbol) sym).getDefinition()
        .isEmpty() || ((DefinitionSymbol) sym).getTerm().isEmpty());
  }

  @Override
  protected void fillTitle() {
    this.title = "Definitionen in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    if (getDifficultyOrDefault() > 7) {
      texts.add(String.format(
          "Ergänzen Sie die Lücken im folgenden Text für die in der Vorlesung gegebene Definition von %s.",
          fdlNode.getTopicNode().getNodeName()));
    } else {
      texts.add(String.format(
          "Ergänzen Sie die Lücken im folgenden Text für die in der Vorlesung gegebene Definition von %s. Nutzen Sie dabei die folgenden Wörter:",
          fdlNode.getTopicNode().getNodeName()));
      texts.add(String.join(", ", wordPool));
      //if(getDifficultyOrDefault() > 3) {
      //  texts.add("Beachten Sie, dass nicht alle Wörter benötigt werden / korrekt sind.");
      //}
    }
  }

  @Override
  public void fillOriginalText() {
    originalText = ((DefinitionSymbol) fdlNode.getSymbol()).getDefinition();
  }

  @Override
  public void fillBlankIndexes() {
    int difficulty = getDifficultyOrDefault();
    int inverse = Math.max(12 - difficulty, 6);
    for (Integer wordSplice : wordSplices) {
      if (textSplices.get(wordSplice).length() >= inverse) {
        blankSplices.add(wordSplice);
      }
    }
  }

  public void fillWordPool() {
    if (getDifficultyOrDefault() < 8) {
      for (Integer i : blankSplices) {
        wordPool.add(textSplices.get(i));
      }
      if (getDifficultyOrDefault() > 3) {
        //todo: add n distractors, based on diff. Implement after Hauke Def-template
      }
      Collections.shuffle(wordPool, task.getRandomInstance());
    }
  }

  public List<String> getWordPool() {
    return wordPool;
  }
}
