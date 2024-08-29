package generator.exercises.implementations;

import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl._symboltable.TradeOffSymbol;
import generator.caching.FdlNode;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import generator.util.KnowledgeModelHelper;
import generator.util.TemplateHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This exercise type asks the student to select examples of a certain topic from a list of possible
 * answers
 */
public class SelectVantageFromMC extends TextMultipleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.TRADE_OFF;

  private boolean isAdvantage;
  private List<String> targetList;

  public SelectVantageFromMC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "BasicMultipleChoice";
  }

  public SelectVantageFromMC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof TradeOffSymbol) && !(((TradeOffSymbol) sym).getAdvantages().isEmpty()
        && ((TradeOffSymbol) sym).getDisadvantages().isEmpty());
  }

  @Override
  public void generateAbstractExercise() {
    setup();
    fillTitle();
    fillText();
    fillCorrectOptions();
    fillWrongOptions();
    calcOptions();
  }

  protected void setup() {
    TradeOffSymbol sym = (TradeOffSymbol) fdlNode.getSymbol();
    if (sym.getAdvantages().isEmpty()) {
      isAdvantage = false;
    } else if (sym.getDisadvantages().isEmpty()) {
      isAdvantage = true;
    } else {
      isAdvantage = task.getRandomInstance().nextBoolean();
    }
    targetList = isAdvantage ? new ArrayList<>(sym.getAdvantages())
        : new ArrayList<>(sym.getDisadvantages());
  }

  @Override
  protected void fillTitle() {
    title = "Vor-/Nachteile in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format(
        "Selektieren Sie alle Optionen bei denen es sich um einen expliziten " + (isAdvantage
            ? "Vorteil" : "Nachteil") + " von '%s' handelt.",
        ((TradeOffSymbol) fdlNode.getSymbol()).getObject()));
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
    int numCorrect = max(max(difficulty / 3, 1), targetList.size());

    Collections.shuffle(targetList, task.getRandomInstance());

    for (int i = 0; i < numCorrect && i < targetList.size(); i++) {
      correctOptions.add(targetList.get(i));
    }
  }

  // todo: cover case if distractor is actually correct
  @Override
  protected void fillWrongOptions() {
    int difficulty = getDifficultyOrDefault();
    List<String> correct = new ArrayList<>(isAdvantage ?
        ((TradeOffSymbol) fdlNode.getSymbol()).getAdvantages()
        : ((TradeOffSymbol) fdlNode.getSymbol()).getDisadvantages());

    // use opposites from same FDL
    if (difficulty <= 2) {
      TradeOffSymbol sym = (TradeOffSymbol) fdlNode.getSymbol();
      List<String> distSource = isAdvantage ? new ArrayList<>(sym.getDisadvantages())
          : new ArrayList<>(sym.getAdvantages());
      int numDistractors = min(2, distSource.size());
      Collections.shuffle(distSource, task.getRandomInstance());
      for (int i = 0; i < numDistractors; i++) {
        wrongOptions.add(distSource.get(i));
      }
    } else {
      List<String> distractors;
      String[] specificDistractors = TemplateHelper.getAdditionalFdls(task.getParameters());
      if (specificDistractors != null) {
        distractors = new ArrayList<>();
        for (String fdl : specificDistractors) {
          FdlNode node = task.getCacheReference().getFdlnodes().get(fdl);
          if (node.getSymbol() instanceof TradeOffSymbol) {
            if (isAdvantage) {
              distractors.addAll(((TradeOffSymbol) node.getSymbol()).getAdvantages());
            } else {
              distractors.addAll(((TradeOffSymbol) node.getSymbol()).getDisadvantages());
            }
          }
        }
      } else {
        int startDistance = difficulty < 5 ? 4 : 2;
        FdlNode distractorNode = KnowledgeModelHelper.findOtherFdlTo(fdlNode, FdlTypes.TRADE_OFF,
            startDistance, (s -> isAdvantage ? !((TradeOffSymbol) s).getAdvantages().isEmpty()
                : !((TradeOffSymbol) s).getDisadvantages().isEmpty()), task.getRandomInstance());
        if (distractorNode == null) {
          return;
          //throw smth
        }
        TradeOffSymbol distSym = (TradeOffSymbol) distractorNode.getSymbol();
        distractors = new ArrayList<>(
            isAdvantage ? distSym.getAdvantages() : distSym.getDisadvantages());
      }
      int numDistractors = min(max(difficulty / 2, 2), distractors.size());
      Collections.shuffle(distractors, task.getRandomInstance());
      for (int i = 0; i < numDistractors && i < distractors.size(); i++) {
        if (!correct.contains(distractors.get(i))) {
          wrongOptions.add(distractors.get(i));
        }
      }
    }
  }

}
