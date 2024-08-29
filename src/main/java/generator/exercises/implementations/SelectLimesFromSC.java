package generator.exercises.implementations;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTAssignmentExpression;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.mathexpressions._ast.ASTSumExpression;
import fdl.types.containers.Reference;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.CalculationHelper;
import generator.util.EquationSymbolsHelper.IdentifiersInFunctionsCollector;
import generator.util.EquationSymbolsHelper.IndexIdentifierCollector;
import generator.util.EquationSymbolsHelper.MathJaxStringReplacer;
import generator.util.EquationSymbolsHelper.ValueFiller;
import generator.util.SymjaHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class SelectLimesFromSC extends TextSingleChoiceExercise {

  private static final boolean KENNEN = false;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = true;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;
  private final List<String> distractors = new ArrayList<>();
  private List<String> variableOrder;
  private Map<String, String> presetIdentifiers;
  private Map<String, String> varToSym;
  private Map<String, String> symToVar;
  private double answerValue;
  private String answerText;
  private EquationSymbol sym;
  private String limitVar;
  private String limit;
  private String limitSign;
  private String leftSign;

  public SelectLimesFromSC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicSingleChoice";
    int random = task.getRandomInstance().nextInt(100);

    if (random <= 50) {
      limit = "Infinity";
      limitSign = "\\infty";
    } else {
      limit = "0";
      limitSign = "0^{+}";
    }
  }

  public SelectLimesFromSC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof EquationSymbol) && !(((EquationSymbol) sym).getSpecificationMap()
        .isEmpty());
  }

  @Override
  public void generateAbstractExercise() throws UnfulfillableException {
    setupExercise();
    fillTitle();
    fillText();
    fillCorrectOption();
    fillWrongOptions();
    calcOptions();
    fillAdditionalElements();
  }

  @Override
  public void fillTitle() {
    title = "Limes von " + fdlNode.getTopicNode().getNodeName();
  }

  @Override
  public void fillText() {
    MathJaxStringReplacer replacer = new MathJaxStringReplacer(UnitHelper.baseUnitMap);
    sym.getRootExpression().accept(replacer);
    String formula = "\\(" + replacer.getExpressionString() + "\\)";
    texts.add(
        "Betrachten Sie folgende Formel" + (sym.getAliasMap().containsKey(leftSign) ? " für "
            + sym.getAliasMap().get(leftSign)
            : "") + ": " + formula + ". Wie verhält sich '" + limitVar + "', wenn \\( "
            + presetIdentifiers.get(
            variableOrder.get(variableOrder.size() - 1))
            + "\\) gegen \\(" + limitSign + "\\) läuft; also \\(\\lim_{" + presetIdentifiers.get(
            variableOrder.get(
                variableOrder.size() - 1)) + " \\to " + limitSign + "}\\)");
  }


  @Override
  public void fillCorrectOption() {
    if (answerValue == Double.POSITIVE_INFINITY) {
      correctOption = limitVar + " geht gegen \\(\\infty\\)";
    } else if (answerValue == Double.NEGATIVE_INFINITY) {
      correctOption = limitVar + " geht gegen -\\(\\infty\\)";
    } else if (answerValue == 0 && answerText == null) {
      correctOption = limitVar + " geht gegen 0";
    } else if (answerValue != 0 && answerText == null) {
      correctOption = limitVar + " konvergiert gegen einen konstanten Wert \\(\\neq\\) 0";
    } else if (!answerText.equals("")) {
      correctOption = answerText;
    } else {
      correctOption = limitVar + " geht gegen " + answerValue;
    }
  }

  @Override
  public void fillWrongOptions() {
    int difficulty = getDifficultyOrDefault();
    int numDistractors = Math.max((int) (difficulty / 2.5), 2);

    distractors.add(limitVar + " geht gegen \\(\\infty\\)");
    distractors.add(limitVar + " geht gegen -\\(\\infty\\)");
    distractors.add(limitVar + " geht gegen 0");
    distractors.add(limitVar + " konvergiert gegen einen konstanten Wert \\(\\neq\\) 0");

    if (answerValue != Double.POSITIVE_INFINITY && answerValue != Double.NEGATIVE_INFINITY
        && answerValue != 0) {
      Reference ref = sym.getReferenceMap()
          .get(variableOrder.get(variableOrder.size() - 1));
      double distractLimes =
          ref.getMin() + task.getRandomInstance().nextDouble() * (ref.getMax() - ref.getMin());
      if (answerValue % 1 != 0) {
        distractLimes = CalculationHelper.roundToNDecimals(distractLimes, 2);
      } else {
        distractLimes = CalculationHelper.roundToNDecimals(distractLimes, 0);
      }
      distractors.add(limitVar + " geht gegen " + distractLimes);
    }
    distractors.remove(correctOption);
    if (distractors.size() >= numDistractors) {
      Collections.shuffle(distractors, task.getRandomInstance());
    } else {
      distractors.add("Keine Aussage möglich");
      Collections.shuffle(distractors, task.getRandomInstance());
    }
    Collections.shuffle(distractors, task.getRandomInstance());
    for (int i = 0; i < Math.min(numDistractors, distractors.size()); i++) {
      wrongOptions.add(distractors.get(i));
    }
  }

  private void setupExercise() throws UnfulfillableException {

    sym = (EquationSymbol) fdlNode.getSymbol();

    ASTExpression expression = sym.getRootExpression();
    ASTExpression leftExpression = ((ASTAssignmentExpression) expression).getLeft();

    leftSign = ASTHelper.expressionToString(leftExpression);

    presetIdentifiers = new HashMap<>(ASTHelper.collectExtendedNamesMap(expression));

    IndexIdentifierCollector sumCol = new IndexIdentifierCollector();
    sym.getRootExpression().accept(sumCol);
    Map<String, String> indexedIds = sumCol.getIdentifiersWithIndex();

    IdentifiersInFunctionsCollector funCol = new IdentifiersInFunctionsCollector();
    sym.getRootExpression().accept(funCol);
    List<String> functionIds = funCol.getIteratorIdentifiers().stream().toList();

    variableOrder = new ArrayList<>(ASTHelper.collectExtendedNames(expression));
    variableOrder.removeAll(sym.getListIdentifiers());
    Map<String, String> varToSymList = new HashMap<>();

    int counter = 0;
    for (String str : sym.getListIdentifiers()) {
      varToSymList.put(str, "x" + counter);
      counter++;
    }

    Collections.shuffle(variableOrder, task.getRandomInstance());

    while (indexedIds.containsValue(variableOrder.get(0)) || functionIds.contains(
        variableOrder.get(0))) {
      String temp = variableOrder.get(0);
      variableOrder.remove(0);
      variableOrder.add(
          variableOrder.size() - (variableOrder.size() - indexedIds.size() - functionIds.size()),
          temp);
    }

    varToSym = new HashMap<>();
    symToVar = new HashMap<>();
    for (String str : variableOrder) {
      varToSym.put(str, "x" + counter);
      symToVar.put("x" + counter, str);
      counter++;
    }
    limitVar = "\\(" + presetIdentifiers.get(variableOrder.get(0)) + "\\)";

    SumExpr vis =
        sym.getListIdentifiers().size() > 0 ? new SumExpr(varToSym, null, sym,
            varToSymList)
            : new SumExpr(varToSym, sym);
    expression.accept(vis);

    String query;

    String equation =
        "Solve(" + vis.getExpressionString() + "," + varToSym.get(variableOrder.get(0)) + ")";

    ExprEvaluator utilE = new ExprEvaluator();
    IExpr resultE = utilE.eval(equation);
    String solution;
    resultE.isMatrix();
    if (resultE.isMatrix()[0] > 1) {
      String result = resultE.getAt(2).toString();
      String cut = result.substring(1, result.length() - 1);
      // split at ->
      String[] split = cut.split("->");
      solution = split[1];
    } else {
      solution = SymjaHelper.getResult(resultE);
    }

    if (limit.equals("Infinity")) {
      query = "Limit(" + solution + "," + varToSym.get(variableOrder.get(variableOrder.size() - 1))
          + " -> " + limit + ")";
    } else {
      query = "Limit(" + solution + ", " + varToSym.get(variableOrder.get(variableOrder.size() - 1))
          + "-> " + limit + ", Direction -> -1)";
    }

    // hand query to Symja and extract solution

    ExprEvaluator util = new ExprEvaluator();
    IExpr result = util.eval(query);
    String sol = result.toString();

    if (sol == null) {
      answerValue = 1234567890.1234567890f;
    } else if (sol.contains("-Infinity") && !sol.contains("sqrt(-Infinity")) {
      answerValue = Double.NEGATIVE_INFINITY;
    } else if (sol.contains("Infinity")) {
      answerValue = Double.POSITIVE_INFINITY;
    } else if (sol.equals("Indeterminate") || sol.contains("sqrt(-Infinity")) {
      answerText = "Keine Aussage möglich";
    } else if (result.isInteger()) {
      Reference toSolveRef = sym.getReferenceMap().get(variableOrder.get(variableOrder.size() - 1));
      if (toSolveRef != null && toSolveRef.hasRoundingMode()) {
        switch (toSolveRef.getRoundingMode()) {
          case ROUND_UP -> answerValue = (new BigDecimal(sol).setScale(0,
              RoundingMode.UP)).doubleValue();
          case ROUND_DOWN -> answerValue = (new BigDecimal(sol).setScale(0,
              RoundingMode.DOWN)).doubleValue();
          case ROUND_NEAREST -> answerValue = (new BigDecimal(sol).setScale(0,
              RoundingMode.HALF_UP)).doubleValue();
        }
        answerText = limitVar + " konvergiert gegen einen konstanten Wert \\(\\neq\\) 0";
      } else {
        // I hate this solution to convert radians solutions to degrees
        // conversion needs to be done before 2nd decimal point rounding
        ASTExpression blankSpec = sym.getSpecificationMap()
            .get(variableOrder.get(variableOrder.size() - 1));
        if (blankSpec != null) {
          String unit = ASTHelper.expressionToString(blankSpec);
          if (unit.equals("°") || unit.equals("degrees")) {
            sol = String.valueOf(Double.parseDouble(sol) * (180 / Math.PI));
          }
        }
        answerValue = (new BigDecimal(sol).setScale(2, RoundingMode.HALF_UP)).doubleValue();
      }
    } else {
      answerText = limitVar + " konvergiert gegen einen konstanten Wert \\(\\neq\\) 0";
    }
  }

  protected void fillAdditionalElements() {
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }

  public class SumExpr extends ValueFiller {

    Map<String, String> valueMap;
    EquationSymbol sym;
    Map<String, String> indexMap;

    public SumExpr(Map<String, String> valueMap, EquationSymbol sym) {
      super(valueMap, sym);
      this.valueMap = valueMap;
    }

    public SumExpr(Map<String, String> valueMap, Map<String, List<String>> listMap,
        EquationSymbol sym, Map<String, String> indexMap) {

      super(valueMap, listMap, sym);
      this.indexMap = indexMap;
      this.valueMap = valueMap;
      this.sym = sym;

    }

    @Override
    public void traverse(ASTSumExpression node) {
      String var = varToSym.get(ASTHelper.expressionToString(node.getVar()));
      String max;
      getSb().append("Sum(");
      getSb().append(var);
      if (var.equals(varToSym.get(variableOrder.get(variableOrder.size() - 1)))) {
        max = var;
      } else {
        max = "n";
      }
      getSb().append(", {").append(var).append(", ")
          .append(sym.getReferenceMap().get(symToVar.get(var)).getMin())
          .append(", ").append(max).append("})");
    }
  }
}
