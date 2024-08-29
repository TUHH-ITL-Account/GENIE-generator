package generator.exercises.implementations;

import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.VariableGroupsSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTExpression;
import fdl.types.containers.Reference;
import fdl.types.units.PhysicalQuantity;
import fdl.types.units.UnitHelper;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.MultiCalculationExercise;
import generator.exercises.supplements.SpecifiedVariableInputCalculationExercise;
import generator.types.CalculationGraph;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.CalculationHelper;
import generator.util.CollectionHelper;
import generator.util.EquationSymbolsHelper.IdentifiersInFunctionsCollector;
import generator.util.EquationSymbolsHelper.MathJaxStringReplacer;
import generator.util.StringUtils;
import generator.util.TemplateHelper;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class PoolCalculation extends MultiCalculationExercise {

  private static final boolean KENNEN = false;
  private static final boolean KOENNEN = true;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;
  protected Map<String, String> usedUnits;
  protected Map<String, EquationSymbol> symbols = new HashMap<>();
  protected Map<String, String> solutionMap = new HashMap<>(); // mjax id -> sol val
  protected List<InputMissingEquationValue> partExercises = new ArrayList<>();
  protected Map<SimpleEntry<String, String>, Double> valueMap = new HashMap<>(); //[id, fdl] -> val
  protected List<SimpleEntry<String, String>> varToGen = new ArrayList<>();
  protected List<FdlNode> usedFdls = new ArrayList<>();
  protected Map<String, String> mathjaxIdMap = new HashMap<>();
  protected List<SimpleEntry<String, String>> varToCalc = new ArrayList<>();
  protected List<String> exerciseText = new ArrayList<>();
  protected Map<String, String> aliasMap = new HashMap<>();
  public PoolCalculation(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "PoolCalculationExercise";
  }

  public PoolCalculation() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  public Map<String, String> getMathjaxIdMap() {
    return mathjaxIdMap;
  }

  public Map<String, String> getAliasMap() {
    return aliasMap;
  }

  public Map<String, String> getUsedUnits() {
    return usedUnits;
  }

  public Map<String, String> getSolutionMap() {
    return solutionMap;
  }

  public List<SimpleEntry<String, String>> getVarToCalc() {
    return varToCalc;
  }

  public List<String> getExerciseText() {
    return exerciseText;
  }

  @Override
  public void generateAbstractExercise() throws Exception {
    setupExercise();
    fillTitle();
    fillText();
    fillAnswers();
    fillAdditionalElements();
    setUsesResources();
  }

  @Override
  protected void fillTitle() {
    title = "Berechnungen in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    StringBuilder provided = new StringBuilder();
    int i = 0;
    for (SimpleEntry<String, String> entry : varToGen) {
      provided.append(((EquationSymbol) task.getCacheReference().getFdlnodes().get(entry.getValue())
              .getSymbol()).getAliasMap().get(entry.getKey())).append(" \\(")
          .append(mathjaxIdMap.get(entry.getKey())).append("\\)").append(": ")
          .append(valueMap.get(entry)).append(" ").append(usedUnits.get(entry.getKey()))
          .append(i < varToGen.size() - 1 ? ", " : ".");
      i++;
    }
    exerciseText.add("Es wurden Ihnen folgende Größen bereitgestellt: " + provided);
    exerciseText.add(
        "Berechnen Sie nun die fehlenden, erfragten Werte. Dabei entspricht die Reihenfolge der Lösungsfelder nicht notwendigerweise der des Vorgehens.");
    exerciseText.add(
        "Sofern das Ergebnis es nicht anders erfordert, runden Sie dabei auf die zweite Nachkommastelle und benutzen Sie einen Punkt als Kommazeichen.");
  }

  @Override
  protected void fillAnswers() {
    for (int i = 0; i < partExercises.size(); i++) {
      answers.add(String.valueOf(partExercises.get(i).answerValue));
    }
  }

  @Override
  protected void fillTexts() {
    Map<String, Integer> imageCount = new HashMap<>();
    /*for(InputMissingEquationValue ex : partExercises) {
      for(String img : ex.sym.getImageMap().values()) {
        Integer exists = imageCount.get(img);
        if(exists != null) {
          imageCount.put(img, exists+1);
        } else {
          imageCount.put(img, 1);
        }
      }
    }
    for (int i = 0; i < partExercises.size(); i++) {
      List<String> subtaskTexts = new ArrayList<>();
      subtaskTexts.add(String.format("%d) %s", i, partExercises.get(i).getTexts().get(0)));
      texts.add(subtaskTexts);
    }*/
    for (int i = 0; i < partExercises.size(); i++) {
      for (String img : partExercises.get(i).sym.getImageMap().values()) {
        Integer exists = imageCount.get(img);
        if (exists != null) {
          imageCount.put(img, exists + 1);
        } else {
          imageCount.put(img, 1);
        }
      }
      List<String> textcopy = new ArrayList<>(partExercises.get(i).getTexts());
      for (String text : textcopy) {
        for (Entry<String, Integer> entry : imageCount.entrySet()) {
          if (text.contains(entry.getKey())) {
            if (imageCount.get(entry.getKey()) > 1) {
              partExercises.get(i).getTexts().remove(text);
              imageCount.put(entry.getKey(), imageCount.get(entry.getKey()) - 1);
            }
          }
        }
      }
    }
    for (int i = 0; i < partExercises.size(); i++) {
      List<String> textcopy = new ArrayList<>(partExercises.get(i).getTexts());
      for (int j = 0; j < textcopy.size(); j++) {
        if (partExercises.get(i).getTexts().get(j).contains("Folgende Abbildung(en) ")
            && j + 1 < partExercises.get(i).getTexts().size() && !partExercises.get(i).getTexts()
            .get(j + 1).contains("img src=")) {
          partExercises.get(i).getTexts().remove(j);
          break;
        }
      }
    }
  }

  @Override
  protected void setupExercise() throws Exception {
    //List<FdlNode> usedFdls = new ArrayList<>();
    usedFdls.add(task.getCacheReference().getFdlnodes().get(task.getFdlName()));
    String[] additionalFdls = TemplateHelper.getAdditionalFdls(task.getParameters());
    if (additionalFdls != null) {
      for (String aFdl : additionalFdls) {
        usedFdls.add(task.getCacheReference().getFdlnodes().get(aFdl));
      }
    } else {
      //todo: find other FDLs
    }
    List<SimpleEntry<String, String>> pseudoOrder = new ArrayList<>();
    FdlNode varGroups = task.getCacheReference().getVarGroupsFdlNode();
    CalculationGraph cGraph = varGroups != null ? new CalculationGraph(usedFdls,
        (VariableGroupsSymbol) varGroups.getSymbol()) : null;
    List<String> fdlOrder = new ArrayList<>();

    // fill in initial unit expressions or null
    Map<String, PhysicalQuantity> dims = UnitHelper.unitObjectsMap;
    Map<String, ASTExpression> originalUnits = new HashMap<>(); //überschreibt andere FDL ids -.-
    for (FdlNode node : usedFdls) {
      EquationSymbol sym = (EquationSymbol) node.getSymbol();
      mathjaxIdMap.putAll(ASTHelper.collectExtendedNamesMap(
          sym.getRootExpression()));
      symbols.put(node.getFdlName(), sym);
      aliasMap.putAll(sym.getAliasMap());
      for (String known : sym.getAliasMap().keySet()) {
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
            for (EquationSymbol sym : symbols.values()) {
              if (sym.getSpecificationMap().containsKey(key)) {
                basedVars.put(key,
                    dims.get(ASTHelper.expressionToString(sym.getSpecificationMap().get(key)))
                        .getBaseUnit());
                break;
              } else {
                for (PhysicalQuantity pq : dims.values()) {
                  if (pq.containsUnit(usedUnit)) {
                    BigDecimal oldValue = BigDecimal.valueOf(
                        valueMap.get(new SimpleEntry<>(key, sym.getName())));
                    basedVars.put(key, pq.toBaseUnit(oldValue, usedUnit).toString());
                    break;
                  }
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

    // select vars to gen and to calc
    if (varGroups != null) {
      List<SimpleEntry<String, String>> identifiers = new ArrayList<>();
      List<SimpleEntry<String, String>> singleVars = new ArrayList<>();
      Set<SimpleEntry<String, String>> mustGenIdentifiers = new HashSet<>();

      for (FdlNode node : usedFdls) {
        IdentifiersInFunctionsCollector funcIdColl = new IdentifiersInFunctionsCollector();
        ((EquationSymbol) node.getSymbol()).getRootExpression().accept(funcIdColl);
        Set<String> identifiersInEvilFunctions = funcIdColl.getIteratorIdentifiers();
        for (String id : identifiersInEvilFunctions) {
          mustGenIdentifiers.add(new SimpleEntry<>(id, node.getFdlName()));
        }
        for (String id : ((EquationSymbol) node.getSymbol()).getAliasMap().keySet()) {
          if (((EquationSymbol) node.getSymbol()).getReferenceMap().get(id).isConstant()) {
            continue;
          }
          SimpleEntry<String, String> pair = new SimpleEntry<>(id, node.getFdlName());
          identifiers.add(pair);
          for (List<SimpleEntry<String, String>> partOf : cGraph.getVarGroups()
              .getVariableToGroupsMap().get(pair)) {
            if (partOf.size() == 1) {
              singleVars.add(pair);
            }
          }
        } //identifiers ordnen nach Anzahl?
      }
      do {
        Map<String, Integer> counter = new HashMap<>();
        for (SimpleEntry<String, String> id : identifiers) {
          counter.putIfAbsent(id.getValue(), 0);
          counter.computeIfPresent(id.getValue(), (k, v) -> v + 1);
        }
        boolean lastVarFound = false;
        boolean noNewChange = false;
        while (!noNewChange && !lastVarFound) {
          noNewChange = true;
          for (String fdl : counter.keySet()) {
            if (counter.get(fdl) == 1) {
              lastVarFound = true;
              noNewChange = false;
              SimpleEntry<String, String> lastId = new SimpleEntry<>(null, null);
              for (SimpleEntry<String, String> idFdl : identifiers) {
                if (Objects.equals(idFdl.getValue(), fdl)) {
                  lastId = idFdl;
                  break;
                }
              }
              pseudoOrder.add(lastId);
              varToCalc.add(lastId);
              fdlOrder.add(lastId.getValue()); //check if already contains?
              CollectionHelper.removeAll(identifiers, lastId);
              singleVars.remove(lastId);
              for (List<SimpleEntry<String, String>> group : cGraph.getVarGroups()
                  .getVariableToGroupsMap()
                  .get(lastId)) {
                for (SimpleEntry<String, String> id : group) {
                  CollectionHelper.removeAll(identifiers, id);
                  counter.computeIfPresent(id.getValue(), (k, v) -> v - 1);
                }
              }
            }
          }
        }
        if (!lastVarFound) {
          SimpleEntry<String, String> pick;
          if (!mustGenIdentifiers.isEmpty()) {
            pick = CollectionHelper.choice(mustGenIdentifiers, task.getRandomInstance());
            CollectionHelper.removeAll(mustGenIdentifiers, pick);
          } else {
            if (singleVars.size() == 1) {
              List<SimpleEntry<String, String>> tmp = new ArrayList<>(identifiers);
              tmp.remove(singleVars.get(0));
              pick = CollectionHelper.choice(tmp, task.getRandomInstance());
            } else {
              pick = CollectionHelper.choice(identifiers, task.getRandomInstance());
            }
          }
          CollectionHelper.removeAll(identifiers, pick);
          singleVars.remove(pick);
          pseudoOrder.add(pick);
          varToGen.add(pick);
          for (List<SimpleEntry<String, String>> group : cGraph.getVarGroups()
              .getVariableToGroupsMap()
              .get(pick)) {
            for (SimpleEntry<String, String> id : group) {
              CollectionHelper.removeAll(identifiers, id);
            }
          }
        }
      } while (identifiers.size() > 0);
    } else {
      //todo: "guess" connections
    }

    // Generate selected values
    for (SimpleEntry<String, String> idFdl : varToGen) {
      if (valueMap.containsKey(idFdl)) {
        continue;
      }
      double val = Double.parseDouble(CalculationHelper.generateValueToReference(
          ((EquationSymbol) task.getCacheReference().getFdlnodes().get(idFdl.getValue())
              .getSymbol()).getReferenceMap()
              .get(idFdl.getKey()), task.getRandomInstance())); //todo: use normal dist?
      valueMap.put(idFdl, val);
      if (varGroups != null) {
        for (List<SimpleEntry<String, String>> group : ((VariableGroupsSymbol) varGroups.getSymbol()).getVariableToGroupsMap()
            .get(idFdl)) {
          for (SimpleEntry<String, String> pair : group) {
            valueMap.put(pair, val);
          }
        }
      }
    }

    // Calculate missing values
    for (SimpleEntry<String, String> idFdl : varToCalc) {
      FdlNode node = task.getCacheReference().getFdlnodes().get(idFdl.getValue());
      Map<String, Double> givenIdentifierValues = new HashMap<>();
      for (SimpleEntry<String, String> pair : valueMap.keySet()) {
        if (Objects.equals(pair.getValue(), idFdl.getValue())) {
          givenIdentifierValues.put(pair.getKey(), valueMap.get(pair));
        }
      }
      InputMissingEquationValue ex = new SpecifiedVariableInputCalculationExercise(node, task,
          givenIdentifierValues, idFdl.getKey());
      ex.generateAbstractExercise();
      partExercises.add(ex);
      if (varGroups != null) {
        for (List<SimpleEntry<String, String>> group : ((VariableGroupsSymbol) varGroups.getSymbol()).getVariableToGroupsMap()
            .get(idFdl)) {
          for (SimpleEntry<String, String> pair : group) {
            valueMap.put(pair, ex.answerValue);
          }
        }
      }
    }
    for (SimpleEntry<String, String> entry : varToCalc) {
      solutionMap.put(entry.getKey(), String.valueOf(valueMap.get(entry)));
    }
  }

  /**
   * Calculates how close each variable in fdl1 is to each variable in fdl2 by comparing their
   * specification, identifiers and full aliases.
   *
   * @return a map of the identifiers in fdl1 onto a map of identifiers in fdl2 onto closeness
   * values
   */
  private Map<String, Map<String, Double>> evaluateVariableCloseness(FdlNode fdl1, FdlNode fdl2) {
    assert fdl1.getSymbol() instanceof EquationSymbol && fdl2.getSymbol() instanceof EquationSymbol;
    EquationSymbol sym1 = ((EquationSymbol) fdl1.getSymbol()), sym2 = ((EquationSymbol) fdl2.getSymbol());
    Map<String, Map<String, Double>> ret = new HashMap<>();
    for (String fdl1Id : sym1.getAliasMap().keySet()) {
      Map<String, Double> scores = new HashMap<>();
      for (String fdl2Id : sym2.getAliasMap().keySet()) {
        ASTExpression spec1 = sym1.getSpecificationMap().get(fdl1Id);
        ASTExpression spec2 = sym2.getSpecificationMap().get(fdl2Id);
        if ((spec1 == null ^ spec2 == null) || spec1 != null &&
            !ASTHelper.expressionToString(spec1).equals(ASTHelper.expressionToString(spec2))) {
          scores.put(fdl2Id, 0d);
          continue;
        }
        scores.put(fdl2Id, StringUtils.similarity(fdl1Id, fdl2Id) + 2 * StringUtils.similarity(
            sym1.getAliasMap().get(fdl1Id), sym2.getAliasMap().get(fdl2Id)));
      }
      ret.put(fdl1Id, scores);
    }
    return ret;
  }

  @Override
  protected void fillAdditionalElements() {
    // MathJax 3.x
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }

  public List<InputMissingEquationValue> getPartExercises() {
    return partExercises;
  }

  @SuppressWarnings("unchecked")
  protected void setUsesResources() {
    for (InputMissingEquationValue ex : partExercises) {
      Collection<String> images = ((EquationSymbol) ex.getFdlNode().getSymbol()).getImageMap()
          .values();
      if (!images.isEmpty()) {
        task.getParameters().putIfAbsent("usesResources", new HashSet<String>());
        ((Set<String>) task.getParameters().get("usesResources")).addAll(images);
      }
    }
  }

  // this template is disabled from random generation
  @Override
  public boolean isEnabled() {
    return false;
  }
}
