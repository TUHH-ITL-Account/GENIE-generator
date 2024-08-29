package generator.util;

import static java.lang.Math.min;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import fdl.fdl._ast.ASTSpaces;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.types.containers.Reference;
import fdl.types.units.PhysicalQuantity;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.types.GenerationTask;
import generator.util.EquationSymbolsHelper.IdentifiersInFunctionsCollector;
import generator.util.EquationSymbolsHelper.IndexIdentifierCollector;
import generator.util.EquationSymbolsHelper.MathJaxStringReplacer;
import generator.util.EquationSymbolsHelper.SumIteratorIdentifierCollector;
import generator.util.EquationSymbolsHelper.ValueFiller;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class CalculationHelper {


  public static CalculationContainer setupExercise(FdlNode fdlNode, GenerationTask task)
      throws UnfulfillableException {
    CalculationContainer calcContainer = new CalculationContainer();
    Map<String, String> constantMap = calcContainer.constantMap;
    List<String> variableOrder = calcContainer.variableOrder;
    Map<String, String> variableValues = calcContainer.variableValues;
    Map<String, List<String>> listValues = calcContainer.listValues;
    Map<String, String> presentIdentifiers = calcContainer.presentIdentifiers;
    Map<String, String> usedUnits = calcContainer.usedUnits;
    double answerValue = 1234.5678d;

    Random rand = task.getRandomInstance();
    if (rand == null) {
      rand = new Random();
    }

    EquationSymbol sym = (EquationSymbol) fdlNode.getSymbol();
    calcContainer.sym = sym;
    int difficulty = getDifficultyOrDefault(task, 6);
    presentIdentifiers.putAll(ASTHelper.collectExtendedNamesMap(
        ((EquationSymbol) fdlNode.getSymbol()).getRootExpression())); //Liste wird mit Root Expressions gefüllt todo: Variablen von Parametern und Konstanten trennen
    List<String> openVariablesAndParameters = new ArrayList<>();

    //todo: unify traversers via visitor registering
    SumIteratorIdentifierCollector sumColl = new SumIteratorIdentifierCollector();
    sym.getRootExpression().accept(sumColl);
    Set<String> iteratorIdentifiers = sumColl.getIteratorIdentifiers();

    IdentifiersInFunctionsCollector funcIdColl = new IdentifiersInFunctionsCollector();
    sym.getRootExpression().accept(funcIdColl);
    Set<String> identifiersInEvilFunctions = funcIdColl.getIteratorIdentifiers();

    for (String id : presentIdentifiers.keySet()) {
      //Wenn Konstante, dann wird sie zur Constant Map hinzugefügt
      if (sym.getReferenceMap().containsKey(id) && sym.getReferenceMap().get(id)
          .isConstant()) {
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
    variableOrder.addAll(
        openVariablesAndParameters); //Anhand der variablen Variablen wird die Liste variable orders erstellt
    if (difficulty > 5) {
      Collections.shuffle(variableOrder.subList(1, variableOrder.size()),
          rand);
    } else {
      Collections.shuffle(variableOrder, rand);
    }
    // add identifiers which require a value / are not allowed to be the unknown at the start
    for (String id : identifiersInEvilFunctions) {
      variableOrder.add(0, id);
    }

    Map<String, String> res = new HashMap<>();
    boolean foundGoodValues = false;
    int valueIteration = 0;
    while (!foundGoodValues && valueIteration < 50) {
      try {
        res.putAll(EquationSymbolsHelper.generateVariablesWithRefs(
            sym, variableOrder.get(variableOrder.size() - 1), new HashMap<>(), rand));

        if (res.size() >= variableOrder.size()) {
          //System.out.println("new ok");
          variableValues.putAll(res);//(;
          /*variableValues.put(variableOrder.get(variableOrder.size() - 1),
              String.valueOf(cautiousRoundTo2Decimals(Double.parseDouble(
                  variableValues.get(variableOrder.get(variableOrder.size() - 1))))));*/
          for (String listId : sym.getListIdentifiers()) {
            listValues.put(listId, List.of(res.get(listId).split(";")));
          }
        } else {
          // assign all except last variable in order a random value
          for (int i = 0; i < variableOrder.size() - 1; i++) {
            variableValues.put(variableOrder.get(i),
                generateValueToReference(sym.getReferenceMap().get(variableOrder.get(i)), rand));
          }
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
              int numElements = rand
                  .nextInt((int) iteratorRef.getMax() - (int) iteratorRef.getMin() + 1) + 1;
              for (int j = 0; j < numElements; j++) {
                listValues.get(sym.getListIdentifiers().get(i)).add(
                    generateValueToReference(
                        sym.getReferenceMap().get(sym.getListIdentifiers().get(i)), rand));
              }
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
                if (sym.getSpecificationMap().containsKey(key)) {
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

        if (res.size() >= variableOrder.size()) {
          answerValue = Double.parseDouble(
              variableValues.get(variableOrder.get(variableOrder.size() - 1)));
        } else {
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
          String query = "Solve(" + vis.getExpressionString() + ",x,Reals)";

          // hand query to Symja and extract solution
          ExprEvaluator util = new ExprEvaluator();
          IExpr result = util.eval(query);

          String sol = SymjaHelper.getResult(result);
          if (sol == null) {
            throw new UnfulfillableException("CAS returned unknown answer?: " + result);
          }
          answerValue = Double.parseDouble(sol);
        }
        Reference toSolveRef = sym.getReferenceMap()
            .get(variableOrder.get(variableOrder.size() - 1));
        if (toSolveRef != null && toSolveRef.hasRoundingMode()) {
          switch (toSolveRef.getRoundingMode()) {
            case ROUND_UP -> answerValue = (new BigDecimal(answerValue).setScale(0,
                RoundingMode.UP)).doubleValue();
            case ROUND_DOWN -> answerValue = (new BigDecimal(answerValue).setScale(0,
                RoundingMode.DOWN)).doubleValue();
            case ROUND_NEAREST -> answerValue = (new BigDecimal(answerValue).setScale(0,
                RoundingMode.HALF_UP)).doubleValue();
          }
        } else {
          // If we have a (degree) angle calculated from non-angles, e.g. a = tan(len1/len2)
          ASTExpression blankSpec = sym.getSpecificationMap()
              .get(variableOrder.get(variableOrder.size() - 1));
          if (blankSpec != null) {
            String unit = ASTHelper.expressionToString(blankSpec);
            if (unit.equals("°") || unit.equals("degrees")) {
              answerValue *= (180 / Math.PI);
            }
          }
          answerValue = CalculationHelper.roundToNDecimals(answerValue, 2);
        }
        foundGoodValues = true;
      } catch (UnfulfillableException ue) {
        valueIteration++;
      }
    }
    calcContainer.answerValue = answerValue;
    return calcContainer;
  }

  public static String generateValueToReference(Reference ref, Random rand) {
    // todo: use int if not specified as floating point
    if (rand == null) {
      rand = new Random();
    }
    if (ref != null) {
      double lower, upper;
      // if no upper bound is specified we generate a number within +- 10, but dont go above -1 or below 1
      if (ref.getMin() == ref.getMax()) {
        if (ref.getMin() < 0) {
          upper = Math.max(-1, ref.getMin());
          lower = ref.getMin() - 10;
        } else if (ref.getMin() > 0) {
          lower = min(ref.getMin(), 1);
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
              rand.nextInt((int) (upper - lower)) + (int) lower);
        } else {
          double randNum = rand.nextDouble() * (upper - lower) + lower;
          return ref.getDecimalSpaces() != -1 ?
              String.valueOf(
                  BigDecimal.valueOf(randNum)
                      .setScale(ref.getDecimalSpaces(), RoundingMode.HALF_UP)) :
              String.valueOf(BigDecimal.valueOf(randNum).setScale(3, RoundingMode.HALF_UP));
        }
      }
    }
    return String.valueOf(rand.nextInt(18) + 2);
  }

  private static int getDifficultyOrDefault(GenerationTask task, int defaultDifficulty) {
    Integer diff = (Integer) task.getParameters().get("difficulty");
    if (diff == null) {
      task.getParameters().put("difficulty", defaultDifficulty);
      return defaultDifficulty;
    }
    return diff;
  }

  public static double roundToNDecimals(double value, int n) {
    return (BigDecimal.valueOf(value).setScale(n, RoundingMode.HALF_UP)).doubleValue();
  }

  public static class CalculationContainer {

    public Map<String, String> constantMap;
    public List<String> variableOrder;
  public Map<String, String> variableValues;
    public Map<String, List<String>> listValues;
    public Map<String, String> presentIdentifiers;
    public Map<String, String> usedUnits;
    public double answerValue;
    public EquationSymbol sym;

    public CalculationContainer() {
      constantMap = new HashMap<>();
      variableOrder = new ArrayList<>();
      variableValues = new HashMap<>();
      listValues = new HashMap<>();
      presentIdentifiers = new HashMap<>();
      usedUnits = new HashMap<>();
      answerValue = 1234.5678d;
    }
  }

}
