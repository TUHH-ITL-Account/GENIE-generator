package generator.caching;

import fdl.exceptions.DslException;
import fdl.fdl.FdlTool;
import fdl.fdl._symboltable.ConstantNumberSymbol;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.VariableGroupsSymbol;
import fdl.types.containers.Reference;
import generator.exceptions.ModelException;
import generator.exercises.inputs.classes.AbstractExercise;
import generator.exercises.inputs.classes.AssigningExercise;
import generator.exercises.inputs.classes.CalculationExercise;
import generator.exercises.inputs.classes.FillOutExercise;
import generator.exercises.inputs.classes.MultiCalculationExercise;
import generator.exercises.inputs.classes.OrderingExercise;
import generator.exercises.inputs.classes.TextAnswersExercise;
import generator.exercises.inputs.classes.TextMultipleChoiceExercise;
import generator.exercises.inputs.classes.TextSingleChoiceExercise;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.yaml.snakeyaml.Yaml;

public class Cache {

  private final String modelName;
  private final String modelDirectory;
  //private final String fdlDirectory;
  private TopicNode modelroot;
  private Map<String, Map<String, List<Class<?>>>> templateMatrix;
  private Map<String, AbstractExercise> templateObjects;

  private Map<String, TopicNode> topicnodes;
  private Map<String, FdlNode> fdlnodes;
  private Map<String, ConstantNumberSymbol> constants;

  private FdlNode varGroupsFdlNode;

  public Cache(String directory, String model, boolean withFdlParse)
      throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, DslException, ModelException {
    File dir = new File(directory);
    if (!dir.exists()) {
      throw new IOException(String.format("Directory '%s' does not exist.", directory));
    }
    if (!dir.isDirectory()) {
      throw new IOException(String.format("'%s' is not a directory.", directory));
    }
    modelName = model;
    modelDirectory = dir.getAbsolutePath();
    File modelDir = new File(dir, model);
    assert modelDir.exists() && modelDir.isDirectory() && modelDir.canRead();
    File modelFile = new File(modelDir, model + ".yaml");
    if (!modelFile.exists()) {
      System.out.println("YAML modelfile does not have the same name as its directory.");
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        if (file.getName().endsWith(".yaml")) {
          System.out.printf("Assuming %s is the modelfile.%n", file.getName());
          modelFile = file;
          break;
        }
      }
    }
    if (!modelFile.exists()) {
      throw new IOException("Unable to find any modelfile");
    }
    Map<String, Object> yamlModel = loadYAML(modelFile);
    cacheModel(model, yamlModel, withFdlParse);
    for(FdlNode node : fdlnodes.values()) {
      if(node.getSymbol() instanceof VariableGroupsSymbol) {
        varGroupsFdlNode = node;
        break;
      }
    }
    initTemplateMatrix();
    cacheTemplateMatrix();
  }

  /*
  public Cache(String modelfile, String modelDirectory, String fdlDirectory)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, DslException {
    this.modelfile = modelfile;
    this.modelDirectory = modelDirectory.charAt(modelDirectory.length() - 1) == '/' ? modelDirectory
        : modelDirectory + "/";
    this.fdlDirectory = fdlDirectory.charAt(fdlDirectory.length() - 1) == '/' ? fdlDirectory
        : fdlDirectory + "/";
    Map<String, Object> model = loadYAML(this.modelDirectory + modelfile);
    cacheModel(model, true);
    initTemplateMatrix();
    cacheTemplateMatrix();
  }
   */

  private Map<String, Object> loadYAML(File filename) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    Yaml yaml = new Yaml();
    return yaml.load(inputStream);
  }

  private Map<String, Object> loadYAML(String filename) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File(filename));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    Yaml yaml = new Yaml();
    return yaml.load(inputStream);
  }


  private void cacheModel(String modelname, Map<String, Object> model, boolean withFdlParse)
      throws DslException, ModelException {
    topicnodes = new HashMap<>();
    Map<String, List<String>> topicnodesChildren = new HashMap<>();
    fdlnodes = new HashMap<>();
    Map<String, List<String>> fdlnodesChildren = new HashMap<>();
    Map<String, List<String>> fdlnodesPrereqs = new HashMap<>();
    constants = new HashMap<>();

    // create all topicnodes with their name and id + save lists of topic children and fdls
    if (model.containsKey("wmnodes")) {
      ArrayList<LinkedHashMap<String, LinkedHashMap<String, Object>>> wmnodes = (ArrayList<LinkedHashMap<String, LinkedHashMap<String, Object>>>) model.get(
          "wmnodes");
      for (LinkedHashMap<String, LinkedHashMap<String, Object>> node : wmnodes) {
        String nodeName = node.keySet().stream().toList().get(0);
        String nodeId = (String) node.get(nodeName).get("id");

        TopicNode tNode = new TopicNode(nodeName, nodeId, modelname);
        topicnodes.put(nodeName, tNode);
        if (node.get(nodeName).containsKey("children")) {
          topicnodesChildren.put(nodeName, (ArrayList<String>) node.get(nodeName).get("children"));
        }
        if (node.get(nodeName).containsKey("fdls")) {
          fdlnodesChildren.put(nodeName, (ArrayList<String>) node.get(nodeName).get("fdls"));
        }
        Object gSource = node.get(nodeName).get("generationSource");
        tNode.setGenerationSource(gSource != null ? (Boolean) gSource : true);
      }
    }

    // create all fdlnodes with their name
    if (model.containsKey("fdlnodes")) {
      ArrayList<LinkedHashMap<String, Object>> fdls = (ArrayList<LinkedHashMap<String, Object>>) model.get(
          "fdlnodes");
      for (LinkedHashMap<String, Object> yamlFDL : fdls) {
        Entry<String, Object> entry = yamlFDL.entrySet().stream().toList().get(0);
        assert entry.getValue() instanceof LinkedHashMap;
        String fdlName = entry.getKey();//yamlFDL.keySet().stream().toList().get(0);
        String fdlFileName = ((LinkedHashMap<String, String>) entry.getValue()).getOrDefault(
            "filename", "");

        LinkedHashMap<String, Object> fdlMap = (LinkedHashMap<String, Object>) yamlFDL.get(fdlName);
        List<TopicNode> prereq = new ArrayList<>();
        for (String pre : (List<String>) fdlMap.getOrDefault("prereq", new ArrayList<>())) {
          prereq.add(topicnodes.get(pre));
        }
        fdlnodes.put(fdlName,
            new FdlNode(fdlName, (String) fdlMap.get("id"),
                fdlFileName.equals("") ? fdlName + ".fdl" : fdlFileName, prereq, "", null,
                true));
      }
    }

    // bi-link all topic nodes + link fdlnodes
    for (TopicNode node : topicnodes.values()) {
      for (String child : topicnodesChildren.get(node.getNodeName())) {
        node.getChildren().add(topicnodes.get(child));
        TopicNode t = topicnodes.get(child);
        if (t == null) {
          throw new ModelException(
              "For no topic node '%s' found for parent '%s'.".formatted(child, node.getNodeName()));
        }
        t.setParent(node);
      }
      for (String fdl : fdlnodesChildren.get(node.getNodeName())) {
        FdlNode fdlNode = fdlnodes.get(fdl);
        if (fdlNode == null) {
          throw new ModelException(
              "For topic '%s' unable to find FDL '%s'.".formatted(node.getNodeName(), fdl));
        }
        node.getFdls().add(fdlNode);
        fdlnodes.get(fdl).setTopicNode(node);
      }
    }

    // search for root topicnode
    for (TopicNode node : this.topicnodes.values()) {
      if (node.getParent() == null) {
        this.modelroot = node;
      }
    }
    if (this.modelroot == null) {
      //todo throw exception
    }

    // link prereqs of fdlnodes
    //todo

    // resolve fdl models and link symbols to fdlnodes
    if (withFdlParse) {
      //todo: refactor the actual file name check
      List<String> fdlFiles = Arrays.stream(
          Objects.requireNonNull(new File(getFullModelPath(), "fdls").list())).toList();
      FdlTool cli = new FdlTool();
      for (FdlNode fdl : fdlnodes.values()) {
        if (fdlFiles.stream().noneMatch(fdl.getFileName()::equals)) {
          throw new ModelException(
              String.format("No file named '%s' found in fdl directory.", fdl.getFileName()));
        }
        fdl.setSymbolAndType(
            cli.fullParse(getFullModelPath() + "/fdls/" + fdl.getFileName(), 1).getLocalSymbol());
        if (fdl.getFdlType() == FdlTypes.CONSTANT) {
          ConstantNumberSymbol sym = (ConstantNumberSymbol) fdl.getSymbol();
          constants.put(sym.getTerm(), sym);
        }
      }
    }

    for (FdlNode node : fdlnodes.values()) {
      if (node.getSymbol() instanceof EquationSymbol) {
        EquationSymbol sym = (EquationSymbol) node.getSymbol();
        for (Entry<String, Reference> e : sym.getReferenceMap().entrySet()) {
          if (e.getValue().isConstant()) {
            ConstantNumberSymbol csym = constants.get(e.getValue().getConstantName());
            if (csym == null) {
              throw new ModelException("Unable to find constant.");
            }
            e.getValue().setConstant(csym);
          }
        }
      }
    }
  }

  private void initTemplateMatrix() {
    templateMatrix = new HashMap<>();
    templateObjects = new HashMap<>();

    Map<String, List<Class<?>>> kennenDim = new HashMap<>();
    Map<String, List<Class<?>>> koennenDim = new HashMap<>();
    Map<String, List<Class<?>>> verstehenDim = new HashMap<>();

    String[] inputArten = {"fill-out", "text-answers", "multiple-choice", "single-choice",
        "ordering", "assigning", "calculation", "multi-calculation"};

    for (String inpType : inputArten) {
      kennenDim.put(inpType, new ArrayList<>());
      koennenDim.put(inpType, new ArrayList<>());
      verstehenDim.put(inpType, new ArrayList<>());
    }

    templateMatrix.put("kennen", kennenDim);
    templateMatrix.put("koennen", koennenDim);
    templateMatrix.put("verstehen", verstehenDim);
  }

  /**
   * Method uses reflection to get all classes within 'generator.exercises.implementations' and
   * places them within the templateMatrix according to which interfaces they implement
   *
   * @throws NoSuchMethodException     a found class does not have a parameterless constructor
   * @throws InvocationTargetException todo
   * @throws InstantiationException    todo
   * @throws IllegalAccessException    todo
   */
  private void cacheTemplateMatrix()
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Reflections reflections = new Reflections("generator.exercises.implementations");
    //Set<Class<?>> implementedClasses = reflections.get(SubTypes.of(AbstractExercise.class).asClass()); //still contains abstract super classes
    //todo: figure out org.reflections filter
    Set<Class<?>> exerciseTypes = reflections.getSubTypesOf(AbstractExercise.class).stream().filter(
        c -> !Modifier.isAbstract(c.getModifiers())
    ).collect(Collectors.toSet());

    for (Class<?> clazz : exerciseTypes) {
      AbstractExercise exObj = (AbstractExercise) clazz.getConstructor().newInstance();

      Map<Class<?>, String> classMap = new HashMap<>();
      classMap.put(FillOutExercise.class, "fill-out");
      classMap.put(TextAnswersExercise.class, "text-answers");
      classMap.put(TextSingleChoiceExercise.class, "single-choice");
      classMap.put(TextMultipleChoiceExercise.class, "multiple-choice");
      classMap.put(OrderingExercise.class, "ordering");
      classMap.put(AssigningExercise.class, "assigning");
      classMap.put(CalculationExercise.class, "calculation");
      classMap.put(MultiCalculationExercise.class, "multi-calculation");

      for (Class<?> intrface : classMap.keySet()) {
        if (intrface.isAssignableFrom(clazz)) {
          if (exObj.isKennen()) {
            this.templateMatrix.get("kennen").get(classMap.get(intrface)).add(clazz);
          }
          if (exObj.isKoennen()) {
            this.templateMatrix.get("koennen").get(classMap.get(intrface)).add(clazz);
          }
          if (exObj.isVerstehen()) {
            this.templateMatrix.get("verstehen").get(classMap.get(intrface)).add(clazz);
          }
          templateObjects.put(clazz.getSimpleName(), exObj);
        }
      }
    }
  }

  public String getModelName() {
    return modelName;
  }

  /**
   * Returns the absolute path to the directory in which the model lies in. E.g. if the Model 'TL22'
   * lies in 'C://a/b/c/TL22', it returns 'C://a/b/c'
   *
   * @return String of absolute path
   */
  public String getModelPath() {
    return modelDirectory;
  }

  /**
   * Returns the absolute path of the model itself. E.g. 'C://a/b/c/TL22', 'TL22' being the model.
   *
   * @return String of absolute path
   */
  public String getFullModelPath() {
    return (new File(modelDirectory, modelName)).getAbsolutePath();
  }

  public TopicNode getModelRoot() {
    return modelroot;
  }

  public Map<String, Map<String, List<Class<?>>>> getTemplateMatrix() {
    return templateMatrix;
  }

  public Map<String, AbstractExercise> getTemplateObjects() {
    return templateObjects;
  }

  public Map<String, TopicNode> getTopicnodes() {
    return topicnodes;
  }

  public Map<String, FdlNode> getFdlnodes() {
    return fdlnodes;
  }

  public Map<String, ConstantNumberSymbol> getConstants() {
    return constants;
  }

  public FdlNode getVarGroupsFdlNode() {
    return varGroupsFdlNode;
  }
}
