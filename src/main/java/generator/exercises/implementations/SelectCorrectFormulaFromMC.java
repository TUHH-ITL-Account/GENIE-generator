package generator.exercises.implementations;

import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl.util.ASTHelper;
import fdl.mathexpressions._ast.ASTAssignmentExpression;
import fdl.mathexpressions._ast.ASTExpression;
import generator.caching.FdlNode;
import generator.caching.TopicNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.EquationSymbolsHelper;
import generator.util.EquationSymbolsHelper.MathJaxStringReplacer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class SelectCorrectFormulaFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EQUATION;
  private final List<String> exactDistractors = new ArrayList<>();
  private final List<String> inaccurateDistractors = new ArrayList<>();
  private final List<String> correct = new ArrayList<>();
  private String alias, specification, reference;
  private Map<String, String> presentIdentifiers;
  private TopicNode originNode;
  private EquationSymbol sym;


  public SelectCorrectFormulaFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicMultipleChoice";
  }

  public SelectCorrectFormulaFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    if (sym instanceof EquationSymbol && ((EquationSymbol) sym).hasSingleLeftSide()) {
      ASTExpression expression = ((EquationSymbol) sym).getRootExpression();
      ASTExpression expressionLeft = ((ASTAssignmentExpression) expression).getLeft();
      return (((EquationSymbol) sym).getAliasMap()
          .containsKey(ASTHelper.expressionToString(expressionLeft)));
    }
    return false;
  }

  @Override
  public void generateAbstractExercise() throws UnfulfillableException {
    setUpExercise();
    fillTitle();
    fillText();
    fillCorrectOptions();
    fillWrongOptions();
    calcOptions();
    fillAdditionalElements();
  }

  private void setUpExercise() {
    sym = ((EquationSymbol) fdlNode.getSymbol());
    presentIdentifiers = ASTHelper.collectExtendedNamesMap(sym.getRootExpression());
  }

  @Override
  protected void fillTitle() {
    title = "Formeln in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    ASTExpression expression = sym.getRootExpression();
    ASTExpression expressionLeft = ((ASTAssignmentExpression) expression).getLeft();
    String formulaSign = ASTHelper.expressionToString(expressionLeft);
    texts.add(String.format("Selektieren Sie alle Formeln, welche '%s' definieren.",
        sym.getAliasMap().getOrDefault(formulaSign, fdlNode.getFdlName())));
  }

  @Override
  protected void fillCorrectSet() throws UnfulfillableException {

  }

  @Override
  protected void fillWrongSet() throws UnfulfillableException {

  }

  @Override
  protected void fillCorrectOptions() {
    ASTExpression expression = sym.getRootExpression();
    ASTExpression expressionLeft = ((ASTAssignmentExpression) expression).getLeft();
    String formulaSign = ASTHelper.expressionToString(expressionLeft);
    int numCorrect;
    alias = sym.getAliasMap().getOrDefault(formulaSign, "no Alias");
    if (sym.getReferenceMap().get(formulaSign) != null
        && sym.getReferenceMap().get(formulaSign).getUnitExpression() != null) {
      reference = ASTHelper.expressionToString(
          sym.getReferenceMap().get(formulaSign).getUnitExpression());
    } else {
      reference = "no Reference";
    }
    if (sym.getSpecificationMap().containsKey(formulaSign)) {
      specification = ASTHelper.expressionToString(sym.getSpecificationMap().get(formulaSign));
    } else {
      specification = "no Specification";
    }

    originNode = fdlNode.getTopicNode();
    int steps = searchThroughChildren(fdlNode.getTopicNode(), 0, true);
    for (int i = 0; i < 10 - steps; i++) {
      if (originNode.getParent() != null) {
        searchThroughChildren(originNode.getParent(), steps + i, true);
        originNode = originNode.getParent();
      } else {
        break;
      }
    }

    numCorrect = min(max(getDifficultyOrDefault() / 3, 1), correct.size());
    Collections.shuffle(correct, task.getRandomInstance());
    for (int i = 0; i < numCorrect; i++) {
      correctOptions.add(correct.get(i));
    }
  }

  @Override
  protected void fillWrongOptions() {
    List<String> distractors = new ArrayList<>();
    int numDistractors = max(getDifficultyOrDefault() / 2, 2);
    originNode = fdlNode.getTopicNode();
    int steps = searchThroughChildren(fdlNode.getTopicNode(), 0, false);
    for (int i = 0; i < 10 - steps; i++) {
      if (originNode.getParent() == null) {
        break;
      }
      searchThroughChildren(originNode.getParent(), steps + i, false);
      originNode = originNode.getParent();
    }
    distractors.addAll(exactDistractors);
    distractors.addAll(inaccurateDistractors);
    Collections.shuffle(distractors, task.getRandomInstance());
    for (int i = 0; i < Math.min(numDistractors, distractors.size()); i++) {
      wrongOptions.add(distractors.get(i));
    }
  }

  private int searchThroughChildren(TopicNode node, int step, boolean correctOptions) {
    step++;
    for (FdlNode fdl : node.getFdls()) {
      if (fdl.getFdlType() == fdlNode.getFdlType()) {
        ASTExpression expression = ((EquationSymbol) fdl.getSymbol()).getRootExpression();
        ASTExpression expressionLeft = ((ASTAssignmentExpression) expression).getLeft();
        ASTExpression expressionRight = ((ASTAssignmentExpression) expression).getRight();

        EquationSymbol fdlSym = (EquationSymbol) fdl.getSymbol();

        String fdlLeftExpression = ASTHelper.expressionToString(expressionLeft);
        String fdlLeftAlias;
        String fdlLeftSpecification;
        String fdlReference;
        StringBuilder formula = new StringBuilder();

        Map<String, String> fdlAliasMap = new HashMap<>(fdlSym.getAliasMap());

        presentIdentifiers = new HashMap<>(ASTHelper.collectExtendedNamesMap(expression));
        EquationSymbolsHelper.MathJaxStringReplacer replacer;

        if (fdlSym.getSpecificationMap().containsKey(fdlLeftExpression)) {
          fdlLeftSpecification = ASTHelper.expressionToString(
              fdlSym.getSpecificationMap().get(fdlLeftExpression));
        } else {
          fdlLeftSpecification = "no Specification";
        }

        fdlLeftAlias = fdlSym.getAliasMap().getOrDefault(fdlLeftExpression, "no Alias");

        if (fdlSym.getReferenceMap().get(fdlLeftExpression) != null
            && fdlSym.getReferenceMap().get(fdlLeftExpression).getUnitExpression() != null) {
          fdlReference = ASTHelper.expressionToString(
              fdlSym.getReferenceMap().get(fdlLeftExpression).getUnitExpression());
        } else {
          fdlReference = "no Reference";
        }

        replacer = new MathJaxStringReplacer(presentIdentifiers);
        if (expressionRight != null) {
          expressionRight.accept(replacer);
          List<String> extendedNamesRight = new ArrayList<>(
              ASTHelper.collectExtendedNames(expressionRight));
          formula = new StringBuilder();
          formula.append("\\(").append(replacer.getSb().toString()).append("\\)")
              .append("<br>");
          ListIterator<String> extendedNamesRightIterator = extendedNamesRight.listIterator();
          while (extendedNamesRightIterator.hasNext()) {
            String varName = extendedNamesRightIterator.next();
            int index = extendedNamesRightIterator.nextIndex();
            if (fdlAliasMap.containsKey(varName)) {
              formula.append("\\(").append(presentIdentifiers.get(varName)).append("\\)")
                  .append(": ");
              formula.append(fdlAliasMap.get(varName));
              if (extendedNamesRightIterator.hasNext() && fdlAliasMap.containsKey(
                  extendedNamesRight.get(index))) {
                formula.append("; ");
              }
            } else if (extendedNamesRightIterator.hasNext() && fdlAliasMap.containsKey(
                extendedNamesRight.get(max(0, index - 2)))) {
              formula.append("; ");
            }
          }
        }

        if (correctOptions) {
          if (fdlLeftAlias != null && fdlLeftAlias.equals(alias)) {
            if (!this.correct.contains(formula.toString())) {
              this.correct.add(formula.toString());
            }
          }
        } else {
          if (fdlLeftAlias.equals(alias)) {
            if (!exactDistractors.contains(formula.toString()) && !this.correctOptions.contains(
                formula.toString()) && !formula.toString().equals("")) {
              exactDistractors.add(formula.toString());
            }
          } else if (fdlLeftSpecification != null && specification != null) {
            if (specification.contains(fdlLeftSpecification) || reference.equals(fdlReference)) {
              if (!inaccurateDistractors.contains(formula.toString())
                  && !this.correctOptions.contains(formula.toString()) && !formula.toString()
                  .equals("")) {
                inaccurateDistractors.add(formula.toString());
              }
            }
          }
        }
      }
    }
    if (node.getChildren().size() != 0 && (!alias.equals("no Alias") || !correctOptions)) {
      for (TopicNode nextNode : node.getChildren()) {
        if (!nextNode.getNodeName().equals(originNode.getNodeName())) {
          searchThroughChildren(nextNode, step, correctOptions);
        }
      }
    }
    return Math.min(step, 10);
  }

  protected void fillAdditionalElements() {
    additionalElements.add(
        "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>");
    additionalElements.add(
        "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3.2.0/es5/tex-chtml.js\"></script>");
  }
}
