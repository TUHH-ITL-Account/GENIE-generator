package generator.exercises.implementations;

import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.fdl._symboltable.ExamplesSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.types.containers.Example;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.KnowledgeModelHelper;
import generator.util.TemplateHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectExamplesFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EXAMPLES;

  private final ExamplesSymbol symbol;

  public SelectExamplesFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicMultipleChoice";
    symbol = (ExamplesSymbol) node.getSymbol();
  }

  public SelectExamplesFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
    template = "BasicMultipleChoice";
    symbol = null;
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    if (!(sym instanceof ExamplesSymbol)) {
      return false;
    }
    if (!((ExamplesSymbol) sym).getExamples().isEmpty()) {
      return true;
    }
    return !((ExamplesSymbol) sym).getSuperFdls().isEmpty();
    //return findEntry((ExamplesSymbol)sym, false); //kritisch, da hier kein CacheRef gegeben ist
  }

  private boolean findEntry(ExamplesSymbol node, boolean found) {
    if (!node.getExamples().isEmpty() || found) {
      return true;
    }
    for (String s : node.getSuperFdls()) {
      found = findEntry((ExamplesSymbol) task.getCacheReference().getFdlnodes().get(s).getSymbol(),
          found);
      if (found) {
        break;
      }
    }
    return found;
  }

  @Override
  protected void fillTitle() {
    title = "Beispiele in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format("Selektieren Sie alle Begriffe bei denen es sich um '%s' handelt.",
        ((ExamplesSymbol) fdlNode.getSymbol()).getObject()));
  }

  @Override
  protected void fillCorrectSet() throws UnfulfillableException {
    // Add own
    for (Example e : symbol.getExamples()) {
      correctSet.add(e.getName());
      ownCorrect.add(e.getName());
    }

    // Add all examples from sub FDLs
    List<String> subFdlChain = symbol.getSuperFdls();
    for (String sub : subFdlChain) {
      ExamplesSymbol subsym = (ExamplesSymbol) task.getCacheReference().getFdlnodes().get(sub)
          .getSymbol();
      for (Example e : subsym.getExamples()) {
        correctSet.add(e.getName());
      }
      for (String subsub : subsym.getSuperFdls()) {
        if (!subFdlChain.contains(subsub)) {
          subFdlChain.add(subsub);
        }
      }
    }
  }

  @Override
  protected void fillWrongSet() throws UnfulfillableException {
    // Add explicit falses
    wrongSet.addAll(symbol.getExplicitFalse());

    // Add Opp Sub*
    wrongSet.addAll(distHelper(new ArrayList<>(), symbol, false));
  }

  // Let's assume there are no subset loops
  private List<String> distHelper(List<String> found, ExamplesSymbol node, boolean inOpposite) {
    if (inOpposite) {
      for (Example e : node.getExamples()) {
        found.add(e.getName());
      }
      for (String sub : node.getSuperFdls()) {
        distHelper(found,
            (ExamplesSymbol) task.getCacheReference().getFdlnodes().get(sub).getSymbol(), true);
      }
    } else {
      for (String opp : node.getOpposingFdls()) {
        distHelper(found,
            (ExamplesSymbol) task.getCacheReference().getFdlnodes().get(opp).getSymbol(), true);
      }
    }
    return found;
  }

  @Override
  protected void fillCorrectOptions() {
    int difficulty = getDifficultyOrDefault();
    int numCorrect = min(max(difficulty / 3, 1), correctSet.size());
    //Set-iterators returning a random order is a lie!
    List<String> correctAll = new ArrayList<>(correctSet);
    Collections.shuffle(correctAll, task.getRandomInstance());
    List<String> correctOwn = new ArrayList<>(ownCorrect);
    Collections.shuffle(correctOwn, task.getRandomInstance());

    // Add 1 possible outside collected element
    Iterator<String> iterAll = correctAll.iterator();
    if (iterAll.hasNext()) {
      correctOptions.add(iterAll.next());
    }
    // Fill with own entries
    Iterator<String> iterOwn = correctOwn.iterator();
    int i = correctOptions.size();
    while (iterOwn.hasNext() && i < numCorrect) {
      String next = iterOwn.next();
      if (!correctOptions.contains(next)) {
        correctOptions.add(next);
      }
      i++;
    }
    // Fill with outsiders if still missing correct options
    while (correctOptions.size() < numCorrect && iterAll.hasNext()) {
      String next = iterAll.next();
      if (!correctOptions.contains(next)) {
        correctOptions.add(next);
      }
    }
    Collections.shuffle(correctOptions, task.getRandomInstance());
  }

  @Override
  protected void fillWrongOptions() {
    ExamplesSymbol sym = (ExamplesSymbol) fdlNode.getSymbol();
    int difficulty = getDifficultyOrDefault();
    int numDistractors = max(difficulty / 2, 2);
    List<String> wrongAll = new ArrayList<>(wrongSet);
    Collections.shuffle(wrongAll, task.getRandomInstance());

    if (!wrongAll.isEmpty()) { // Assume wrong set is correctly wrong
      Iterator<String> iter = wrongAll.iterator();
      while (iter.hasNext() && wrongOptions.size() < numDistractors) {
        wrongOptions.add(iter.next());
      }
    }
    if (wrongOptions.size() < numDistractors) {
      int startDistance = difficulty < 5 ? 6 : 2;
      List<String> distractors = new ArrayList<>();
      String[] specificDistractors = TemplateHelper.getAdditionalFdls(task.getParameters());
      if (specificDistractors != null) {
        for (String fdl : specificDistractors) {
          FdlNode node = task.getCacheReference().getFdlnodes().get(fdl);
          if (node.getSymbol() instanceof ExamplesSymbol) {
            for (Example e : ((ExamplesSymbol) node.getSymbol()).getExamples()) {
              distractors.add(e.getName());
            }
          }
        }
      } else {
        List<FdlNode> distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
            fdlNode.getFdlType(), startDistance, 5, new ArrayList<>(),
            sym.getCategories().isEmpty() ? null : s -> s instanceof ExamplesSymbol &&
                ((ExamplesSymbol) s).getCategories().stream()
                    .anyMatch(o -> sym.getCategories().contains(o)));

        if (distractorNodes.isEmpty()) {
          distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
              fdlNode.getFdlType(), 5, 10, new ArrayList<>(),
              sym.getCategories().isEmpty() ? null : s -> s instanceof ExamplesSymbol &&
                  ((ExamplesSymbol) s).getCategories().stream()
                      .anyMatch(o -> sym.getCategories().contains(o)));
          if (distractorNodes.isEmpty()) {
            distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
                fdlNode.getFdlType(), 0, 5, new ArrayList<>(), null);
            if (distractorNodes.isEmpty()) {
              return;
            }
          }
        }
        distractors = new ArrayList<>();
        for (FdlNode dist : distractorNodes) {
          for (Example e : ((ExamplesSymbol) dist.getSymbol()).getExamples()) {
            if (!correctSet.contains(e.getName())) {
              distractors.add(e.getName());
            }
          }
        }
      }
      Collections.shuffle(distractors, task.getRandomInstance());
      for (int i = 0; 0 < numDistractors - wrongOptions.size() && i < distractors.size(); i++) {
        String dist = distractors.get(i);
        if (!correctSet.contains(dist) && !wrongOptions.contains(dist)) {
          wrongOptions.add(dist);
        }
      }
    }
    Collections.shuffle(wrongOptions, task.getRandomInstance());
  }

}
