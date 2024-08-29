package generator.exercises.implementations;

import fdl.fdl._symboltable.HierarchySymbol;
import fdl.fdl._symboltable.HierarchySymbol.HierarchyNode;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.FillOutExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputHierarchyLabels extends FillOutExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.HIERARCHY;
  public final String SOLUTION_FORMAT = "STRING";
  protected final List<String> answers = new ArrayList<>();
  private final Set<Integer> filledNodes = new HashSet<>();
  private final Set<HierarchyNode> filledSubtrees = new HashSet<>();
  private List<String> treeParts = new ArrayList<>();

  public InputHierarchyLabels(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "HierarchyTreeLabeling";
  }

  public InputHierarchyLabels() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof HierarchySymbol) && ((HierarchySymbol) sym).getHierarchyRoot() != null;
  }

  public void generateAbstractExercise() {
    setup();
    fillTitle();
    fillText();
    fillAdditionalElements();
  }

  @Override
  public void generateSolutionMap() {
    solutionMap = new HashMap<>();
    solutionMap.put("SOLUTION_FORMAT", SOLUTION_FORMAT);
    for (int i = 0; i < answers.size(); i++) {
      solutionMap.put(HTML_ID_NAME_PREFIX + i, answers.get(i));
    }
  }

  protected void setup() {
    selectBlankNodes(((HierarchySymbol) fdlNode.getSymbol()).getHierarchyRoot());
    adaptToDifficulty();
    buildHtmlTree();
  }

  @Override
  protected void fillTitle() {
    title = "Hierarchien in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format("Füllen Sie die freien Felder in der Hierarchie für '%s' aus.",
        ((HierarchySymbol) fdlNode.getSymbol()).getObject()));
  }

  private void selectBlankNodes(HierarchyNode root) {
    identifyGivenSubtrees(root);
    pickBlanks(root);
  }

  private void identifyGivenSubtrees(HierarchyNode root) {
    if (root.getParent() != null) {
      for (HierarchyNode sibling : root.getParent().getChildren()) {
        if (root.getId() != sibling.getId()) {
          if (root.getStructure().equals(sibling.getStructure())) {
            if (!filledSubtrees.contains(root) && !filledSubtrees.contains(
                sibling)) {
              filledSubtrees.add(
                  task.getRandomInstance().nextInt(100) < 50 ? root : sibling);
            }
          }
        }
      }
    }
    for (HierarchyNode child : root.getChildren()) {
      selectBlankNodes(child);
    }
  }

  private void pickBlanks(HierarchyNode root) {
    if (filledSubtrees.contains(root)) {
      int r = task.getRandomInstance().nextInt(root.getChildren().size() + 1);
      if (r == root.getChildren().size()) {
        filledNodes.add(root.getId());
        filledSubtrees.remove(root);
      } else {
        filledSubtrees.remove(root);
        filledSubtrees.add(root.getChildren().get(r));
      }
    }
    for (HierarchyNode child : root.getChildren()) {
      pickBlanks(child);
    }
  }

  /**
   * Aim to fulfil difficulty * 0.1 = percentage of nodes left blank
   */
  private void adaptToDifficulty() {
    HierarchyNode root = ((HierarchySymbol) fdlNode.getSymbol()).getHierarchyRoot();

    int numNodes = countNodes(root);
    int nodesToFill =
        (int) Math.round(numNodes * (1 - getDifficultyOrDefault() * 0.1)) - filledNodes.size();
    if (nodesToFill > 0) {
      List<Integer> nodes = new ArrayList<>();
      for (int i = 0; i < numNodes; i++) {
        if (!filledNodes.contains(i)) {
          nodes.add(i);
        }
      }
      Collections.shuffle(nodes, task.getRandomInstance());
      for (int i = 0; i < nodesToFill; i++) {
        filledNodes.add(i);
      }
    }
  }

  private int countNodes(HierarchyNode node) {
    int i = 1;
    for (HierarchyNode child : node.getChildren()) {
      i = i + countNodes(child);
    }
    return i;
  }

  private void buildHtmlTree() {
    HierarchyNode root = ((HierarchySymbol) fdlNode.getSymbol()).getHierarchyRoot();

    String sb = "<ul class=\"tree\">"
        + processNode(root)
        + "</ul>";
    treeParts = Arrays.stream(sb.split("§HIERARCHY_SPLIT§")).toList();
  }

  private String processNode(HierarchyNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append("<li><span>");
    if (filledNodes.contains(node.getId())) {
      sb.append(node.getContent());
    } else {
      sb.append("§HIERARCHY_SPLIT§");
      answers.add(node.getContent());
    }
    sb.append("</span>");
    if (!node.getChildren().isEmpty()) {
      sb.append("<ul>");
      for (HierarchyNode child : node.getChildren()) {
        sb.append(processNode(child));
      }
      sb.append("</ul>");
    }
    sb.append("</li>");
    return sb.toString();
  }

  public List<String> getTreeParts() {
    return treeParts;
  }

  public List<String> getAnswers() {
    return answers;
  }

  private void fillAdditionalElements() {
    additionalElements.add("""
        <style>
            .tree, .tree ul, .tree li {
            list-style: none;
            margin: 0;
            padding: 0;
            position: relative;
        }
        .tree {
            margin: 0 0 1em;
            text-align: center;
        }
        .tree, .tree ul {
            display: table;
        }
        .tree ul {
            width: 100%;
        }
        .tree li {
            display: table-cell;
            padding: .5em 0;
            vertical-align: top;
        }
        .tree li:before {
            outline: solid 1px #666;
            content: "";
            left: 0;
            position: absolute;
            right: 0;
            top: 0;
        }
        .tree li:first-child:before {
            left: 50%;
        }
        .tree li:last-child:before {
            right: 50%;
        }
        .tree code, .tree span {
            border: solid .1em #666;
            border-radius: .2em;
            display: inline-block;
            margin: 0 .2em .5em;
            padding: .2em .5em;
            position: relative;
        }
        .tree ul:before, .tree code:before, .tree span:before {
            outline: solid 1px #666;
            content: "";
            height: .5em;
            left: 50%;
            position: absolute;
        }
        .tree ul:before {
            top: -.5em;
        }
        .tree code:before, .tree span:before {
            top: -.55em;
        }
        .tree>li {
            margin-top: 0;
        }
        .tree>li:before, .tree>li:after, .tree>li>code:before, .tree>li>span:before {
            outline: none;
        }
        </style>""");
  }
}