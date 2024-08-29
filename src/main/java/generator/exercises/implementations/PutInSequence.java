package generator.exercises.implementations;

import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl._symboltable.SequenceSymbol;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.OrderingExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class PutInSequence extends OrderingExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.SEQUENCE;

  private Map<String, String> solMap;

  public PutInSequence(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    this.template = "PutInSequence";
  }

  public PutInSequence() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof SequenceSymbol) && !(((SequenceSymbol) sym).getSequence().isEmpty());
  }

  @Override
  public void generateAbstractExercise() {
    fillTitle();
    fillText();
    fillOrder();
    fillRandomizedOrder();
    fillGivenIndexes();
  }

  @Override
  protected void fillTitle() {
    title = "Sequenzen in '" + fdlNode.getTopicNode().getNodeName() + "'";
  }

  @Override
  protected void fillText() {
    texts.add(String.format(
        "Geben Sie die richtige Reihenfolge für '%s' an, indem Sie die Nummern der einzelnen Schritte in die Kästchen eintragen.",
        ((SequenceSymbol) fdlNode.getSymbol()).getTerm()));
  }

  @Override
  protected void fillOrder() {
    order = new ArrayList<>(((SequenceSymbol) fdlNode.getSymbol()).getSequence());
    for (int i = 0; i < order.size(); i++) {
      if (multiOccurrences.containsKey(String.valueOf(i))) {
        continue;
      }
      String current = order.get(i);
      List<Integer> sames = new ArrayList<>();
      for (int j = i + 1; j < order.size(); j++) {
        if (order.get(j).equals(current)) {
          if (sames.isEmpty()) {
            sames.add(i);
          }
          sames.add(j);
        }
      }
      for (int k : sames) {
        multiOccurrences.put(String.valueOf(k), sames);
      }
    }
  }

  @Override
  protected void fillRandomizedOrder() {
    randomizedOrder = new ArrayList<>(IntStream.range(0, order.size()).boxed().toList());
    Collections.shuffle(randomizedOrder, task.getRandomInstance());

    solMap = new LinkedHashMap<>();
    for (int i = 0; i < order.size(); i++) {
      solMap.put(String.valueOf(i), order.get(i));
    }
  }

  protected void fillGivenIndexes() {
    int difficulty = getDifficultyOrDefault();

    int numNotGiven = Math.max((int) (difficulty * 0.1d * order.size()), 2);
    int numGiven = order.size() - numNotGiven;

    List<Integer> range = new ArrayList<>(IntStream.range(0, order.size()).boxed().toList());
    Collections.shuffle(range, task.getRandomInstance());
    if (numGiven > 0) {
      givenIndexes = range.subList(0, numGiven);
    }
  }

  public Map<String, String> getSolMap() {
    return solMap;
  }
}

