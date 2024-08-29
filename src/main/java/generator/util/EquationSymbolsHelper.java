package generator.util;

import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import fdl.fdl._ast.ASTExtendedName;
import fdl.fdl._ast.ASTIndex;
import fdl.fdl._ast.ASTSpaces;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._visitor.FdlTraverser;
import fdl.fdl.util.ASTHelper;
import fdl.fdl.visitors.ExpressionPrinter;
import fdl.fdl.visitors.MathJaxExpressionPrinter2;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.mathexpressions._ast.ASTMaxExpression;
import fdl.mathexpressions._ast.ASTMinExpression;
import fdl.mathexpressions._ast.ASTSumExpression;
import fdl.types.containers.Reference;
import generator.exceptions.UnfulfillableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.math.NumberUtils;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class EquationSymbolsHelper {

  public static Map<String, String> generateVariablesWithRefs(EquationSymbol sym, String openVar,
      Map<String, String> givenVariables, Random rand)
      throws UnfulfillableException {
    givenVariables = givenVariables == null ? new HashMap<>() : givenVariables;
    Map<String, String> ret = new HashMap<>(givenVariables);
    // for constants and list indices
    Set<String> immutableVars = new HashSet<>(givenVariables.keySet());

    Set<String> degreeVars = new HashSet<>();
    for(String id : sym.getAliasMap().keySet()) {
      if((sym.getSpecificationMap().get(id) != null &&
          ASTHelper.expressionToString(sym.getSpecificationMap().get(id)).equals("°")) ||
          (sym.getReferenceMap().containsKey(id) && sym.getReferenceMap().get(id).hasUnit() &&
              ASTHelper.expressionToString(sym.getReferenceMap().get(id).getUnitExpression()).equals("°"))) {
        degreeVars.add(id);
      }
    }

    SumIteratorIdentifierCollector sumColl = new SumIteratorIdentifierCollector();
    sym.getRootExpression().accept(sumColl);
    Set<String> iteratorIdentifiers = sumColl.getIteratorIdentifiers();
    immutableVars.addAll(iteratorIdentifiers);
    immutableVars.addAll(sym.getListIdentifiers());

    Map<String, List<String>> listMap = new HashMap<>();
    Map<String, String> indexedIds = new HashMap<>();
    if (sym.getListIdentifiers().size() > 0) {
      IndexIdentifierCollector col = new IndexIdentifierCollector();
      sym.getRootExpression().accept(col);
      indexedIds.putAll(col.getIdentifiersWithIndex());
    }

    if (rand == null) {
      rand = new Random();
    }

    // get ref list, in case set/map is not ordered,
    // move openVar ref to front
    // move lists to back
    List<Entry<String, Reference>> entries = new ArrayList<>(
        sym.getReferenceMap().entrySet().stream().toList());
    List<Entry<String, Reference>> iterList = new ArrayList<>(entries);
    for (Entry<String, Reference> stringReferenceEntry : iterList) {
      //System.out.println(openVar + "==" + entries.get(i).getKey() + " ?: " + entries.get(i).getKey().equals(openVar));
      if (stringReferenceEntry.getKey().equals(openVar)) {
        entries.remove(stringReferenceEntry);
        entries.add(0, stringReferenceEntry);
      } else if (sym.getListIdentifiers().contains(stringReferenceEntry.getKey())) {
        entries.remove(stringReferenceEntry);
        entries.add(stringReferenceEntry);
      }
    }
    //todo: just gen in range and done
    if (entries.size() < 2) {
      return ret;
    }
    //if(sym.getSpecificationMap().containsKey(openVar) && ASTHelper.expressionToString(sym.getSpecificationMap().get(openVar)).equals("°")) {
    //  return ret;
    //}
    if (sym.getListIdentifiers().contains(openVar)) {
      return ret;
    }

    // generate initial values
    int zerosafe = 0;
    for (int i = 1; i < entries.size(); i++) {
      if (givenVariables.containsKey(entries.get(i).getKey())) {
        continue;
      }
      boolean enableZeroSafe = true;
      Entry<String, Reference> entry = entries.get(i);
      ret.put(entry.getKey(), "1234.5678");
      Reference ref = entry.getValue();
      if (ref == null) {
        continue;
      }

      double dist = rand.nextGaussian();
      if (dist < -3) {
        dist = -3;
      } else if (dist > 3) {
        dist = 3;
      }

      double randVal = 1234.5678;
      if (ref.isVariable() || ref.isParameter()) {
        if (iteratorIdentifiers.contains(entry.getKey())) {
          randVal = ref.getMin() + rand.nextInt((int) (ref.getMax() - ref.getMin() + 1));
        } else if (sym.getListIdentifiers().contains(entry.getKey())) {
          int listSize = (int) Double.parseDouble(ret.get(indexedIds.get(entry.getKey())));
          List<String> insert = new ArrayList<>();
          for (int j = 0; j < listSize; j++) {
            double listVal = adaptValueToRef(entry.getKey(),
                (dist + 3) * ((ref.getMax() - ref.getMin()) / 6) + ref.getMin(),
                sym.getReferenceMap());
            insert.add(String.valueOf(listVal));
          }
          listMap.put(entry.getKey(), insert);
          ret.put(entry.getKey(), String.join(";", insert));
          continue;
        } else if (ref.isSetSpace()) {
          enableZeroSafe = false;
          randVal =
              ref.getSetSpace().size() > 0 ? CollectionHelper.choice(ref.getSetSpace(), rand) : 1;
        } else {
          if (ref.getMin() == ref.getMax()) {
            randVal = ref.getMin();
            enableZeroSafe = false;
            immutableVars.add(entry.getKey());
          } else {
            randVal = adaptValueToRef(entry.getKey(),
                (dist + 3) * ((ref.getMax() - ref.getMin()) / 6) + ref.getMin(),
                sym.getReferenceMap());
            if (immutableVars.contains(entry.getKey()) || randVal > ref.getMax()
                || randVal < ref.getMin()) {
              System.out.println("Something generated wrong!:");
              System.out.println(sym.getFullName());
              System.out.println(entry.getKey() + " shouldnt be " + randVal);
            }
            assert !immutableVars.contains(entry.getKey()) || (randVal <= ref.getMax()
                && randVal >= ref.getMin());
            if (ref.hasSpace()) {
              if (ref.getSpace() == ASTSpaces.INT) {
                randVal = Math.floor(randVal);
              } else if (ref.getSpace() == ASTSpaces.FLOAT) {
                randVal =
                    ref.getDecimalSpaces() != -1 ? CalculationHelper.roundToNDecimals(randVal, ref.getDecimalSpaces())
                        : CalculationHelper.roundToNDecimals(randVal, 2);
              }
            }
          }
        }
      } else if (ref.isConstant()) {
        immutableVars.add(entry.getKey());
        randVal = ref.getConstant().getValue();
      }

      // try avoiding 0 as a value, otherwise, or if it can't be helped, add value
      if (enableZeroSafe && randVal == 0 && zerosafe < 10) {
        i--;
        zerosafe++;
      } else {
        zerosafe = 0;
        ret.put(entry.getKey(), String.valueOf(randVal));
      }
    }
/*
    System.out.println("Inital values:");
    for(Entry<String,String> entry : ret.entrySet()) {
      System.out.printf("%s -> %s%n", entry.getKey(), entry.getValue());
    }
*/
    // rename var-to-solve to x for Symja compatibility
    ret.put(openVar, "x");
    //Map<String, String> queryMap = ret;
    //basedVars.isEmpty() ? variableValues : new HashMap<>(variableValues);
    //queryMap.putAll(basedVars);
    //queryMap.putAll(constantMap);

    // build query while replace variables with values
    ValueFiller vis =
        //sym.getListIdentifiers().size() > 0 ? new ValueFiller(queryMap, listValues, sym) :
        new ValueFiller(ret, listMap, sym, true);
    sym.getRootExpression().accept(vis);
    String query = "Solve(" + vis.getExpressionString() + ",x)";

    // hand query to Symja and extract solution
    ExprEvaluator util = new ExprEvaluator();
    IExpr result = util.eval(query);

    String sol = SymjaHelper.getResult(result);
    if (sol == null) {
      return new HashMap<>();
    }
    double dSol = Double.parseDouble(
        sol); //adaptValueToRef(targetVar, Double.parseDouble(sol), sym.getReferenceMap());

    double targetMin = entries.get(0).getValue().getMin();
    double targetMax = entries.get(0).getValue().getMax();
    if (degreeVars.contains(entries.get(0).getKey())) {
      targetMin *= (Math.PI / 180);
      targetMax *= (Math.PI / 180);
    }

    if (dSol < targetMin || dSol > targetMax && !sym.getReferenceMap().get(openVar).isParameter()) {
      int scalingFactor = 1;
      int i = 0;
      int emptyStreak = 0;
      do {
        // non-target var -> impact on targetVar value for 5% increase or decrease of non-target var // null = error
        Map<String, Double> incBehaviorMap = new HashMap<>();
        Map<String, Double> decBehaviorMap = new HashMap<>();
        Map<String, Double> incDegreeMap = new HashMap<>();
        Map<String, Double> decDegreeMap = new HashMap<>();

        boolean invertNeeded = dSol < 0 ? sym.getReferenceMap().get(openVar).getMin() >= 0 :
            sym.getReferenceMap().get(openVar).getMax() < 0;

        for (int j = 1; j < entries.size(); j++) {
          String var = entries.get(j).getKey();
          Reference ref = entries.get(j).getValue();
          if (immutableVars.contains(var)) {
            continue;
          }

          double origValue = Double.parseDouble(ret.get(var));
          double refMin = ref.getMin();
          double refMax = ref.getMax();
          if (degreeVars.contains(var)) {
            refMin *= (Math.PI / 180);
            refMax *= (Math.PI / 180);
          }
          double delta = (refMax - refMin) * (0.4 + 0.05 * emptyStreak);
          if (origValue < refMax) {
            double incVal;
            if (ref.isSetSpace()) {
              incVal = ref.getSetSpace().get(ref.getSetSpace().indexOf(origValue) + 1);
            } else {
              incVal = invertNeeded ?
                  Math.min(refMax,
                      adaptValueToRef(var,
                          origValue + delta,
                          sym.getReferenceMap())) :
                  Math.min(refMax, adaptValueToRef(var, origValue * (1 + scalingFactor * 0.05),
                      sym.getReferenceMap()));
            }
            ret.put(var, String.valueOf(incVal));
            vis = new ValueFiller(ret, listMap, sym, true);
            sym.getRootExpression().accept(vis);
            query = "Solve(" + vis.getExpressionString() + ",x)";
            result = util.eval(query);
            sol = SymjaHelper.getResult(result);
            if (sol != null) {
              incBehaviorMap.put(var, Double.parseDouble(sol));
              incDegreeMap.put(var, incVal / sym.getReferenceMap().get(var).getMax());
            }
          }
          if (origValue > refMin) {
            double decVal;
            if (ref.isSetSpace()) {
              decVal = ref.getSetSpace().get(ref.getSetSpace().indexOf(origValue) - 1);
            } else {
              decVal = invertNeeded ?
                  Math.max(refMin,
                      adaptValueToRef(var, origValue - delta,
                          sym.getReferenceMap())) :
                  Math.max(refMin, adaptValueToRef(var, origValue * (1 - scalingFactor * 0.05),
                      sym.getReferenceMap()));
            }
            ret.put(var, String.valueOf(decVal));
            vis = new ValueFiller(ret, listMap, sym, true);
            sym.getRootExpression().accept(vis);
            query = "Solve(" + vis.getExpressionString() + ",x)";
            result = util.eval(query);
            sol = SymjaHelper.getResult(result);
            if (sol != null) {
              decBehaviorMap.put(var, Double.parseDouble(sol));
              decDegreeMap.put(var, sym.getReferenceMap().get(var).getMin() / decVal);
            }
          }
          ret.put(var, String.valueOf(origValue));
        }

        boolean tweakUp = dSol < entries.get(0).getValue().getMin();
        Map<String, Boolean> tweakableVars = new HashMap<>(); // var -> val is tweaked by increase
        List<Entry<String, Double>> incMap = incBehaviorMap.entrySet().stream().toList();
        List<Entry<String, Double>> decMap = decBehaviorMap.entrySet().stream().toList();
        for (int j = 0; j < Math.max(incMap.size(), decMap.size()); j++) {
          if (j < incMap.size()) {
            if (invertNeeded) {
              if (!sameSign(incMap.get(j).getValue(), dSol)) {
                tweakableVars.put(incMap.get(j).getKey(), Boolean.TRUE);
              }
            } else {
              if (tweakUp && incMap.get(j).getValue() > dSol) {
                tweakableVars.put(incMap.get(j).getKey(), Boolean.TRUE);
              } else if (!tweakUp && incMap.get(j).getValue() < dSol) {
                tweakableVars.put(incMap.get(j).getKey(), Boolean.TRUE);
              }
            }
          }
          if (j < decMap.size()) {
            if (invertNeeded) {
              if (!sameSign(decMap.get(j).getValue(), dSol)) {
                tweakableVars.put(decMap.get(j).getKey(), Boolean.FALSE);
              }
            } else {
              if (tweakUp && decMap.get(j).getValue() > dSol) {
                tweakableVars.put(decMap.get(j).getKey(), Boolean.FALSE);
              } else if (!tweakUp && decMap.get(j).getValue() < dSol) {
                tweakableVars.put(decMap.get(j).getKey(), Boolean.FALSE);
              }
            }
          }
        }
        String varCanidate = null;
        boolean tUp = true;
        double min = 0;
        List<Entry<String, Boolean>> tVarsEntries = tweakableVars.entrySet().stream().toList();
        if (tVarsEntries.size() > 0) {
          varCanidate = tVarsEntries.get(0).getKey();
          tUp = tVarsEntries.get(0).getValue();
          min = tUp ? incDegreeMap.get(varCanidate) : decDegreeMap.get(varCanidate);
          emptyStreak = 0;
        } else {
          scalingFactor++;
          emptyStreak++;
          i++;
          continue;
        }
        for (int j = 1; j < tweakableVars.size(); j++) {
          double comp =
              tVarsEntries.get(j).getValue() ? incDegreeMap.get(tVarsEntries.get(j).getKey()) :
                  decDegreeMap.get(tVarsEntries.get(j).getKey());
          if (comp < min) {
            //double chance = rand.nextDouble();
            //if(chance > i/100d) {
            varCanidate = tVarsEntries.get(j).getKey();
            min = comp;
            tUp = tVarsEntries.get(j).getValue();
            //}
          }
        }
        double oldVal = Double.parseDouble(ret.get(varCanidate));
        Reference ref = sym.getReferenceMap().get(varCanidate);
        double rMin = ref.getMin();
        double rMax = ref.getMax();
        if (degreeVars.contains(varCanidate)) {
          rMin *= (Math.PI / 180);
          rMax *= (Math.PI / 180);
        }
        double delta = (rMax - rMin) * (0.4 + 0.05 * emptyStreak);
        if (ref.isSetSpace()) {
          int origIndex = ref.getSetSpace().indexOf(oldVal);
          ret.put(varCanidate, String.valueOf(
              tUp ? ref.getSetSpace().get(origIndex + 1) : ref.getSetSpace().get(origIndex - 1)));
        } else {
          ret.put(varCanidate, invertNeeded ? (tUp ?
              String.valueOf(adaptValueToRef(varCanidate,
                  Math.min(rMax, oldVal + delta),
                  sym.getReferenceMap())) :
              String.valueOf(adaptValueToRef(varCanidate,
                  Math.max(rMin, oldVal - delta),
                  sym.getReferenceMap()))
          ) : tUp ?
              String.valueOf(
                  adaptValueToRef(varCanidate, Math.min(rMax, oldVal * (1 + scalingFactor * 0.05)),
                      sym.getReferenceMap())) :
              String.valueOf(
                  adaptValueToRef(varCanidate, Math.max(rMin, oldVal * (1 - scalingFactor * 0.05)),
                      sym.getReferenceMap())));
        }
        dSol = tUp ? incBehaviorMap.get(varCanidate) : decBehaviorMap.get(varCanidate);
        //debugHistory.add(varCanidate + " " + oldVal + " -> " + ret.get(varCanidate));

        i++;
        if (i % 10 == 0) {
          scalingFactor++;
        }
      } while (i < 100 && (dSol < entries.get(0).getValue().getMin() || dSol > entries.get(0)
          .getValue().getMax()));
    }

    ret.put(openVar, String.valueOf(dSol));

    //cut X.0 from int-ty doubles
    for (String key : ret.keySet()) {
      String[] tmp = ret.get(key).split("\\.");
      if (tmp.length > 1 && "0".equals(tmp[1])) {
        ret.put(key, tmp[0]);
      }
    }

    return ret;
  }

  private static boolean sameSign(double a, double b) {
    return (a < 0) == (b < 0);
  }

  /**
   * Takes a value and returns it according to the reference, or itself if the references do not
   * contain the variable name. Does NOT consider units.
   *
   * @param varName    name of the variable
   * @param varValue   value of the variable
   * @param references reference map of a EQ-symbol
   * @return a fitting double value
   */
  public static double adaptValueToRef(String varName, double varValue,
      Map<String, Reference> references) {
    Reference ref = references.get(varName);
    if (ref == null) {
      return varValue;
    }
    if (ref.hasRoundingMode()) {
      switch (ref.getRoundingMode()) {
        case ROUND_UP -> {
          return Math.ceil(varValue);
        }
        case ROUND_DOWN -> {
          return Math.floor(varValue);
        }
        case ROUND_NEAREST -> {
          return Math.round(varValue);
        }
        default -> {
          return varValue;
        }
      }
    }
    if (ref.hasSpace()) {
      switch (ref.getSpace()) {
        case INT -> {
          return ref.isForced() ? Math.round(varValue) : CalculationHelper.roundToNDecimals(varValue, 0);
        }
        case FLOAT -> {
          if (ref.getDecimalSpaces() != -1) {
            return CalculationHelper.roundToNDecimals(varValue, ref.getDecimalSpaces());
          }
        }
      }
    }
    return varValue;
  }

  /**
   * Creates a String from an equation given via an EquationSymbol and replaces all occurrences of
   * identifiers, which are present in the according passed map
   */
  public static class ValueFiller extends ExpressionPrinter {

    Map<String, String> valueMap;
    Map<String, List<String>> listMap;
    Map<String, Integer> iteratorMap;
    EquationSymbol sym;
    boolean withDegreeChar;

    public ValueFiller(Map<String, String> valueMap, EquationSymbol sym) {
      this.valueMap = valueMap;
      listMap = new HashMap<>();
      iteratorMap = new HashMap<>();
      this.sym = sym;
      withDegreeChar = false;
    }

    public ValueFiller(Map<String, String> valueMap, Map<String, List<String>> listMap,
        EquationSymbol sym) {
      this.valueMap = valueMap;
      this.listMap = listMap;
      iteratorMap = new HashMap<>();
      this.sym = sym;
      withDegreeChar = false;
    }

    public ValueFiller(Map<String, String> valueMap, Map<String, List<String>> listMap,
        EquationSymbol sym, boolean withDegreeChar) {
      this.valueMap = valueMap;
      this.listMap = listMap;
      iteratorMap = new HashMap<>();
      this.sym = sym;
      this.withDegreeChar = withDegreeChar;
    }

    @Override
    public void traverse(ASTExtendedName node) {
      String name = ASTHelper.extendedNameToString(node);
      String val = valueMap.get(name);

      // turn ° angles into radians
      ASTExpression spec = sym.getSpecificationMap().get(name);
      if (NumberUtils.isParsable(val) && spec != null) {
        String strSpec = ASTHelper.expressionToString(spec);
        if ((strSpec.equals("°") || strSpec.equals("degrees"))) {
          val =
              withDegreeChar ? val + "°" : String.valueOf(Double.parseDouble(val) * Math.PI / 180);
        }
      }
      if (val != null) {
        getSb().append(val);
      }
    }

    @Override
    public void traverse(ASTIndex node) {
      String name = ASTHelper.expressionToString(node.getObject());
      String val = listMap.get(name)
          .get(iteratorMap.get(ASTHelper.expressionToString(node.getIndex())));
      getSb().append(val);
    }

    @Override
    public void traverse(ASTSumExpression node) {
      IndexIdentifierCollector col = new IndexIdentifierCollector();
      sym.getRootExpression().accept(col);
      Map<String, String> indexedIds = col.getIdentifiersWithIndex();
      String var = ASTHelper.expressionToString(node.getVar());
      String sumOverVar = "";
      for (Entry<String, String> entry : indexedIds.entrySet()) {
        if (entry.getValue().equals(var)) {
          sumOverVar = entry.getKey();
          break;
        }
      }
      iteratorMap.put(var, 0);
      for (; iteratorMap.get(var) < listMap.get(sumOverVar).size();
          iteratorMap.computeIfPresent(var, (k, v) -> v + 1)) {
        node.getExpression().accept(this);
        if (iteratorMap.get(var) + 1 < listMap.get(sumOverVar).size()) {
          getSb().append("+");
        }
      }
    }
  }

  /**
   * Creates a String with identifiers being replaced by MathJax'ed versions
   */
  public static class MathJaxStringReplacer extends MathJaxExpressionPrinter2 {

    Map<String, String> valueMap;

    public MathJaxStringReplacer(Map<String, String> replacements) {
      valueMap = replacements;
    }

    @Override
    public void traverse(ASTStringLiteral node) {
      String name = node.getSource();
      getSb().append(valueMap.getOrDefault(name, name));
    }
  }

  /**
   * Collects all identifiers which are used as an index somewhere in the equation; e.g. var[i]
   */
  public static class IndexIdentifierCollector implements FdlTraverser {

    // eg. F[i] -> i
    private final Map<String, String> identifiersWithIndex = new HashMap<>();

    public Map<String, String> getIdentifiersWithIndex() {
      return identifiersWithIndex;
    }

    @Override
    public void visit(ASTIndex node) {
      if (!(node.getObject() instanceof ASTExtendedName)) {
        return;
      }
      identifiersWithIndex.put(ASTHelper.expressionToString(node.getObject()),
          ASTHelper.expressionToString(node.getIndex()));
    }
  }

  /**
   * Collects all identifiers which are used in sum indexes; e.g. sum(i=0;52; eq)
   */
  public static class SumIteratorIdentifierCollector implements FdlTraverser {

    // eg. F[i] -> i
    private final Set<String> iteratorIdentifiers = new HashSet<>();

    public Set<String> getIteratorIdentifiers() {
      return iteratorIdentifiers;
    }

    @Override
    public void visit(ASTSumExpression node) {
      iteratorIdentifiers.add(ASTHelper.expressionToString(node.getVar()));
    }
  }

  /**
   * Collects all identifiers which are used in function calls; eg. Max(a, 3) -> {a}
   */
  public static class IdentifiersInFunctionsCollector implements FdlTraverser {

    private final Set<String> functionIdentifiers = new HashSet<>();
    private int inFuncMutex = 0;

    public Set<String> getIteratorIdentifiers() {
      return functionIdentifiers;
    }

    @Override
    public void traverse(ASTMaxExpression node) {
      inFuncMutex++;
      node.getLeft().accept(this);
      node.getRight().accept(this);
      inFuncMutex--;
    }

    @Override
    public void traverse(ASTMinExpression node) {
      inFuncMutex++;
      node.getLeft().accept(this);
      node.getRight().accept(this);
      inFuncMutex--;
    }

    @Override
    public void visit(ASTExtendedName node) {
      if (inFuncMutex > 0) {
        functionIdentifiers.add(ASTHelper.extendedNameToString(node));
      }
    }
  }

}
