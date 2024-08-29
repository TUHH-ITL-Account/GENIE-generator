package generator.exercises.implementations;


import static java.lang.Math.max;
import static java.lang.Math.min;

import fdl.fdl._symboltable.ICommonFdlSymbol;
import fdl.fdl._symboltable.ProgressionSymbol;
import generator.caching.FdlNode;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SelectProgressionFromSC extends TextSingleChoiceExercise {

  private static final boolean KENNEN = true;
  private static final boolean KOENNEN = false;
  private static final boolean VERSTEHEN = false;
  private static final FdlTypes REQUIRED_TYPE = FdlTypes.PROGRESSION;
  private final Map<String, List<Integer[]>> comparingLines = new HashMap<>();
  private final List<String> distractors = new ArrayList<>();
  private final List<Integer[]> intersections = new ArrayList<>();
  private ProgressionSymbol sym;
  private List<Integer[]> trendingList;
  private List<Integer[]> coordinates;
  private Integer[] possibleExtremum;
  private boolean highPoint;
  private String answer;
  private ObservationType observationType;

  public SelectProgressionFromSC(FdlNode node, GenerationTask task) {
    super(KENNEN, KOENNEN, VERSTEHEN, node, REQUIRED_TYPE, task);
    template = "BasicSingleChoice";
    sym = (ProgressionSymbol) fdlNode.getSymbol();
  }

  public SelectProgressionFromSC() {
    super(KENNEN, KOENNEN, VERSTEHEN, null, REQUIRED_TYPE, null);
  }

  @Override
  public boolean isCompatibleWith(ICommonFdlSymbol sym) {
    return (sym instanceof ProgressionSymbol) && !(((ProgressionSymbol) sym).getSeries().isEmpty());
  }

  @Override
  public void generateAbstractExercise() throws Exception {
    setupExercise();
    fillTitle();
    fillText();
    fillCorrectOption();
    fillWrongOptions();
    calcOptions();
  }

  @Override
  protected void fillTitle() {
    title = String.format("Zusammenhang von '%s' und '%s'", sym.getyAxis(), sym.getxAxis());
  }

  @Override
  protected void fillText() {
    if (observationType == ObservationType.TREND) {
      texts.add(String.format("Welcher Trend lässt sich ab %s '%s' bei '%s' beobachten?",
          trendingList.get(0)[0], sym.getxAxis(), sym.getyAxis()));
    } else if (observationType == ObservationType.EXTREMUM) {
      texts.add(
          String.format("Was lässt sich bei '%s' an der Stelle %s '%s' beobachten?", sym.getyAxis(),
              possibleExtremum[1], sym.getxAxis()));
    } else if (observationType == ObservationType.LASTPOINT) {
      texts.add(String.format(
          "Wie groß ist der Unterschied zwischen '%s' und '%s' an der Stelle %s '%s'?",
          comparingLines.keySet().stream().toList().get(0),
          comparingLines.keySet().stream().toList().get(1),
          coordinates.get(coordinates.size() - 1)[0], sym.getxAxis()));
    } else if (observationType == ObservationType.INTERSECTION) {
      texts.add(String.format(
          "Wo liegt der Schnittpunkt, bzw. die Schnittpunkte (falls mehr als 1 Schnittpunkt vorhanden ist) zwischen den Linien '%s' und '%s'?",
          comparingLines.keySet().stream().toList().get(0),
          comparingLines.keySet().stream().toList().get(1)));
    } else {
      int random = task.getRandomInstance().nextInt(coordinates.size() - 1);
      texts.add(String.format("Welcher Trend lässt sich ab %s '%s' bei '%s' beobachten?",
          coordinates.get(random)[0], sym.getxAxis(), sym.getyAxis()));
    }
  }

  @Override
  protected void fillCorrectOption() {
    switch (observationType) {
      case TREND:
        if (answer.equals("rise")) {
          correctOption = "Es lässt sich ein steigender Trend beobachten";
        } else if (answer.equals("fall")) {
          correctOption = "Es lässt sich ein fallender Trend beobachten";
        } else {
          correctOption = String.format("'%s' bleibt Konstant.", sym.getyAxis());
        }
        break;
      case EXTREMUM:
        if (highPoint) {
          correctOption = "Es lässt sich eine Zunahme beobachten";
        } else {
          correctOption = "Es lässt sich eine Abnahme beobachten";
        }
        break;
      case LASTPOINT:
        correctOption = answer;
        break;
      case INTERSECTION:
        if (intersections.size() >= 3) {
          StringBuilder sb = new StringBuilder();
          sb.append("Die Schnittpunkte liegen zwischen");
          int counter = 0;
          for (Integer[] coor : intersections) {
            if (counter % 2 == 0) {
              sb.append(" ").append(coor[0]);
            } else {
              sb.append(" und ").append(coor[0]);
              if (counter < intersections.size() - 3) {
                sb.append(",");
              } else if (counter == intersections.size() - 3) {
                sb.append(", sowie zwischen ");
              }
            }
            counter++;
          }
          correctOption = sb.toString();
        } else if (intersections.size() == 2) {
          correctOption = String.format("Der Schnittpunkt liegt zwischen %s und %s",
              intersections.get(0)[0], intersections.get(1)[0]);
        } else {
          correctOption = "Es gibt keinen Schnittpunkt";
        }
        break;
      default:
        System.out.println(
            "SelectLimes fell into an unreachable state? ObservationType: " + observationType);
    }
  }

  @Override
  protected void fillWrongOptions() {
    int difficulty = getDifficultyOrDefault();
    int numDistractors = max((int) (difficulty / 2.5), 2);
    switch (observationType) {
      case TREND -> {
        distractors.add("Es lässt sich ein steigender Trend beobachten");
        distractors.add("Es lässt sich ein fallender Trend beobachten");
        distractors.add(String.format("'%s' bleibt Konstant.", sym.getyAxis()));
        distractors.add(String.format("'%s' ist alternierend.", sym.getyAxis()));
        distractors.remove(correctOption);
      }
      case EXTREMUM -> {
        distractors.add("Es lässt sich ein Einbruch beobachten.");
        distractors.add("Es lässt sich eine Zunahme beobachten.");
        distractors.remove(correctOption);
      }
      case LASTPOINT -> {
        float correct = Float.parseFloat(correctOption);
        int failsafe = 0;
        while (distractors.size() < numDistractors && failsafe < 100) {
          float distract = (float) (correct
              + (1 - task.getRandomInstance().nextInt(201) / 100) * (Math.max(3,
              correct * 0.3)));
          if (correct % 1 != 0) {
            correctOption = String.valueOf(Math.pow(correct, 2));
            distract = (float) Math.pow(distract, 2);
          } else {
            correctOption = String.valueOf(Math.pow(correct, 1));
            distract = (int) Math.pow(distract, 1);
          }
          if (distract != correct && !distractors.contains(
              String.valueOf(distract)) && distract > 0) {
            distractors.add(String.valueOf(distract));
          }
          failsafe++;
        }
        if (failsafe == 100) {
          System.out.println(
              "Failsafe reached after 100 iterations in SelectLimes. Correct: " + correctOption);
        }
      }
      case INTERSECTION -> {
        if (!correctOption.equals("Es gibt keinen Schnittpunkt")) {
          distractors.add("Es gibt keinen Schnittpunkt");
        }
        for (int i = 0; i < numDistractors; i++) {
          List<Integer[]> distractCoordinates = new ArrayList<>(
              comparingLines.get(comparingLines.keySet().stream().toList().get(0)));
          boolean changed = true;
          while (changed) {
            changed = false;
            for (int j = 0; j < distractCoordinates.size() - 1; j++) {
              if (distractCoordinates.get(j)[0] > distractCoordinates.get(j + 1)[0]) {
                Integer[] temp = distractCoordinates.get(j + 1);
                distractCoordinates.remove(j + 1);
                distractCoordinates.add(j, temp);
                changed = true;
              }
            }
          }
          if (intersections.size() >= 3) {
            int place;
            Map<Integer, String> distractSections = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < intersections.size() / 2; k++) {
              place = task.getRandomInstance().nextInt(distractCoordinates.size() - 1);
              String sec = distractCoordinates.get(place)[0] + " und " + distractCoordinates.get(
                  place + 1)[0];
              distractSections.putIfAbsent(distractCoordinates.get(place)[0], sec);
            }
            if (distractSections.size() >= 2) {
              sb.append("Die Schnittpunkte liegen zwischen ");
            } else {
              sb.append("Der Schnittpunkt liegt zwischen ");
            }
            List<Integer> sortedIntersections = new ArrayList<>(distractSections.keySet());
            Collections.sort(sortedIntersections);
            int counter = 0;
            for (int intersection : sortedIntersections) {
              sb.append(distractSections.get(intersection));
              if (counter < distractSections.size() - 2) {
                sb.append(", ");
              } else if (counter == distractSections.size() - 2) {
                sb.append(", sowie zwischen ");
              }
              counter++;
            }
            if (!distractors.contains(sb.toString()) && !correctOption.equals(sb.toString())) {
              distractors.add(sb.toString());
            }
          } else if (intersections.size() == 0) {
            int place;
            for (int j = 0; j < numDistractors; j++) {
              Map<Integer, String> distractSections = new HashMap<>();
              StringBuilder sb = new StringBuilder();
              sb.append("Die Schnittpunkte liegen zwischen");
              sb.append(" ");
              for (int k = 0; k < max(1, task.getRandomInstance().nextInt(3)); k++) {
                place = task.getRandomInstance().nextInt(distractCoordinates.size() - 1);
                String sec = distractCoordinates.get(place)[0] + " und " + distractCoordinates.get(
                    place + 1)[0];
                distractSections.putIfAbsent(distractCoordinates.get(place)[0], sec);
              }
              List<Integer> sortedIntersections = new ArrayList<>(distractSections.keySet());
              Collections.sort(sortedIntersections);
              int counter = 0;
              for (int intersection : sortedIntersections) {
                sb.append(distractSections.get(intersection));
                if (counter < distractSections.size() - 2) {
                  sb.append(", ");
                } else if (counter == distractSections.size() - 2) {
                  sb.append(", sowie zwischen ");
                }
                counter++;
              }
              if (!distractors.contains(sb.toString()) && !correctOption.equals(sb.toString())) {
                distractors.add(sb.toString());
              }
            }
          } else {
            for (int k = 0; k < numDistractors; k++) {
              int place = task.getRandomInstance().nextInt(distractCoordinates.size() - 1);
              String sec =
                  "Der Schnittpunkt liegt zwischen " + distractCoordinates.get(place)[0] + " und "
                      + distractCoordinates.get(place + 1)[0];
              if (!distractors.contains(sec) && !correctOption.equals(sec)) {
                distractors.add(sec);
              }
            }
          }
        }
      }
    }

    Collections.shuffle(distractors, task.getRandomInstance());

    for (int i = 0; i < min(numDistractors, distractors.size()); i++) {
      wrongOptions.add(distractors.get(i));
    }
  }

  public void setupExercise() {

    enum Tendency {
      RISE, KONST, FALL
    }

    int mode;
    Map<String, Map<Tendency, List<Integer[]>>> trendMap = new HashMap<>();

    Map<String, List<Integer[]>> series = new HashMap<>(sym.getSeries());
    List<String> keys = new ArrayList<>(series.keySet());
    Collections.shuffle(keys, task.getRandomInstance());

    if (series.size() >= 1) {
      mode = task.getRandomInstance().nextInt(2);
    } else {
      mode = 0;
    }
    if (mode == 0 || keys.size() == 1) {
      coordinates = new ArrayList<>(series.get(keys.get(0)));
      List<Float> riseList = new ArrayList<>();

      boolean changed = true;
      while (changed) {
        changed = false;
        for (int j = 0; j < coordinates.size() - 1; j++) {
          if (coordinates.get(j)[0] > coordinates.get(j + 1)[0]) {
            Integer[] temp = coordinates.get(j + 1);
            coordinates.remove(j + 1);
            coordinates.add(j, temp);
            changed = true;
          }
        }
      }
      List<Integer[]> trendList = new ArrayList<>();

      for (int i = 0; i < coordinates.size() - 1; i++) {
        float growth =
            (float) (coordinates.get(i)[1] - coordinates.get(i + 1)[1]) / (coordinates.get(i)[0]
                - coordinates.get(i + 1)[0]);
        riseList.add(growth);
      }

      Tendency tendency = Tendency.KONST;

      int trendCounter = 0;

      for (int i = 0; i < riseList.size(); i++) {
        if (riseList.get(i) >= 0.5) {
          if (tendency == Tendency.RISE) {
            trendList.add(coordinates.get(i));
          } else {
            if (trendList.size() != 0) {
              trendList.add(coordinates.get(i + 1));
              trendMap.put("trend" + trendCounter, new HashMap<>(Map.of(tendency, trendList)));
              trendList = new ArrayList<>();
              trendCounter++;
            }
            tendency = Tendency.RISE;
          }
        } else if (Math.abs(riseList.get(i)) <= 0.5) {
          if (tendency == Tendency.KONST) {
            trendList.add(coordinates.get(i));
          } else {
            if (trendList.size() != 0) {
              trendList.add(coordinates.get(i + 1));
              trendMap.put("trend" + trendCounter, new HashMap<>(Map.of(tendency, trendList)));
              trendList = new ArrayList<>();
              trendCounter++;
            }
            tendency = Tendency.KONST;
          }
        } else {
          if (tendency == Tendency.FALL) {
            trendList.add(coordinates.get(i));
          } else {
            if (trendList.size() != 0) {
              trendList.add(coordinates.get(i + 1));
              trendMap.put("trend" + trendCounter, new HashMap<>(Map.of(tendency, trendList)));
              trendList = new ArrayList<>();
              trendCounter++;
            }
            tendency = Tendency.FALL;
          }
        }
        if (riseList.get(i) >= 3) {
          possibleExtremum = coordinates.get(i + 1);
          highPoint = true;
        } else if (riseList.get(i) <= -3) {
          possibleExtremum = coordinates.get(i + 1);
          highPoint = false;
        }
      }
      if (trendList.size() != 0) {
        trendList.add(coordinates.get(coordinates.size() - 1));
        trendMap.put("trend" + trendCounter, new HashMap<>(Map.of(tendency, trendList)));
      }

      List<String> trendKey = new ArrayList<>(trendMap.keySet());

      if (!trendMap.isEmpty() &&
          trendMap.get(trendKey.get(trendKey.size() - 1)).values().stream().toList().get(0).size()
              >= coordinates.size() * 0.3) {
        trendingList = new ArrayList<>(
            trendMap.get(trendKey.get(trendKey.size() - 1)).values().stream().toList().get(0));
        observationType = ObservationType.TREND;
        answer = tendency.toString();
      } else if (possibleExtremum != null) {
        observationType = ObservationType.EXTREMUM;
      } else {
        observationType = ObservationType.TREND;
        answer = "Keine Aussage möglich";
        trendingList = new ArrayList<>();
        trendingList.add(
            coordinates.get(Math.max(0, task.getRandomInstance().nextInt(coordinates.size() - 3))));
      }

    } else {
      int randomCompare = task.getRandomInstance().nextInt(2);
      if (randomCompare == 0) {
        outside:
        for (int i = 0; i < keys.size(); i++) {
          List<Integer[]> coordinates1 = new ArrayList<>(series.get(keys.get(i)));

          boolean changed = true;
          while (changed) {
            changed = false;
            for (int k = 0; k < coordinates1.size() - 1; k++) {
              if (coordinates1.get(k)[0] > coordinates1.get(k + 1)[0]) {
                Integer[] temp = coordinates1.get(k + 1);
                coordinates1.remove(k + 1);
                coordinates1.add(k, temp);
                changed = true;
              }
            }
          }

          for (int j = i + 1; j < keys.size(); j++) {
            List<Integer[]> coordinates2 = new ArrayList<>(series.get(keys.get(j)));

            changed = true;
            while (changed) {
              changed = false;
              for (int k = 0; k < coordinates2.size() - 1; k++) {
                if (coordinates2.get(k)[0] > coordinates2.get(k + 1)[0]) {
                  Integer[] temp = coordinates2.get(k + 1);
                  coordinates2.remove(k + 1);
                  coordinates2.add(k, temp);
                  changed = true;
                }
              }
            }

            if ((Objects.equals(coordinates1.get(coordinates1.size() - 1)[0], coordinates2.get(
                coordinates2.size() - 1)[0])) && (
                coordinates1.get(coordinates1.size() - 1)[1] < coordinates2.get(
                    coordinates2.size() - 1)[1]
                    || coordinates1.get(coordinates1.size() - 1)[1] > coordinates2.get(
                    coordinates2.size() - 1)[1])) {
              observationType = ObservationType.LASTPOINT;
              answer = String.valueOf(Math.abs(
                  coordinates1.get(coordinates1.size() - 1)[1] - coordinates2.get(
                      coordinates2.size() - 1)[1]));
              comparingLines.put(keys.get(i), coordinates1);
              comparingLines.put(keys.get(j), coordinates2);
              coordinates = new ArrayList<>(coordinates1);
              break outside;
            }
          }
        }
      } else {
        List<Integer[]> coordinates1 = new ArrayList<>(series.get(keys.get(0)));
        List<Integer[]> coordinates2 = new ArrayList<>(series.get(keys.get(1)));
        boolean changed = true;
        while (changed) {
          changed = false;
          for (int k = 0; k < coordinates1.size() - 1; k++) {
            if (coordinates1.get(k)[0] > coordinates1.get(k + 1)[0]) {
              Integer[] temp = coordinates1.get(k + 1);
              coordinates1.remove(k + 1);
              coordinates1.add(k, temp);
              changed = true;
            }
          }
        }

        changed = true;
        while (changed) {
          changed = false;
          for (int k = 0; k < coordinates2.size() - 1; k++) {
            if (coordinates2.get(k)[0] > coordinates2.get(k + 1)[0]) {
              Integer[] temp = coordinates2.get(k + 1);
              coordinates2.remove(k + 1);
              coordinates2.add(k, temp);
              changed = true;
            }
          }
        }
        float distance = coordinates1.get(0)[1] - coordinates2.get(0)[1];
        for (int j = 1; j < min(coordinates1.size(), coordinates2.size()); j++) {
          if (distance >= 0) {
            if (coordinates1.get(j)[1] - coordinates2.get(j)[1] < 0) {
              intersections.add(coordinates1.get(j - 1));
              intersections.add(coordinates1.get(j));
            }
          } else {
            if (coordinates1.get(j)[1] - coordinates2.get(j)[1] > 0) {
              intersections.add(coordinates1.get(j - 1));
              intersections.add(coordinates1.get(j));
            }
          }
          distance = coordinates1.get(j)[1] - coordinates2.get(j)[1];
        }
        observationType = ObservationType.INTERSECTION;
        comparingLines.put(keys.get(0), coordinates1);
        comparingLines.put(keys.get(1), coordinates2);
      }
    }
  }

  private enum ObservationType {
    TREND, EXTREMUM, LASTPOINT, INTERSECTION
  }
}
