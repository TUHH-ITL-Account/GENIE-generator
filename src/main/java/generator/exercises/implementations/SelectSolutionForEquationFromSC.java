package generator.exercises.implementations;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl.util.ASTHelper;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.CalculationHelper;
import generator.util.CalculationHelper.CalculationContainer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectSolutionForEquationFromSC extends TextSingleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;

  private CalculationContainer container;

  public SelectSolutionForEquationFromSC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicSingleChoice";
  }

  public SelectSolutionForEquationFromSC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof EquationSymbol) && !(
        ((EquationSymbol) sym).getSpecificationMap().isEmpty()
            || ((EquationSymbol) sym).getAliasMap().isEmpty()
            || ((EquationSymbol) sym).getReferenceMap().isEmpty());
  }

  @Override
  public void generateAbstractExercise() throws Exception {
    container = CalculationHelper.setupExercise(fdlNode, task);
    super.generateAbstractExercise();
    fillAdditionalElements();
    setUsesResources();
  }

  @Override
  protected void fillTitle() {
    title = "Formeln in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    StringBuilder sbGiven = new StringBuilder();
    for (int i = 0; i < container.variableOrder.size() - 1; i++) {
      String var = container.variableOrder.get(i);
      if (container.sym.getAliasMap().containsKey(var)) {
        sbGiven.append("'").append(container.sym.getAliasMap().get(var))
            .append("'"); //Name zum Formelzeichen wird hinzugefügt
        sbGiven.append(" ");
      }
      sbGiven.append("\\(").append(container.presentIdentifiers.get(var)).append("\\)");
      sbGiven.append(": ");
      sbGiven.append(container.variableValues.get(var));
      if (container.usedUnits.containsKey(var)) {
        sbGiven.append(" ");
        sbGiven.append(container.usedUnits.get(var)); //Einheit wird hinzugefügt
      }
      sbGiven.append(i + 1 == container.variableOrder.size() - 2 ? " und "
          : i + 1 < container.variableOrder.size() - 1 ? ", " : "");
    }
    StringBuilder sbConst = new StringBuilder();
    if (!container.constantMap.isEmpty()) {
      sbConst.append(" Ferner seien vorhandene Konstanten: ");
      List<Entry<String, String>> entries = container.constantMap.entrySet().stream().toList();
      for (Entry<String, String> entry : entries) {
        String constant = entry.getKey();
        if (container.sym.getAliasMap().containsKey(constant)) {
          sbConst.append("'").append(container.sym.getAliasMap().get(constant)).append("'");
          sbConst.append(" ");
        }
        sbConst.append("\\(").append(container.presentIdentifiers.get(constant)).append("\\)");
        sbConst.append(": ");
        sbConst.append(container.constantMap.get(constant));
        if (container.sym.getReferenceMap().get(constant).getConstant().hasUnit()) {
          sbConst.append(" \\(").append(ASTHelper.expressionToEscapedMathJaxString(
              container.sym.getReferenceMap().get(constant).getConstant().getUnit())).append("\\)");
        }
      }
      sbConst.append(".");
    }
    StringBuilder sbLists = new StringBuilder();
    if (container.listValues != null && !container.listValues.isEmpty()) {
      sbLists.append(" Des weiteren sind folgende Listen gegeben: ");
      for (Entry<String, List<String>> entry : container.listValues.entrySet()) {
        if (container.sym.getAliasMap().containsKey(entry.getKey())) {
          sbLists.append("'").append(container.sym.getAliasMap().get(entry.getKey())).append("' ");
        }
        sbLists.append("\\(").append(container.presentIdentifiers.get(entry.getKey())).append("\\)")
            .append(": [");
        for (int i = 0; i < entry.getValue().size(); i++) {
          sbLists.append(entry.getValue().get(i));
          if (i + 1 < entry.getValue().size()) {
            sbLists.append(", ");
          }
        }
        sbLists.append("].");
      }
    }

    String unknownVar = container.variableOrder.get(container.variableOrder.size() - 1);
    String sbToCalc = container.sym.getAliasMap()
        .getOrDefault(container.variableOrder.get(container.variableOrder.size() - 1), "");
    String unknownUnit =
        container.usedUnits.getOrDefault(unknownVar, "");
    texts.add(String.format(
        "Gegeben " + (container.variableOrder.size() > 2 ? "seien" : "sei")
            + " %s.%s%s Berechnen und selektieren Sie den Wert für '%s' \\(%s\\)" + (
            unknownUnit.equals("") ? ""
                : " in " + unknownUnit) + ".",
        sbGiven, sbConst, sbLists, sbToCalc,
        container.presentIdentifiers.getOrDefault(unknownVar, "")));
    if (!container.sym.getImageMap().isEmpty()) {
      texts.add("Folgende Abbildung(en) stehen Ihnen dabei als Referenz zur Verfügung:");
      int i = 0;
      for (Entry<String, String> image : container.sym.getImageMap().entrySet()) {
        texts.add(String.format(
            "<figure><img src='genie/%s' alt='Abbildung %d'><figcaption>%s</figcaption></figure>",
            image.getValue(), i, image.getKey()));
        i++;
      }
    }
    texts.add(
        "Sofern das Ergebnis es nicht anders erfordert, runden Sie dabei auf die zweite Nachkommastelle.");
  }

  @Override
  protected void fillCorrectOption() {
    correctOption = String.valueOf(container.answerValue);
  }

  //todo: closeness check with other options
  //todo negative correct
  @Override
  protected void fillWrongOptions() {
    int difficulty = (int) task.getParameters().getOrDefault("difficulty", 6);
    int numDistractors = max((int) (difficulty / 2.5), 2);

    double correct = Double.parseDouble(correctOption);
    double half = correct / 2;

    if (correct == 0d) {
      for (int i = 0; i < numDistractors; i++) {
        double dis = CalculationHelper.roundToNDecimals(task.getRandomInstance()
            .nextInt(20) - 10, 2);
        String sol = String.valueOf(dis);
        if (sol.equals(correctOption)) {
          i--;
          continue;
        }
        wrongOptions.add(sol);
      }
    } else if (abs(correct) < 1) {
      int attempts = 0;
      for (int i = 0; i < numDistractors; i++) {
        double dis = (BigDecimal.valueOf(task.getRandomInstance().nextDouble())
            .setScale(2, RoundingMode.HALF_UP)).doubleValue();
        if (correct < 0) {
          dis *= -1;
        }
        String sol = String.valueOf(dis);
        if (sol.equals(correctOption)) {
          if (attempts > 100) {
            attempts = 0;
            wrongOptions.add(String.valueOf(dis + i));
            continue;
          }
          attempts++;
          i--;
          continue;
        }
        wrongOptions.add(sol);
      }
    } else {
      for (int i = 0; i < numDistractors; i++) {
        double pre = task.getRandomInstance().nextInt(abs((int) correct)) + abs(half);
        if (correct < 0) {
          pre *= -1;
        }
        double post = task.getRandomInstance().nextDouble() + 0.001;
        String[] correctSplit = correctOption.split("\\.");
        String sol;
        if (correctSplit.length > 1) {
          String[] postSplit = String.valueOf(post).split("\\.");
          sol = (int) pre + "." + postSplit[1].substring(0,
              Math.min(correctSplit[1].length(), postSplit[1].length()));
        } else {
          sol = String.valueOf(pre);
        }
        if (sol.equals(correctOption)) {
          i--;
          continue;
        }
        wrongOptions.add(sol);
      }
    }
  }

  protected void fillAdditionalElements() {
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }

  @SuppressWarnings("unchecked")
  protected void setUsesResources() {
    Collection<String> images = ((EquationSymbol) fdlNode.getSymbol()).getImageMap().values();
    if (!images.isEmpty()) {
      task.getParameters().putIfAbsent("usesResources", new HashSet<String>());
      ((Set<String>) task.getParameters().get("usesResources")).addAll(images);
    }
  }

}
