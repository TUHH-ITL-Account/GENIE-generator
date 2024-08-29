package generator.exercises.supplements;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.types.containers.Reference;
import fdl.types.units.PhysicalQuantity;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.implementations.InputMissingEquationValue;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class SpecifiedVariableInputCalculationExercise extends InputMissingEquationValue {

  private final Map<String, Double> givenIdentifierValues;
  private String blankIdentifier = "";

  /**
   * Use this constructor if you have given variable values and wish for a specific, blanked
   * variable
   *
   * @param node                  FdlNode for the equation
   * @param task                  GenerationTask for the current exercise generation
   * @param givenIdentifierValues Map of identifiers onto values which are to be already given
   * @param blankIdentifier       The identifier to be left blank / needing to be calculated by
   *                              learner
   */
  public SpecifiedVariableInputCalculationExercise(FdlNode node, GenerationTask task,
      Map<String, Double> givenIdentifierValues, String blankIdentifier) {
    super(node, task);
    this.givenIdentifierValues =
        givenIdentifierValues == null ? new HashMap<>() : givenIdentifierValues;
    this.blankIdentifier = blankIdentifier;
  }

  /**
   * Use this constructor if you have given variable values but the to-be-calculated variable is
   * irrelevant
   *
   * @param node                  FdlNode for the equation
   * @param task                  GenerationTask for the current exercise generation
   * @param givenIdentifierValues Map of identifiers onto values which are to be already given
   */
  public SpecifiedVariableInputCalculationExercise(FdlNode node, GenerationTask task,
      Map<String, Double> givenIdentifierValues) {
    super(node, task);
    this.givenIdentifierValues =
        givenIdentifierValues == null ? new HashMap<>() : givenIdentifierValues;
  }

  @Override
  public void generateAbstractExercise() throws UnfulfillableException {
    setupExercise();
    fillTitle();
    fillText();
    fillAnswer();
    fillAdditionalElements();
  }

  @Override
  protected void fillText() {
    //Used Units: Einheiten
    //VariableOrder: Liste der Formelzeichen für die Variablen
    //VariableValues: Werte für die Variablen

    StringBuilder sbPrevious = new StringBuilder();
    if (!givenIdentifierValues.isEmpty()) {
      sbPrevious.append(
          "Benutzen Sie für die folgende Rechnung bereits bekannte/errechnete Werte für ");
      int i = 0;
      for (var entry : givenIdentifierValues.entrySet()) {
        String var = entry.getKey();
        if (sym.getAliasMap().containsKey(var)) {
          sbPrevious.append("'").append(sym.getAliasMap().get(var))
              .append("'"); //Name zum Formelzeichen wird hinzugefügt
          sbPrevious.append(" ");
        }
        sbPrevious.append("\\(").append(presentIdentifiers.get(var)).append("\\)");
        sbPrevious.append(i + 1 == givenIdentifierValues.size() - 2 ? " und "
            : i + 1 < givenIdentifierValues.size() - 1 ? ", " : "");
        i++;
      }
      sbPrevious.append(". ");
    }
    StringBuilder sbGiven = new StringBuilder();
    if (variableOrder.size() - givenIdentifierValues.size() > 1) {
      //sbGiven.append("Gegeben seien nun: ");
      for (int i = 0; i < variableOrder.size() - 1; i++) {
        String var = variableOrder.get(i);
        if (givenIdentifierValues.containsKey(var)) {
          continue;
        }
        if (sym.getAliasMap().containsKey(var)) {
          // alias
          sbGiven.append("'").append(sym.getAliasMap().get(var)).append("'");
          sbGiven.append(" ");
        }
        // variable
        sbGiven.append("\\(").append(presentIdentifiers.get(var)).append("\\)");
        sbGiven.append(": ");
        // value
        sbGiven.append(variableValues.get(var));
        // unit
        if (usedUnits.containsKey(var)) {
          sbGiven.append(" ");
          sbGiven.append(usedUnits.get(var));
        }
        // spliterator
        sbGiven.append(i + 1 == variableOrder.size() - 1 ? ""
            : i + 2 == variableOrder.size() - 1 ? " und " : ", ");
      }
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
    if(unknownUnit.equals("%")) {
      unknownUnit += "%";
    }
    texts.add(String.format(
        "%s" + (variableOrder.size() - givenIdentifierValues.size() > 1 ? "Gegeben " + (
            variableOrder.size() - givenIdentifierValues.size() > 2 ? "seien" : "sei")
            + (sbPrevious.isEmpty() ? " " : " nun ")
            + sbGiven + "." : "") + "%s%s Berechnen Sie den Wert für '%s' \\(%s\\)" + (
            unknownUnit.equals("") ? ""
                : " in \\(" + unknownUnit) + "\\).",
        sbPrevious, sbConst, sbLists, sbToCalc,
        presentIdentifiers.getOrDefault(unknownVar, "")));
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
        openVariablesAndParameters.add(0,id);
      }
      //Wenn Variable nicht konstant, dann wird sie zu open Variables hinzugefügt
      else {
        openVariablesAndParameters.add(id);
      }
    }
    variableOrder = new ArrayList<>(
        openVariablesAndParameters); //Anhand der variablen Variablen wird die Liste variable orders erstellt
    if (blankIdentifier != null && !blankIdentifier.isEmpty()) {
      if (!variableOrder.remove(blankIdentifier)) {
        throw new UnfulfillableException(
            String.format("Template: Unable to find '%s' (to blank)", blankIdentifier));
      }
      variableOrder.add(blankIdentifier);
    } else { // place already calculated variables at the start, shuffle the rest of the order
      for (String id : givenIdentifierValues.keySet()) {
        variableOrder.remove(id);
        variableOrder.add(0, id);
      }
      Collections.shuffle(variableOrder.subList(givenIdentifierValues.size(), variableOrder.size()),
          task.getRandomInstance());
      // add identifiers which require a value / are not allowed to be the unknown at the start
      for (String id : identifiersInEvilFunctions) {
        variableOrder.add(0, id);
      }
    }
    if (blankIdentifier.equals("")) {
      blankIdentifier = variableOrder.get(variableOrder.size() - 1);
    }
    // assign all except last variable in order a passed or random value
    /*for (int i = 0; i < variableOrder.size() - 1; i++) {
      Double presetValue = givenIdentifierValues.get(variableOrder.get(i));
      variableValues.put(variableOrder.get(i), presetValue != null ? String.valueOf(presetValue) :
          generateValueToReference(sym.getReferenceMap().get(variableOrder.get(i))));
    }*/
    Map<String, String> given = new HashMap<>();
    for (Entry<String, Double> entry : givenIdentifierValues.entrySet()) {
      given.put(entry.getKey(), String.valueOf(entry.getValue()));
    }
    variableValues.putAll(EquationSymbolsHelper.generateVariablesWithRefs(sym, variableOrder.get(
        variableOrder.size() - 1), given, task.getRandomInstance()));

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
              generateValueToReference(sym.getReferenceMap().get(sym.getListIdentifiers().get(i))));
        }
      }
    }

    // fill in initial unit expressions or null
    Map<String, PhysicalQuantity> dims = UnitHelper.unitObjectsMap;
    Map<String, ASTExpression> originalUnits = new HashMap<>();
    for (String known : variableOrder) {
      if (sym.getReferenceMap().containsKey(known) && sym.getReferenceMap().get(known).hasUnit()) {
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
          if (((ASTStringLiteral) val).getSource().equals("°")) {
            usedUnits.put(key, "°");
          } else {
            usedUnits.put(key, UnitHelper.baseUnitMap.get(((ASTStringLiteral) val).getSource()));
          }
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
          usedUnits.put(key, replacer.getExpressionString());
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
        sym.getListIdentifiers().size() > 0 ? new ValueFiller(queryMap, listValues, sym, true)
            : new ValueFiller(queryMap, new HashMap<>(), sym, true);
    ((EquationSymbol) fdlNode.getSymbol()).getRootExpression().accept(vis);
    String query = "Solve(" + vis.getExpressionString() + ",x)";

    // hand query to Symja and extract solution
    ExprEvaluator util = new ExprEvaluator();
    IExpr result = util.eval(query);

    String sol = SymjaHelper.getResult(result);

    if (sol == null) {
      throw new UnfulfillableException("CAS returned unknown answer?: " + result);
    }

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
    } else {
      // If we have a (degree) angle calculated from non-angles, e.g. a = tan(len1/len2)
      ASTExpression blankSpec = sym.getSpecificationMap().get(blankIdentifier);
      if (blankSpec != null) {
        String unit = ASTHelper.expressionToString(blankSpec);
        if (unit.equals("°") || unit.equals("degrees")) {
          sol = String.valueOf(Double.parseDouble(sol) * (180 / Math.PI));
        }
      }
      answerValue = CalculationHelper.roundToNDecimals(Double.parseDouble(sol), 2);
    }
  }

}
