package generator.exercises.implementations;

import static java.lang.Math.max;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTAssignmentExpression;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectUnitForVariableFromSC extends TextSingleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;

  private EquationSymbol sym;
  private String correctFormulaSign;

  public SelectUnitForVariableFromSC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicSingleChoice";
    sym = (EquationSymbol) fdlNode.getSymbol();

  }

  public SelectUnitForVariableFromSC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof EquationSymbol && ((EquationSymbol) sym).hasSingleLeftSide() && !(
        ((EquationSymbol) sym).getSpecificationMap().isEmpty()
            || ((EquationSymbol) sym).getAliasMap().isEmpty()));
  }

  @Override
  public void fillTitle() {
    title = "Formeln in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  public void fillText() {
    ASTExpression expression = ((EquationSymbol) fdlNode.getSymbol()).getRootExpression();
    expression = ((ASTAssignmentExpression) expression).getLeft();
    String formulaSign = ASTHelper.expressionToString(expression);
    texts.add(String.format("Selektieren Sie welche Einheit zu '%s' gehört.",
        sym.getAliasMap().get(formulaSign)));
  }

  @Override
  public void fillCorrectOption() {
    ASTExpression expression = sym.getRootExpression();
    expression = ((ASTAssignmentExpression) expression).getLeft();
    correctFormulaSign = ASTHelper.expressionToString(expression);
    ASTExpression unitExpr;
    if (sym.getReferenceMap().containsKey(correctFormulaSign)) {
      unitExpr = sym.getReferenceMap().get(correctFormulaSign).getUnitExpression();
      if (unitExpr != null) {
        correctOption = ASTHelper.expressionToString(unitExpr);
      } else {
        correctOption = "keine Einheit";
      }

    } else if (sym.getSpecificationMap().containsKey(correctFormulaSign)) {
      unitExpr = sym.getSpecificationMap().get(correctFormulaSign);
      if (UnitHelper.baseUnitMap.containsKey(ASTHelper.expressionToString(unitExpr))) {
        correctOption = UnitHelper.baseUnitMap.get(ASTHelper.expressionToString(unitExpr));
      } else {
        for (String unit : UnitHelper.baseUnitMap.keySet()) {
          if (ASTHelper.expressionToString(unitExpr).contains(unit)) {
            correctOption = ASTHelper.expressionToString(unitExpr)
                .replace(unit, UnitHelper.baseUnitMap.get(unit));
            break;
          }
        }
      }
    } else {
      correctOption = "keine Einheit";
    }
  }

  @Override
  protected void fillWrongOptions() {
    int difficulty = getDifficultyOrDefault();
    int numDistractors = max(difficulty / 2, 2);
    ASTExpression expression = ((EquationSymbol) fdlNode.getSymbol()).getRootExpression();
    ASTExpression rightExpression = ((ASTAssignmentExpression) expression).getRight();
    ASTExpression leftExpression = ((ASTAssignmentExpression) expression).getLeft();
    List<String> distractors = new ArrayList<>();
    Set<String> expressionSet = ASTHelper.collectExtendedNames(rightExpression);

    if (!correctOption.equals("keine Einheit")) {
      distractors.add("keine Einheit");
    }
    for (String formulaSign : expressionSet) {
      ASTExpression unitExpr;
      if (sym.getReferenceMap().containsKey(formulaSign)) {
        unitExpr = sym.getReferenceMap().get(formulaSign).getUnitExpression();
      } else {
        unitExpr = null;
      }
      if (unitExpr != null) {
        String unit = ASTHelper.expressionToString(unitExpr);
        if (!distractors.contains(unit) && !this.correctOption.equals(unit)) {
          distractors.add(unit);
        }
      }
    }
    String leftSpec = "";
    List<String> corUnits;
    List<String> distractUnits = new ArrayList<>();
    if (sym.getSpecificationMap().containsKey(correctFormulaSign)) {
      leftSpec = ASTHelper.expressionToString(sym.getSpecificationMap().get(correctFormulaSign));
    }
    if (sym.getReferenceMap().containsKey(correctFormulaSign)
        && sym.getReferenceMap().get(correctFormulaSign).getUnitExpression() != null) {
      corUnits = ASTHelper.collectExtendedNames(
          sym.getReferenceMap().get(correctFormulaSign).getUnitExpression()).stream().toList();
      distractUnits = new ArrayList<>(corUnits);
    }

    for (String key : UnitHelper.baseUnitMap.keySet()) {
      if (leftSpec != null && !leftSpec.contains(key)) {
        distractUnits.add(UnitHelper.baseUnitMap.get(key));
        if (!distractors.contains(UnitHelper.baseUnitMap.get(key))) {
          distractors.add(UnitHelper.baseUnitMap.get(key));
        }
      }
    }
    for (int i = 0; i < numDistractors - distractors.size(); i++) {
      Collections.shuffle(distractUnits, task.getRandomInstance());
      String operator;
      String complDistractor;
      int random = task.getRandomInstance().nextInt(2);
      if (random == 0) {
        operator = "*";
      } else {
        operator = "/";
      }
      random = task.getRandomInstance().nextInt(101);
      int exponent = (int) ((random * 3 + 200.0) / 100);
      if (random < 15) {
        complDistractor = distractUnits.get(0) + operator + distractUnits.get(1) + "^" + exponent;
      } else if (random > 15 && random <= 40) {
        complDistractor = distractUnits.get(0) + "^" + exponent + operator + distractUnits.get(1);
      } else {
        complDistractor = distractUnits.get(0) + operator + distractUnits.get(1);
      }
      if (!correctOption.equals(complDistractor) || !distractors.contains(complDistractor)) {
        distractors.add(complDistractor);
      } else {
        i--;
      }
    }
    Collections.shuffle(distractors, this.task.getRandomInstance());
    for (int i = 0; i < numDistractors; i++) {
      wrongOptions.add(distractors.get(i));
    }
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}