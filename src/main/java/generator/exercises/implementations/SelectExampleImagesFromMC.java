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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectExampleImagesFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.EXAMPLES;
  private String imageSourceDir = "";

  public SelectExampleImagesFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "BasicImageMultipleChoice";
  }

  public SelectExampleImagesFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof ExamplesSymbol) && ((ExamplesSymbol) sym).hasExamplesWithImages();
  }

  @Override
  public void generateAbstractExercise() throws UnfulfillableException {
    setImageSourceDir();
    fillTitle();
    fillText();
    fillCorrectOptions();
    fillWrongOptions();
    calcOptions();
    setUsesResources();
  }

  @Override
  protected void fillTitle() {
    title = "Beispiele in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format("Selektieren Sie alle Bilder bei denen es sich um '%s' handelt.",
        ((ExamplesSymbol) fdlNode.getSymbol()).getObject()));
  }

  @Override
  protected void fillCorrectSet() throws UnfulfillableException {

  }

  @Override
  protected void fillWrongSet() throws UnfulfillableException {

  }

  @Override
  protected void fillCorrectOptions() {
    int difficulty = getDifficultyOrDefault();
    int numCorrect = min(max(difficulty / 3, 1),
        ((ExamplesSymbol) fdlNode.getSymbol()).getExamples().size());
    List<Example> correct = new ArrayList<>(
        ((ExamplesSymbol) fdlNode.getSymbol()).getExamples().stream().filter(Example::hasImages)
            .toList());
    List<String> correctBackup = new ArrayList<>();
    Collections.shuffle(correct, task.getRandomInstance());
    if (correct.size() < numCorrect) {
      //do smth
    }
    for (int i = 0; i < numCorrect && i < correct.size(); i++) {
      int choice = task.getRandomInstance().nextInt(correct.get(i).getImages().size());
      if (correctOptions.contains(correct.get(i).getImages().get(choice))) {
        numCorrect--;
        continue;
      }
      correctOptions.add(correct.get(i).getImages().get(choice));
      for (int j = 0; j < correct.get(i).getImages().size(); j++) {
        if (j == choice) {
          continue;
        }
        correctBackup.add(correct.get(i).getImages().get(j));
      }
    }
    if (numCorrect < correctOptions.size()) {
      Collections.shuffle(correctBackup, task.getRandomInstance());
      for (int i = 0; i < numCorrect - correctOptions.size(); i++) {
        if (!correctOptions.contains(correctBackup.get(i))) {
          correctOptions.add(correctBackup.get(i));
        }
      }
    }
  }

  @Override
  protected void fillWrongOptions() throws UnfulfillableException {
    int difficulty = getDifficultyOrDefault();
    int startDistance = difficulty < 5 ? 6 : 2;
    int numDistractors = max(difficulty / 2, 2);
    List<String> correct = new ArrayList<>();
    for (Example ex : ((ExamplesSymbol) fdlNode.getSymbol()).getExamples()) {
      correct.addAll(ex.getImages());
    }

    List<Example> distractors = new ArrayList<>();
    String[] specificDistractors = TemplateHelper.getAdditionalFdls(task.getParameters());
    if (specificDistractors != null) {
      for (String fdl : specificDistractors) {
        FdlNode node = task.getCacheReference().getFdlnodes().get(fdl);
        if (node.getSymbol() instanceof ExamplesSymbol) {
          distractors.addAll(((ExamplesSymbol) node.getSymbol()).getExamples());
        }
      }
    } else {
      FdlNode distractorNode = KnowledgeModelHelper.findOtherFdlTo(fdlNode,
          fdlNode.getFdlType(), startDistance,
          s -> s instanceof ExamplesSymbol && ((ExamplesSymbol) s).hasExamplesWithImages(),
          task.getRandomInstance());
      if (distractorNode == null) {
        throw new UnfulfillableException(
            "Unable to find a distractor source (Example FDL with images).");
      }
      distractors = new ArrayList<>(
          ((ExamplesSymbol) distractorNode.getSymbol()).getExamples());
    }
    List<String> duplicateReserve = new ArrayList<>();
    Collections.shuffle(distractors, task.getRandomInstance());
    for (int i = 0; i < distractors.size() && wrongOptions.size() < numDistractors; i++) {
      int numImages = distractors.get(i).getImages().size();
      if (numImages == 1) {
        if (!correct.contains(distractors.get(i).getImages().get(0))) {
          wrongOptions.add(distractors.get(i).getImages().get(0));
        }
      } else if (numImages > 1) {
        boolean added = false;
        List<String> shuffle = new ArrayList<>(distractors.get(i).getImages());
        Collections.shuffle(shuffle, task.getRandomInstance());
        for (String s : shuffle) {
          if (!correct.contains(s)) {
            if (!added) {
              wrongOptions.add(s);
              added = true;
            } else {
              duplicateReserve.add(s);
            }
          }
        }
      }
    }
    if (wrongOptions.size() < numDistractors && !duplicateReserve.isEmpty()) {
      List<String> shuffle = new ArrayList<>(duplicateReserve);
      Collections.shuffle(shuffle, task.getRandomInstance());
      for (int i = 0; i < shuffle.size() && wrongOptions.size() < numDistractors; i++) {
        if (!correct.contains(shuffle.get(i)) && !wrongOptions.contains(shuffle.get(i))) {
          wrongOptions.add(shuffle.get(i));
        }
      }
    }
  }

  public void calcOptions() {
    this.options = new ArrayList<>(this.correctOptions);
    this.options.addAll(new ArrayList<>(this.wrongOptions));
    Collections.shuffle(this.options); //todo: überlegen ob seed abhängigkeit nötig
  }

  protected void setImageSourceDir() {
    imageSourceDir = "genie/";
  }

  @SuppressWarnings("unchecked")
  protected void setUsesResources() {
    task.getParameters().putIfAbsent("usesResources", new HashSet<String>());
    ((Set<String>) task.getParameters().get("usesResources")).addAll(options);
  }

  public String getImageSourceDir() {
    return imageSourceDir;
  }
}
