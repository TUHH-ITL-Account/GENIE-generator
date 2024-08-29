package generator.exercises.implementations;

import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.automata.util.DFAutomaton;
import fdl.automata.util.DFAutomaton.Relevancy;
import fdl.automata.util.State;
import fdl.fdl._symboltable.PatternSymbol;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectPatternFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.PATTERN;

  private List<List<String>> validList;

  public SelectPatternFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicMultipleChoice";
  }

  public SelectPatternFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  public void generateAbstractExercise() throws UnfulfillableException {
    fillTitle();
    fillText();
    fillCorrectOptions();
    fillWrongOptions();
    calcOptions();
    fillAdditionalElements();
  }

  protected void fillAdditionalElements() {
    // MathJax 3.x
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }

  @Override
  protected void fillTitle() {
    title = "Muster in " + fdlNode.getTopicNode().getNodeName();
  }

  @Override
  protected void fillText() {
    texts.add(String.format(
        "Selektieren Sie alle Optionen bei denen es sich um eine valide Form von '%s' handelt.",
        ((PatternSymbol) fdlNode.getSymbol()).getObject()));
  }

  @Override
  protected void fillCorrectSet() throws UnfulfillableException {

  }

  @Override
  protected void fillWrongSet() throws UnfulfillableException {

  }

  @Override
  protected void fillCorrectOptions() throws UnfulfillableException {
    PatternSymbol sym = (PatternSymbol) fdlNode.getSymbol();
    int difficulty = getDifficultyOrDefault();

    Set<List<String>> validPatterns = sym.getAutomaton().getValidRunsUpTo(5, 0);
    if (validPatterns.size() < 2) {
      validPatterns = sym.getAutomaton().getValidRunsUpTo(8, 1);
    }
    if (validPatterns.isEmpty()) {
      throw new UnfulfillableException("Unable to generate any patterns of length 0-8");
    }
    int numCorrect = min(max(difficulty / 3, 1), validPatterns.size());
    validList = new ArrayList<>(validPatterns);
    Collections.shuffle(validList, task.getRandomInstance());
    for (int i = 0; i < numCorrect; i++) {
      if (sym.getAutomaton().getRelevancy() == Relevancy.ALL) {
        correctOptions.add(runListToAllString(validList.get(i)));
      } else {
        correctOptions.add(runListToInputsString(validList.get(i)));
      }
    }
  }

  private String runListToAllString(List<String> run) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\(");
    for (String s : run) {
      State isState = ((PatternSymbol) fdlNode.getSymbol()).getAutomaton().getStateByName(s);
      if (isState != null) {
        sb.append(isState.getLabel());
      } else {
        sb.append(String.format("\\xrightarrow{\\text{%s}}", s));
      }
    }
    sb.append("\\)");
    return sb.toString();
  }

  private String runListToInputsString(List<String> run) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < run.size(); i = i + 2) {
      sb.append(run.get(i));
    }
    return sb.toString();
  }

  // todo: cover case if distractor is actually correct
  @Override
  protected void fillWrongOptions() {
    PatternSymbol sym = (PatternSymbol) fdlNode.getSymbol();
    int difficulty = getDifficultyOrDefault();
    int numDistractors = max(difficulty / 2, 2);

    Collections.reverse(validList);
    for (int i = 0; wrongOptions.size() < numDistractors && i < validList.size(); i++) {
      List<String> target = validList.get(i);
      boolean wronged = false;
      for (int tries = 0; tries < 3 && !wronged; tries++) {
        if (target.size() > 3) {
          int rand = task.getRandomInstance().nextInt(3);
          if (rand == 0) {
            cut2Elements(target);
          } else if (rand == 1) {
            swapRandomElements(target);
          } else {
            int replaces = task.getRandomInstance().nextInt(2);
            for (int j = -1; j < replaces; j++) {
              replaceRandomElement(target, sym.getAutomaton());
            }
          }
        } else {
          replaceRandomElement(target, sym.getAutomaton());
        }
        if (!sym.getAutomaton().isValidRun(target)) {
          wronged = true;
          if (sym.getAutomaton().getRelevancy() == Relevancy.ALL) {
            wrongOptions.add(runListToAllString(target));
          } else {
            wrongOptions.add(runListToInputsString(target));
          }
        }
      }
    }
  }

  /**
   * Swaps two odd or two even element's indexes.
   *
   * @param list A list of Strings representing a run.
   */
  private void swapRandomElements(List<String> list) {
    int evenOrOdd = task.getRandomInstance().nextInt(2);
    int s1 = task.getRandomInstance().nextInt(list.size() / 2);
    int s2 = task.getRandomInstance().nextInt(list.size() / 2);
    if (s1 == s2) {
      s2 = s2 == 0 ? s2 + 1 : s2 - 1;
    }
    Collections.swap(list, evenOrOdd + 2 * s1, evenOrOdd + 2 * s2);
  }

  /**
   * Replaces a random state or transition with another.
   *
   * @param list      A list of Strings representing a run.
   * @param automaton A DFAutomaton to get replacing states/transitions from.
   */
  private void replaceRandomElement(List<String> list, DFAutomaton automaton) {
    int pick = task.getRandomInstance().nextInt(list.size());
    boolean isState = pick % 2 == 0;
    if (isState) {
      for (int tries = 0; tries < 10; tries++) {
        int ran = task.getRandomInstance().nextInt(automaton.getStates().size());
        String newName = automaton.getStates().get(ran).getName();
        if (!newName.equals(list.get(pick))) {
          list.set(pick, newName);
          break;
        }
      }
    } else {
      Set<String> inputs = new HashSet<>();
      for (Map<String, String> f : automaton.getTransitions().values()) {
        inputs.addAll(f.values());
      }
      List<String> inputList = new ArrayList<>(inputs);
      for (int tries = 0; tries < 10; tries++) {
        int ran = task.getRandomInstance().nextInt(inputList.size());
        String newLabel = inputList.get(ran);
        if (!newLabel.equals(list.get(pick))) {
          list.set(pick, newLabel);
          break;
        }
      }
    }
  }

  /**
   * Cuts two adjacent elements.
   *
   * @param list A list of Strings representing a run.
   */
  private void cut2Elements(List<String> list) {
    int cutAt = task.getRandomInstance().nextInt(list.size() - 1);
    list.remove(cutAt);
    list.remove(cutAt);
  }

}
