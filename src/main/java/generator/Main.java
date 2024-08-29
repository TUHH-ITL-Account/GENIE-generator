package generator;

import fdl.exceptions.DslException;
import fdl.fdl.FdlTool;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.caching.TopicNode;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.AbstractExercise;
import generator.exercises.inputs.classes.AssigningExercise;
import generator.exercises.inputs.classes.CalculationExercise;
import generator.exercises.inputs.classes.OrderingExercise;
import generator.exercises.inputs.classes.TextAnswersExercise;
import generator.exercises.inputs.classes.TextCompletionExercise;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import generator.types.GenerationTask;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;

public class Main {

  public static Map<String, Class<?>> templateClasses = new HashMap<>();
  public static Map<Class<?>, String> classMap = new HashMap<>();
  public static Set<Class<?>> exerciseTypes = new HashSet<>();
  public static boolean changeParameters;

  private static Generator generator;

  public static void main(String[] args)
      throws ModelException, UnfulfillableException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, URISyntaxException, ParseException {

    setupGenerator();
    runCLI();
  }

  private static void setupGenerator() {
    generator = new Generator();
    classMap.put(TextCompletionExercise.class, "fill-out");
    classMap.put(TextAnswersExercise.class, "text-answers");
    classMap.put(TextSingleChoiceExercise.class, "single-choice");
    classMap.put(TextMultipleChoiceExercise.class, "multiple-choice");
    classMap.put(OrderingExercise.class, "ordering");
    classMap.put(AssigningExercise.class, "assigning");
    classMap.put(CalculationExercise.class, "calculation");
  }

  public static void runCLI()
      throws UnfulfillableException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException, DslException {

    String modelDirectory = "knowledgemodels";
    String currentModel = "";
    String input;
    int fileCounter = 1;
    Scanner sc = new Scanner(System.console().reader());

    Reflections reflections = new Reflections("generator.exercises.implementations");
    exerciseTypes = reflections.getSubTypesOf(AbstractExercise.class).stream()
        .filter(c -> !Modifier.isAbstract(c.getModifiers())).collect(
            Collectors.toSet());

    for (Class<?> tempClass : exerciseTypes) {
      for (Class<?> intrface : classMap.keySet()) {
        if (intrface.isAssignableFrom(tempClass)) {
          templateClasses.put(classMap.get(intrface), tempClass);
        }
      }
    }

    Map<String, Map<String, String>> commandMap = new LinkedHashMap<>();
    Map<String, String> topicMap = new LinkedHashMap<>();

    commandMap.put("help", Map.of("help", "List all possible commands."));
    commandMap.put("exit", Map.of("exit", "Exits the application."));
    commandMap.put("try caching", Map.of("try caching",
        "Executes caching of a knowledge model, but does **not** keep it in cache."));
    commandMap.put("parse fdls",
        Map.of("parse fdls", "Tries parsing all FDLs in a given directory."));
    commandMap.put("parse fdl", Map.of("parse fdl", "Tries parsing a single FDL."));
    commandMap.put("check all templates for fdls",
        Map.of("check all templates for fdls", "Tries processing all templates for a FDL."));
    commandMap.put("generate from topic", Map.of("generate from topic [-p]",
        "Generate an exercise from any FDL of a specified topic. Add `-p` to change parameters."));
    commandMap.put("generate from fdl",
        Map.of("generate from fdl [-p]", "Generate A Task From A Specific FDL"));
    commandMap.put("generate from course", Map.of("generate from course [-p]",
        "Generate an exercise from any FDL in any topic of a specified course. Add `-p` to change parameters."));
    commandMap.put("generate with template", Map.of("generate with template [-p]",
        "Generate an exercise from a specific FDL using a specified template. Add `-p` to change parameters"));
    commandMap.put("list templates", Map.of("list templates", "List all possible task types."));
    commandMap.put("use model", Map.of("use model", "Change the model"));
    commandMap.put("print topics", Map.of("print topics", "Print all topics"));

    System.out.println("Welcome to the GENIE-Generator CLI. Type 'help' for a list of commands.");
    label:
    while (true) {
      try {
        System.out.print("Command: ");
        String command = sc.nextLine();
        if (command.endsWith(" -p")) {
          command = command.substring(0, command.length() - 3);
          changeParameters = true;
        }
        if (commandMap.containsKey(command)) {
          switch (command) {
            case "exit":
              System.out.println("Application shuts down");
              break label;
            case "help":
              System.out.println("| Command | Description |\n|---------|-------------|");
              for (String key : commandMap.keySet()) {
                System.out.println(
                    "| " + commandMap.get(key).keySet().stream().toList().get(0) + " | "
                        + commandMap.get(key).values().stream().toList().get(0) + " |");
              }
              break;
            case "try caching": {
              System.out.println("Enter the model repository: ");
              String modelrepo = sc.nextLine();
              System.out.println("Enter the name of the model");
              String model = sc.nextLine();
              System.out.println(
                  "Try caching with model repository: " + modelrepo + " and model: " + model);
              tryCaching(modelrepo, model);
              System.out.println("Model was tested successfully.");
              break;
            }
            case "parse fdls":
              System.out.println("Enter a directory: ");
              String directory = sc.nextLine();
              System.out.println("Parse fdls in: " + directory);
              parseFdls(directory);
              break;
            case "parse fdl":
              System.out.println("Enter a filename: ");
              String file = sc.nextLine();
              System.out.println("Parse fdl in: " + file);
              parseFdl(file);
              break;
            case "check all templates for fdls": {
              System.out.println("Enter the model repository: ");
              String modelrepo = sc.nextLine();
              System.out.println("Enter the name of the model");
              String model = sc.nextLine();
              System.out.println(
                  "Check all templates with model repository: " + modelrepo + " and model: "
                      + model);
              checkAllTemplatesForFdls(modelrepo, model);
              break;
            }
            case "list templates":
              for (String name : templateClasses.keySet()) {
                System.out.println(name);
              }
              break;
            case "use model":
              while (true) {
                System.out.println(
                    "Enter the model repository (leave empty to use knowledgemodels): ");
                String repoInput = sc.nextLine();
                String modelRepo = repoInput.isEmpty() ? modelDirectory : repoInput;
                System.out.println("Enter the name of the model");
                String selectedModel = sc.nextLine();
                if (generator.getCacheMap().containsKey(selectedModel)) {
                  currentModel = selectedModel;
                  break;
                } else if (currentModel.equals("help")) {
                  System.out.println("Possible Inputs: " + generator.getCacheMap().keySet());
                } else if (currentModel.equals("exit")) {
                  break;
                } else {
                  generator.cacheModel(modelRepo, selectedModel, true);
                  currentModel = selectedModel;
                  System.out.println("New model was cached.");
                  break;
                }
              }
              break;
            case "print topics":
              if (currentModel.equals("")) {
                System.out.println(
                    "No model is selected. Please select a model with comment 'use model'");
              } else {
                System.out.println("Topic tree: ");
                printTree(generator.getCacheMap().get(currentModel).getModelRoot(), 0);
              }
              break;
            default:
              if (currentModel.equals("")) {
                System.out.println(
                    "No model is selected. Please select a model with comment 'use model'");
              } else {
                GenerationTask task = new GenerationTask(currentModel);
                task.initMissingWithDefaults();
                task.getParameters().remove("topic_actual");
                task.getParameters().put("kennen", true);
                task.getParameters().put("koennen", false);
                task.getParameters().put("verstehen", false);
                if (changeParameters) {
                  int difficulty;
                  System.out.println("Enter a difficulty");
                  difficulty = sc.nextInt();
                  task.getParameters().put("difficulty", difficulty);
                  while (true) {
                    System.out.println("Enter a KKV dimension (kennen/koennen/verstehen): ");
                    input = sc.nextLine();
                    if (input.equals("kennen")) {
                      task.getParameters().put(input, true);
                      task.getParameters().put("koennen", false);
                      task.getParameters().put("verstehen", false);
                      break;
                    } else if (input.equals("koennen")) {
                      task.getParameters().put(input, true);
                      task.getParameters().put("kennen", false);
                      task.getParameters().put("verstehen", false);
                      break;
                    } else if (input.equals("verstehen")) {
                      task.getParameters().put(input, true);
                      task.getParameters().put("kennen", false);
                      task.getParameters().put("koennen", false);
                      break;
                    } else {
                      System.out.println("Invalid input");
                    }
                  }
                }
                fillMap(generator.getCacheMap().get(currentModel).getModelRoot(), topicMap);
                switch (command) {
                  case "generate from topic" -> {
                    String userTopic = null;
                    while (true) {
                      System.out.println("Enter a topic: ");
                      input = sc.nextLine();
                      if (topicMap.containsValue(input)) {
                        userTopic = input;
                        break;
                      } else if (input.equals("help")) {
                        System.out.println("Possible inputs: " + topicMap.values());
                      } else if (input.equals("exit")) {
                        break;
                      } else {
                        System.out.println("Invalid input: " + input);
                      }
                    }
                    if (userTopic != null) {
                      task = generator.generateFromTopic(currentModel, userTopic, task);
                    }
                  }
                  case "generate from fdl" -> {
                    List<String> fdlnames = new ArrayList<>();
                    String userTopic = null;
                    while (true) {
                      System.out.println("Enter a topic: ");
                      input = sc.nextLine();
                      if (topicMap.containsValue(input)) {
                        userTopic = input;
                        break;
                      } else if (input.equals("help")) {
                        System.out.println("Possible inputs: " + topicMap.values());
                      } else if (input.equals("exit")) {
                        break;
                      } else {
                        System.out.println("Invalid input: " + input);
                      }
                    }
                    if (userTopic != null) {
                      for (FdlNode node : generator.getCacheMap().get(currentModel).getTopicnodes()
                          .get(userTopic).getFdls()) {
                        fdlnames.add(node.getFdlName());
                      }
                      String userfdlName = null;
                      while (true) {
                        System.out.println("Enter the name of the fdl: ");
                        input = sc.nextLine();
                        if (fdlnames.contains(input)) {
                          userfdlName = input;
                          break;
                        } else if (input.equals("help")) {
                          System.out.println("Possible inputs: " + fdlnames);
                        } else if (input.equals("exit")) {
                          break;
                        } else {
                          System.out.println("Invalid input: " + input);
                        }
                      }
                      if (userfdlName != null) {
                        task = generator.generateFromFdl(currentModel, userTopic, userfdlName,
                            task);
                      }
                    }
                  }
                  case "generate from course" -> task = generator.generateFromCourse(currentModel,
                      task);
                  case "generate with template" -> {
                    List<String> fdlnames = new ArrayList<>();
                    for (FdlNode node : generator.getCacheMap().get(currentModel).getFdlnodes()
                        .values()) {
                      fdlnames.add(node.getFdlName());
                    }
                    List<String> classNames = new ArrayList<>();
                    String userFdl = null;
                    while (true) {
                      System.out.println("Enter the name of the fdl");
                      input = sc.nextLine();
                      if (fdlnames.contains(input)) {
                        userFdl = input;
                        break;
                      } else if (input.equals("help")) {
                        System.out.println("Possible inputs: " + fdlnames);
                      } else if (input.equals("exit")) {
                        break;
                      } else {
                        System.out.println("Invalid input: " + input);
                      }
                    }
                    FdlNode node = generator.getCacheMap().get(currentModel).getFdlnodes()
                        .get(userFdl);
                    for (Class<?> clazz : exerciseTypes) {
                      AbstractExercise exObj = (AbstractExercise) clazz.getConstructor()
                          .newInstance();
                      FdlTypes type = exObj.getRequiredFdlType();
                      if (type == node.getFdlType() && !classNames.contains(
                          classMap.get(clazz.getSuperclass()))) {
                        classNames.add(classMap.get(clazz.getSuperclass()));
                      }
                    }
                    String userTaskType = null;
                    while (true) {
                      System.out.println("Enter the name of the task type");
                      input = sc.nextLine();
                      if (classNames.contains(input)) {
                        userTaskType = input;
                        break;
                      } else if (input.equals("help")) {
                        System.out.println("Possible inputs: " + classNames);
                      } else if (input.equals("exit")) {
                        break;
                      } else {
                        System.out.println("Invalid input: " + input);
                      }
                    }

                    if (userFdl != null && userTaskType != null) {
                      task = generator.generateFromFdlWithClass(
                          generator.getCacheMap().get(currentModel).getFdlnodes().get(userFdl),
                          templateClasses.get(userTaskType), task);
                    }
                  }
                }
                String htmlExercise = task.getPartHtmlExercise();
                String htmlSolution = task.getPartHtmlSolution();
                String filename = fileCounter + "_" + task.getTopicName();
                // todo: ordentliche file checks
                if (htmlExercise != null) {
                  writeIntoFile(filename + "_exercise", htmlExercise);
                  writeIntoFile(filename + "_solution", htmlSolution);
                  fileCounter++;
                  Desktop desktop = Desktop.getDesktop();
                  URI uri;
                  uri = new File("target/" + filename + "_exercise.html").toURI();
                  desktop.browse(uri);
                } else {
                  System.out.println("Task could not be created");
                }
              }
              break;
          }
        } else {
          System.out.println("Invalid input: " + command);
        }
      } catch (Exception e) {
        System.out.println("An error occurred:");
        System.out.println(e.toString());
        System.out.println(Arrays.toString(e.getStackTrace()));
        System.out.println(e.getMessage());
        System.out.println("---");
      }
    }
  }

  public static void tryCaching(String modelrepo, String model)
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    File dir = new File(modelrepo);
    assert dir.exists() && dir.isDirectory() && dir.canRead();
    File modeldir = new File(dir, model);
    assert modeldir.exists() && modeldir.isDirectory() && modeldir.canRead();
    new Cache(modelrepo, model, true);
  }

  public static void parseFdls(String directory) {
    FdlTool cli = new FdlTool();
    cli.parseFdlsInDir(directory);
  }

  public static void parseFdl(String file) throws DslException {
    FdlTool cli = new FdlTool();
    cli.fullParse(file);
  }

  public static void checkAllTemplatesForFdls(String modelrepo, String model)
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    File dir = new File(modelrepo);
    assert dir.exists() && dir.isDirectory() && dir.canRead();
    File modeldir = new File(dir, model);
    assert modeldir.exists() && modeldir.isDirectory() && modeldir.canRead();
    Cache c = new Cache(modelrepo, model, false);
    Generator gen = new Generator();
    gen.cacheModel(modelrepo, model, true);
    Map<String, Map<String, List<Class<?>>>> templateMatrix = c.getTemplateMatrix();

    List<AbstractExercise> templateInstances = new ArrayList<>();
    String[] dims = {"kennen", "koennen", "verstehen"};
    for (String s : dims) {
      for (List<Class<?>> templateList : templateMatrix.get(s).values()) {
        for (Class<?> template : templateList) {
          templateInstances.add((AbstractExercise) template.getConstructor().newInstance());
        }
      }
    }

    for (FdlNode fdl : c.getFdlnodes().values()) {
      System.out.println("Trying all templates for: " + fdl.getFdlName());
      for (AbstractExercise template : templateInstances) {
        if (fdl.getFdlType() == template.getRequiredFdlType()) {
          System.out.println("Trying template: " + template.getClass().getSimpleName());
          try {
            gen.generateFromFdlWithClass(fdl, template.getClass(), null);
          } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static void writeIntoFile(String filename, String content) {
    String targetDir = "target";
    File existCheck = new File(targetDir);
    if (!existCheck.exists()) {
      existCheck.mkdir();
    }
    try {
      File myObj = new File(targetDir + "/" + filename + ".html");
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      }
      FileWriter myWriter = new FileWriter(targetDir + "/" + filename + ".html");
      myWriter.write(content);
      myWriter.close();
      System.out.println("New task was created");
    } catch (IOException e) {
      System.out.println("An error occured.");
      e.printStackTrace();
    }
  }

  private static void printTree(TopicNode node, int level) {
    if (node.getFdls().size() != 0) {
      for (int i = 0; i < level; i++) {
        System.out.print("|__ ");
      }
      System.out.println(node.getNodeName());
      level++;
    }
    for (TopicNode child : node.getChildren()) {
      printTree(child, level);
    }
  }

  private static void fillMap(TopicNode node, Map<String, String> topicMap) {
    if (!node.getFdls().isEmpty()) {
      topicMap.put(node.getNodeName(), node.getNodeName());
    }
    for (TopicNode child : node.getChildren()) {
      fillMap(child, topicMap);
    }
  }
}

