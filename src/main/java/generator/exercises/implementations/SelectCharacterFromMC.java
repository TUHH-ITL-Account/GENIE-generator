package generator.exercises.implementations;

import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.fdl._symboltable.CharacteristicsSymbol;
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
import java.util.Map;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectCharacterFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.CHARACTERISTICS;

  private final CharacteristicsSymbol symbol;

  public SelectCharacterFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicMultipleChoice";
    symbol = (CharacteristicsSymbol) node.getSymbol();
  }

  public SelectCharacterFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
    template = "BasicMultipleChoice";
    symbol = null;
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof CharacteristicsSymbol)
        && !(((CharacteristicsSymbol) sym).getCharacteristics().isEmpty());
  }

  @Override
  protected void fillTitle() {
    title = "Charakteristika in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format("Selektieren Sie alle wahren Aussagen zu '%s'.",
        ((CharacteristicsSymbol) fdlNode.getSymbol()).getObject()));
  }

  @Override
  protected void fillCorrectSet() throws UnfulfillableException {
    // Add own
    correctSet.addAll(symbol.getCharacteristics());
    ownCorrect.addAll(symbol.getCharacteristics());

    // Add all chars from super FDLs
    List<String> subFdlChain = symbol.getSubFdls();
    for (String sub : subFdlChain) {
      ExamplesSymbol subsym = (ExamplesSymbol) task.getCacheReference().getFdlnodes().get(sub)
          .getSymbol();
      for (Example e : subsym.getExamples()) {
        correctSet.add(e.getName());
      }
      for (String subsub : subsym.getSubFdls()) {
        if (!subFdlChain.contains(subsub)) {
          subFdlChain.add(subsub);
        }
      }
    }
  }

  @Override
  protected void fillWrongSet() throws UnfulfillableException {
    for (String s : symbol.getOpposingFdls()) {
      for (String c : ((CharacteristicsSymbol) task.getCacheReference().getFdlnodes().get(s)
          .getSymbol()).getCharacteristics()) {
        if (!correctSet.contains(c)) {
          wrongSet.add(c);
        }
      }
    }
    wrongSet.addAll(symbol.getExplicitFalse());
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
      int startDistance = 0;

      List<String> distractors;
      String[] specificDistractors = TemplateHelper.getAdditionalFdls(task.getParameters());
      if (specificDistractors != null) {
        distractors = new ArrayList<>();
        for (String fdl : specificDistractors) {
          FdlNode node = task.getCacheReference().getFdlnodes().get(fdl);
          if (node.getSymbol() instanceof CharacteristicsSymbol) {
            distractors.addAll(((CharacteristicsSymbol) node.getSymbol()).getCharacteristics());
          }
        }
      } else {
      /*List<FdlNode> distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
          fdlNode.getFdlType(), startDistance, 5, new ArrayList<>(),
          sym.getCategories().isEmpty() ? null : s -> s instanceof CharacteristicsSymbol &&
              ((CharacteristicsSymbol) s).getCategories().stream()
                  .anyMatch(o -> sym.getCategories().contains(o)));

      if(distractorNodes.isEmpty()) {
        distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
            fdlNode.getFdlType(), 5, 10, new ArrayList<>(),
            sym.getCategories().isEmpty() ? null : s -> s instanceof CharacteristicsSymbol &&
                ((CharacteristicsSymbol) s).getCategories().stream()
                    .anyMatch(o -> sym.getCategories().contains(o)));
        if(distractorNodes.isEmpty()) {
          distractorNodes = KnowledgeModelHelper.findOtherFdlsTo(fdlNode,
              fdlNode.getFdlType(), 0, 5, new ArrayList<>(), null);
          if(distractorNodes.isEmpty()) {
            return;
          }
        }
      }
      distractors = new ArrayList<>();
      for(FdlNode dist : distractorNodes) {
        distractors.addAll(((CharacteristicsSymbol)dist.getSymbol()).getCharacteristics());
      }
      */
        distractors = new ArrayList<>();
        Map<Integer, List<FdlNode>> distractorSources = KnowledgeModelHelper.findOtherCategorizedFdlsTo(
            fdlNode, fdlNode.getFdlType(), startDistance, 5, null);
        if (distractorSources.size() > 0) {
          List<Integer> numMatches = new ArrayList<>(distractorSources.keySet());
          Collections.sort(numMatches);
          for (FdlNode fdl : distractorSources.get(numMatches.get(numMatches.size() - 1))) {
            distractors.addAll(((CharacteristicsSymbol) fdl.getSymbol()).getCharacteristics());
          }
          if (distractors.size() < 2 && numMatches.size() > 1) {
            for (FdlNode fdl : distractorSources.get(numMatches.get(numMatches.size() - 2))) {
              distractors.addAll(((CharacteristicsSymbol) fdl.getSymbol()).getCharacteristics());
            }
          }
        }
      }

      Collections.shuffle(distractors, task.getRandomInstance());
      for (int i = 0; i < numDistractors - wrongOptions.size() && i < distractors.size(); i++) {
        String dist = distractors.get(i);
        if (!correctSet.contains(dist) && !wrongOptions.contains(dist)) {
          wrongOptions.add(dist);
        }
      }
    }
    Collections.shuffle(wrongOptions, task.getRandomInstance());
  }

}
