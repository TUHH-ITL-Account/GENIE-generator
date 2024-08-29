package generator.exercises.implementations;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import fdl.fdl._ast.ASTSpaces;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.types.containers.Reference;
import fdl.types.units.PhysicalQuantity;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.CalculationExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.CalculationHelper;
import generator.util.EquationSymbolsHelper;
import generator.util.EquationSymbolsHelper.IdentifiersInFunctionsCollector;
import generator.util.EquationSymbolsHelper.IndexIdentifierCollector;
import generator.util.EquationSymbolsHelper.MathJaxStringReplacer;
import generator.util.EquationSymbolsHelper.SumIteratorIdentifierCollector;
import generator.util.EquationSymbolsHelper.ValueFiller;
import generator.util.SymjaHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class InputMissingEquationValue extends CalculationExercise {

  protected static final boolean KENNEN = false;
  protected static final boolean KOENNEN = true;
  protected static final boolean VERSTEHEN = false;
  protected static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;

  protected Map<String, String> constantMap;
  protected List<String> variableOrder;
  protected Map<String, String> variableValues;
  protected Map<String, List<String>> listValues;
  protected Map<String, String> presentIdentifiers;
  protected Map<String, String> usedUnits;
  protected double answerValue;
  protected EquationSymbol sym;

  public InputMissingEquationValue(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "BasicTextAnswer";
    this.variableValues = new HashMap<>();
  }

  public InputMissingEquationValue() {
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
  public void generateAbstractExercise() throws UnfulfillableException {
    setupExercise();
    fillTitle();
    fillText();
    fillAnswer();
    fillAdditionalElements();
    setUsesResources();
  }

  private void setupExercise() throws UnfulfillableException {
    sym = (EquationSymbol) fdlNode.getSymbol();
    int difficulty = getDifficultyOrDefault();
    presentIdentifiers = ASTHelper.collectExtendedNamesMap(
        ((EquationSymbol) fdlNode.getSymbol()).getRootExpression()); //Liste wird mit Root Expressions gefüllt todo: Variablen von Parametern und Konstanten trennen
    List<String> openVariablesAndParameters = new ArrayList<>();
    constantMap = new HashMap<>();
//todo: unify traversers via visitor registering
    SumIteratorIdentifierCollector sumColl = new SumIteratorIdentifierCollector();
    sym.getRootExpression().accept(sumColl);
    Set<String> iteratorIdentifiers = sumColl.getIteratorIdentifiers();

    IdentifiersInFunctionsCollector funcIdColl = new IdentifiersInFunctionsCollector();
    sym.getRootExpression().accept(funcIdColl);
    Set<String> identifiersInEvilFunctions = funcIdColl.getIteratorIdentifiers();

    for (String id : presentIdentifiers.keySet()) {
      //Wenn Konstante, dann wird sie zur Constant Map hinzugefügt
      Reference ref = sym.getReferenceMap().get(id);
      if (ref != null && ref.isConstant()) {
        constantMap.put(id, String.valueOf(sym.getReferenceMap().get(id).getConstant().getValue()));
      } else if (sym.getListIdentifiers().contains(id)) {
      } else if (iteratorIdentifiers.contains(id)) {
      } else if (identifiersInEvilFunctions.contains(id)) {
      }
      //Wenn Variable nicht konstant, dann wird sie zu open Variables hinzugefügt
      else {
        openVariablesAndParameters.add(id);
      }
    }

    // give the set of variables a random order; if difficulty greater 5 left side cannot be the unknown
    variableOrder = new ArrayList<>(
        openVariablesAndParameters); //Anhand der variablen Variablen wird die Liste variable orders erstellt
    if (difficulty > 5) {
      Collections.shuffle(variableOrder.subList(1, variableOrder.size()),
          task.getRandomInstance());
    } else {
      Collections.shuffle(variableOrder, task.getRandomInstance());
    }
    // add identifiers which require a value / are not allowed to be the unknown at the start
    for (String id : identifiersInEvilFunctions) {
      variableOrder.add(0, id);
    }
    // assign all except last variable in order a random value
    /*for (int i = 0; i < variableOrder.size() - 1; i++) {
      variableValues.put(variableOrder.get(i),
          generateValueToReference(sym.getReferenceMap().get(variableOrder.get(i))));
    }*/
    boolean foundGoodValues = false;
    int valueIteration = 0;
    while (!foundGoodValues && valueIteration < 50) {
      try {
        variableValues.putAll(EquationSymbolsHelper.generateVariablesWithRefs(sym,
            variableOrder.get(variableOrder.size() - 1), new HashMap<>(),
            task.getRandomInstance()));
        // assign lists variable values
        Map<String, String> indexedIds;
        if (sym.getListIdentifiers().size() > 0) {
          listValues = new HashMap<>();
          IndexIdentifierCollector col = new IndexIdentifierCollector();
          sym.getRootExpression().accept(col);
          indexedIds = col.getIdentifiersWithIndex();
          for (int i = 0; i < sym.getListIdentifiers().size(); i++) {
            listValues.put(sym.getListIdentifiers().get(i), new ArrayList<>());
            Reference iteratorRef = sym.getReferenceMap()
                .get(indexedIds.get(sym.getListIdentifiers().get(i)));
            int numElements = task.getRandomInstance()
                .nextInt((int) iteratorRef.getMax() - (int) iteratorRef.getMin() + 1) + 1;
            for (int j = 0; j < numElements; j++) {
              listValues.get(sym.getListIdentifiers().get(i)).add(
                  generateValueToReference(
                      sym.getReferenceMap().get(sym.getListIdentifiers().get(i))));
            }
          }
        }

        // fill in initial unit expressions or null
        Map<String, PhysicalQuantity> dims = UnitHelper.unitObjectsMap;
        Map<String, ASTExpression> originalUnits = new HashMap<>();
        for (String known : variableOrder) {
          if (sym.getReferenceMap().containsKey(known) && sym.getReferenceMap().get(known)
              .hasUnit()) {
            Reference ref = sym.getReferenceMap().get(known);
            if (ref.isParameter()) {
              continue;
            }
            originalUnits.put(known, ref.getUnitExpression());
          } else {
            originalUnits.put(known, sym.getSpecificationMap().getOrDefault(known, null));
          }
        }

        // convert
        usedUnits = new HashMap<>();
        Map<String, String> basedVars = new HashMap<>(); // name, value after conversion
        originalUnits.forEach((key, val) -> {
          if (val == null) {
            usedUnits.put(key, "");
          } else {
            if (val instanceof ASTStringLiteral) {
              usedUnits.put(key, UnitHelper.baseUnitMap.get(((ASTStringLiteral) val).getSource()));
            } else if (val instanceof ASTNameExpression) {
              String usedUnit = ((ASTNameExpression) val).getName();
              if (UnitHelper.baseUnitMap.containsValue(usedUnit)) {
                usedUnits.put(key, usedUnit);
              } else {
                if (this.sym.getSpecificationMap().containsKey(key)) {
                  basedVars.put(key,
                      dims.get(ASTHelper.expressionToString(sym.getSpecificationMap().get(key)))
                          .getBaseUnit());
                } else {
                  for (PhysicalQuantity pq : dims.values()) {
                    if (pq.containsUnit(usedUnit)) {
                      BigDecimal oldValue = new BigDecimal(variableValues.get(key));
                      basedVars.put(key, pq.toBaseUnit(oldValue, usedUnit).toString());
                      break;
                    }
                  }
                }
              }
            } else { // do not touch complicated expressions, e.g. length/time^2. Just fill in base units.
              MathJaxStringReplacer replacer = new MathJaxStringReplacer(UnitHelper.baseUnitMap);
              val.accept(replacer);
              usedUnits.put(key, "\\(" + replacer.getExpressionString() + "\\)");
            }
          }
        });

        // rename var-to-solve to x for Symja compatibility
        variableValues.put(variableOrder.get(variableOrder.size() - 1), "x");
        Map<String, String> queryMap =
            basedVars.isEmpty() ? variableValues : new HashMap<>(variableValues);
        queryMap.putAll(basedVars);
        queryMap.putAll(constantMap);

        // build query while replace variables with values
        ValueFiller vis =
            sym.getListIdentifiers().size() > 0 ? new ValueFiller(queryMap, listValues, sym)
                : new ValueFiller(queryMap, sym);
        ((EquationSymbol) fdlNode.getSymbol()).getRootExpression().accept(vis);
        String query = "Solve(" + vis.getExpressionString() + ",x)";

        // hand query to Symja and extract solution
        ExprEvaluator util = new ExprEvaluator();
        IExpr result = util.eval(query);

        String sol = SymjaHelper.getResult(result);

        Pattern pat = Pattern.compile("\\.[0-9]55");
        Matcher mat = pat.matcher(sol);
        if (mat.find()) {
          System.out.println("#");
        }

        if (sol == null) {
          answerValue = 1234567890.1234567890f;
        } else {
          Reference toSolveRef = sym.getReferenceMap()
              .get(variableOrder.get(variableOrder.size() - 1));
          if (toSolveRef != null && toSolveRef.hasRoundingMode()) {
            switch (toSolveRef.getRoundingMode()) {
              case ROUND_UP -> answerValue = (new BigDecimal(sol).setScale(0,
                  RoundingMode.UP)).doubleValue();
              case ROUND_DOWN -> answerValue = (new BigDecimal(sol).setScale(0,
                  RoundingMode.DOWN)).doubleValue();
              case ROUND_NEAREST -> answerValue = (new BigDecimal(sol).setScale(0,
                  RoundingMode.HALF_UP)).doubleValue();
            }
          } else {
            // If we have a (degree) angle calculated from non-angles, e.g. a = tan(len1/len2)
            ASTExpression blankSpec = sym.getSpecificationMap()
                .get(variableOrder.get(variableOrder.size() - 1));
            if (blankSpec != null) {
              String unit = ASTHelper.expressionToString(blankSpec);
              if (unit.equals("°") || unit.equals("degrees")) {
                sol = String.valueOf(Double.parseDouble(sol) * (180 / Math.PI));
              }
            }
            answerValue = CalculationHelper.roundToNDecimals(Double.parseDouble(sol), 2);
          }
        }
        foundGoodValues = true;
      } catch (UnfulfillableException ue) {
        valueIteration++;
      }
    }
  }

  @Override
  protected void fillTitle() {
    title = "Berechnungen in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    //Used Units: Einheiten
    //VariableOrder: Liste der Formelzeichen für die Variablen
    //VariableValues: Werte für die Variablen

    StringBuilder sbGiven = new StringBuilder();
    for (int i = 0; i < variableOrder.size() - 1; i++) {
      String var = variableOrder.get(i);
      if (sym.getAliasMap().containsKey(var)) {
        sbGiven.append("'").append(sym.getAliasMap().get(var))
            .append("'"); //Name zum Formelzeichen wird hinzugefügt
        sbGiven.append(" ");
      }
      sbGiven.append("\\(").append(presentIdentifiers.get(var)).append("\\)");
      sbGiven.append(": ");
      sbGiven.append(variableValues.get(var));
      if (usedUnits.containsKey(var)) {
        sbGiven.append(" ");
        sbGiven.append(usedUnits.get(var)); //Einheit wird hinzugefügt
      }
      sbGiven.append(i + 1 == variableOrder.size() - 2 ? " und "
          : i + 1 < variableOrder.size() - 1 ? ", " : "");
    }
    StringBuilder sbConst = new StringBuilder();
    if (!constantMap.isEmpty()) {
      sbConst.append(" Ferner seien vorhandene Konstanten: ");
      List<Entry<String, String>> entries = constantMap.entrySet().stream().toList();
      for (Entry<String, String> entry : entries) {
        String constant = entry.getKey();
        if (sym.getAliasMap().containsKey(constant)) {
          sbConst.append("'").append(sym.getAliasMap().get(constant)).append("'");
          sbConst.append(" ");
        }
        sbConst.append("\\(").append(presentIdentifiers.get(constant)).append("\\)");
        sbConst.append(": ");
        sbConst.append(constantMap.get(constant));
        if (sym.getReferenceMap().get(constant).getConstant().hasUnit()) {
          sbConst.append(" \\(").append(ASTHelper.expressionToEscapedMathJaxString(
              sym.getReferenceMap().get(constant).getConstant().getUnit())).append("\\)");
        }
      }
      sbConst.append(".");
    }
    StringBuilder sbLists = new StringBuilder();
    if (listValues != null && !listValues.isEmpty()) {
      sbLists.append(" Desweiteren sind folgende Listen gegeben: ");
      for (Entry<String, List<String>> entry : listValues.entrySet()) {
        if (sym.getAliasMap().containsKey(entry.getKey())) {
          sbLists.append("'").append(sym.getAliasMap().get(entry.getKey())).append("' ");
        }
        sbLists.append("\\(").append(presentIdentifiers.get(entry.getKey())).append("\\)")
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

    String unknownVar = variableOrder.get(variableOrder.size() - 1);
    String sbToCalc = sym.getAliasMap()
        .getOrDefault(variableOrder.get(variableOrder.size() - 1), "");
    String unknownUnit =
        usedUnits.getOrDefault(unknownVar, "");
    texts.add(String.format(
        "Gegeben " + (variableOrder.size() > 2 ? "seien" : "sei")
            + " %s.%s%s Berechnen Sie den Wert für '%s' \\(%s\\)" + (unknownUnit.equals("") ? ""
            : " in " + unknownUnit) + ".",
        sbGiven, sbConst, sbLists, sbToCalc, presentIdentifiers.getOrDefault(unknownVar, "")));
    if (!sym.getImageMap().isEmpty()) {
      texts.add("Folgende Abbildung(en) stehen Ihnen dabei als Referenz zur Verfügung:");
      int i = 0;
      for (Entry<String, String> image : sym.getImageMap().entrySet()) {
        texts.add(String.format(
            "<figure><img src='genie/%s' alt='Abbildung %d'><figcaption>%s</figcaption></figure>",
            image.getValue(), i, image.getKey()));
        i++;
      }
    }
    texts.add(
        "Sofern das Ergebnis es nicht anders erfordert, runden Sie dabei auf die zweite Nachkommastelle und benutzen Sie einen Punkt als Kommazeichen.");
  }

  @Override
  protected void fillAnswer() {
    answer = String.valueOf(answerValue);
  }

  protected void fillAdditionalElements() {
    // MathJax v2
    //additionalElements.add("<script async src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.9/latest.js?config=TeX-MML-AM_CHTML\"></script>");
    // MathJax 3.x
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }

  protected String generateValueToReference(Reference ref) {
    // todo: use int if not specified as floating point
    if (ref != null) {
      double lower, upper;
      // if no upper bound is specified we generate a number within +- 10, but dont go above -1 or below 1
      if (ref.getMin() == ref.getMax()) {
        if (ref.getMin() < 0) {
          upper = Math.max(-1, ref.getMin());
          lower = ref.getMin() - 10;
        } else if (ref.getMin() > 0) {
          lower = Math.min(ref.getMin(), 1);
          upper = ref.getMin() + 10;
        } else {
          lower = ref.getMin() - 10;
          upper = lower + 20;
        }
      } else {
        lower = ref.getMin();
        upper = ref.getMax();
      }
      if (ref.getSpace() != null) {
        // todo: improve 'dividability'
        if (ref.getSpace() == ASTSpaces.INT) {
          return String.valueOf(
              task.getRandomInstance().nextInt((int) (upper - lower)) + (int) lower);
        } else {
          double rand = task.getRandomInstance().nextDouble() * (upper - lower) + lower;
          return ref.getDecimalSpaces() != -1 ?
              String.valueOf(
                  BigDecimal.valueOf(rand).setScale(ref.getDecimalSpaces(), RoundingMode.HALF_UP)) :
              String.valueOf(BigDecimal.valueOf(rand).setScale(3, RoundingMode.HALF_UP));
        }
      } else if (ref.isSetSpace()) {
        List<Double> set = ref.getSetSpace();
        return String.valueOf(set.get(task.getRandomInstance().nextInt(set.size())));
      }
    }
    return String.valueOf(task.getRandomInstance().nextInt(18) + 2);
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
