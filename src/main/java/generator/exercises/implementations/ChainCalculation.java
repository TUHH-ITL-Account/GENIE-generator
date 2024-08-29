package generator.exercises.implementations;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTExpression;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.MultiCalculationExercise;
import generator.exercises.supplements.SpecifiedVariableInputCalculationExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.KnowledgeModelHelper;
import generator.util.StringUtils;
import generator.util.TemplateHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ChainCalculation extends MultiCalculationExercise {

  private static final boolean KENNEN = false;
  private static final boolean KOENNEN = true;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;

  List<InputMissingEquationValue> partExercises = new ArrayList<>();

  public ChainCalculation(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "MultiCalculationExercise";
  }

  public ChainCalculation() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public void generateAbstractExercise() throws Exception {
    setupExercise();
    fillTitle();
    fillTexts();
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
    //see fillTexts()
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
    // index n contains a map of a variable onto a value calculated in the nth indexed fdlChain
    List<Map<String, Double>> identifiersToSolutions = new ArrayList<>();
    // which output identifiers go into which input identifiers
    // e.g. fdl1 - 2 - 3
    // [m_gs , m_fm, s, s] would connect m_gs (fdl1) -> m_fm (fdl2) and s (fdl2) -> s (fdl3)
    List<String> identifierChain = new ArrayList<>();
    // find other FDLs to chain with
    String[] fdlNameChain = TemplateHelper.getAdditionalFdls(task.getParameters());
    List<FdlNode> fdlChain = new ArrayList<>();
    fdlChain.add(fdlNode);
    if (fdlNameChain != null) { // FDLs are provided in additional_fdls parameter
      for (String s : fdlNameChain) {
        FdlNode node = task.getCacheReference().getFdlnodes().get(s);
        if (node == null) {
          throw new UnfulfillableException(
              String.format("Referenced FDL '%s' not found in cache of model.", s));
        }
        Map<String, Map<String, Double>> scores = evaluateVariableCloseness(
            fdlChain.get(fdlChain.size() - 1), node);
        String maxId1 = "", maxId2 = "";
        double maxScore = 0;
        for (Entry<String, Map<String, Double>> outer : scores.entrySet()) {
          for (Entry<String, Double> inner : outer.getValue().entrySet()) {
            if (inner.getValue() > maxScore) {
              maxScore = inner.getValue();
              maxId2 = inner.getKey();
              maxId1 = outer.getKey();
            }
          }
        }
        if (maxScore > 0.5 && !maxId1.equals("") && !maxId2.equals("")) {
          identifierChain.add(maxId1);
          identifierChain.add(maxId2);
          fdlChain.add(node);
          identifiersToSolutions.add(new HashMap<>());
        }
      }
    } else { // try and find other equations to chain with
      List<FdlNode> otherNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode, fdlNode.getFdlType(),
          0, 1, new ArrayList<>(), null);
      while (!otherNodes.isEmpty()) {
        FdlNode maxNode = null;
        double maxScore = 0d;
        String maxId1 = "", maxId2 = "";
        for (FdlNode node : otherNodes) {
          Map<String, Map<String, Double>> scores = evaluateVariableCloseness(
              fdlChain.get(fdlChain.size() - 1), node);
          for (Entry<String, Map<String, Double>> outer : scores.entrySet()) {
            for (Entry<String, Double> inner : outer.getValue().entrySet()) {
              if (inner.getValue() > maxScore) {
                maxScore = inner.getValue();
                maxId2 = inner.getKey();
                maxId1 = outer.getKey();
                maxNode = node;
              }
            }
          }

        }
        if (maxScore < 0.5) {
          break;
        }
        fdlChain.add(maxNode);
        identifierChain.add(maxId1);
        identifierChain.add(maxId2);
        otherNodes.remove(maxNode);
        identifiersToSolutions.add(new HashMap<>());
      }
    }
    // generate InputMissingEquationValue for target FDL using ident from chain
    for (int i = 0; i < fdlChain.size(); i++) {
      // generate InputMissingEquationValues using ident from chain
      InputMissingEquationValue ex;
      if (fdlChain.size() == 1) { // there is no chain, only the target FDL itself
        ex = new InputMissingEquationValue(fdlChain.get(i), task);
      } else if (i
          == fdlChain.size() - 1) { // last element in the chain does not need a specific blank
        ex = new SpecifiedVariableInputCalculationExercise(fdlChain.get(i), task,
            identifiersToSolutions.get(i - 1)); //hmmmmm
      } //else if (i == 0) {}
      else { // exercise with potentially given values and a specified blank
        ex = new SpecifiedVariableInputCalculationExercise(fdlChain.get(i), task,
            identifiersToSolutions.get(Math.max(0, i - 1)), identifierChain.get(i * 2)); //hmm
      }
      ex.generateAbstractExercise();
      partExercises.add(ex);
      if (i != fdlChain.size() - 1) {
        identifiersToSolutions.get(i).put(identifierChain.get(i * 2 + 1),
            ex.answerValue); //use "right sided" var of var chain-pair
      }
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
