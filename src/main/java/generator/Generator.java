package generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import fdl.exceptions.DslException;
import fdl.fdl._symboltable.ConstantNumberSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.Cache;
import generator.caching.FdlNode;
import generator.caching.TopicNode;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import generator.exercises.inputs.classes.AbstractExercise;
import generator.types.GenerationTask;
import generator.types.GenerationTask.TASK_STATUS;
import generator.util.CollectionHelper;
import generator.util.KnowledgeModelHelper;
import generator.util.TemplateHelper;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generator {

  public static Map<String, Map<String, ConstantNumberSymbol>> CONSTANTS_MAP = new HashMap<>();
  protected boolean DEBUG_MODE = true;
  protected int ATTEMPT_LIMIT = 50;
  private Map<String, Cache> cacheMap;
  private Configuration cfg;

  public Generator() {
    setup();
  }

  public void setup() {
    this.cacheMap = new HashMap<>();
    this.cfg = new Configuration(Configuration.VERSION_2_3_28);
    this.cfg.setClassForTemplateLoading(this.getClass(), "/templates");
    this.cfg.setDefaultEncoding("UTF-8");
    this.cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    this.cfg.setLogTemplateExceptions(false);
    this.cfg.setWrapUncheckedExceptions(true);
  }

  public void reloadModel(String modelName, boolean withFdlParse)
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    String model = KnowledgeModelHelper.cutExtension(modelName);
    Cache old = cacheMap.get(model);
    if (old == null) {
      return;
    }
    Cache reloaded = new Cache(old.getModelPath(), modelName, withFdlParse);
    cacheMap.put(model, reloaded);
    CONSTANTS_MAP.put(model, reloaded.getConstants()); //BAH
  }

  public void cacheModel(String modelDirectory, String modelName, boolean withFdlParse)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, DslException, ModelException {
    String model = KnowledgeModelHelper.cutExtension(modelName);

    Cache cache = new Cache(modelDirectory, modelName, withFdlParse);
    cacheMap.put(model, cache);
    CONSTANTS_MAP.put(model, cache.getConstants()); //BAH
  }

  public GenerationTask generateFromCourse(String model, GenerationTask task)
      throws UnfulfillableException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JsonProcessingException {
    if (model == null) {
      throw new UnfulfillableException("Model cannot be null.");
    }
    if (task == null) {
      task = new GenerationTask(model);
      task.initMissingWithDefaults();
    }
    task.setModeldir(cacheMap.get(model).getFullModelPath());
    if (!cacheMap.containsKey(model)) {
      throw new UnfulfillableException("Model not cached: " + model);
    }
    Object topic = task.getParameters().get("topic_actual");
    if (topic != null) {
      return generateFromTopic(model, (String) topic, task);
    }
    int numTopics = cacheMap.get(model).getTopicnodes().size();
    int start = task.getRandomInstance().nextInt(numTopics);
    List<TopicNode> topicNodeList = cacheMap.get(model).getTopicnodes().values().stream().toList();
    for (int i = 0; i < numTopics; i++) {
      TopicNode t = topicNodeList.get((start + i) % numTopics);
      if (!t.getFdls().isEmpty() && t.isGenerationSource()) {
        task.setTopicName(t.getNodeName());
        task.getParameters().put("topic_actual", t.getNodeName());
        task.initParamSetters();
        task.getParamSetters().put("topic_actual", "GN");
        String fdl = t.getFdls().get(task.getRandomInstance().nextInt(t.getFdls().size()))
            .getFdlName();
        task.setFdlName(fdl);
        return generateFromFdl(model, t.getNodeName(), fdl, task);
      }
    }
    throw new UnfulfillableException("Model does not contain any FDLs.");
  }

  /**
   * Generate a random exercise for a specified topic in a knowledge-model
   *
   * @param model name of the knowledge-model
   * @param topic name of the topicnode in the knowledge-model
   * @return a GenerationTask object for this generation task
   */
  public GenerationTask generateFromTopic(String model, String topic, GenerationTask task)
      throws UnfulfillableException, JsonProcessingException {
    if (model == null || topic == null) {
      throw new UnfulfillableException("No model/topic given.");
    }
    if (!cacheMap.containsKey(model)) {
      throw new UnfulfillableException("Given model '" + model + "' not cached.");
    }
    if (task == null) {
      task = new GenerationTask(model, topic);
      task.initMissingWithDefaults();
    }
    task.setModeldir(cacheMap.get(model).getFullModelPath());
    task.getParameters().putIfAbsent("topic_actual", topic);
    task.initParamSetters();
    task.getParamSetters().putIfAbsent("topic_actual", "GN");
    task.setTimeStarted();

    Cache cache = cacheMap.get(model);
    String selectedFdl = (String) task.getParameters().get("fdl_actual");
    Class<?> selectedTemplate = stringToTemplateClass(
        (String) task.getParameters().get("template_actual"), cache);
    int topicFdlIndex = 0;
    if (selectedFdl != null) {
      task = generateFromFdl(model, topic, selectedFdl, task);
    } else { // if we need to choose a FDL, shuffle and use first one which is usable by a template
      List<FdlNode> topicFdls = new ArrayList<>(cache.getTopicnodes().get(topic).getFdls());
      if (topicFdls.isEmpty()) {
        throw new UnfulfillableException("Selected topic does not contain any FDLs.");
      }
      Collections.shuffle(topicFdls);
      if (selectedTemplate == null) {
        do {
          selectedFdl = topicFdls.get(topicFdlIndex).getFdlName();
          List<Class<? extends AbstractExercise>> compatibleTemplates = getCompatibleTemplates(
              cache, topicFdls.get(topicFdlIndex).getSymbol());
          if (!compatibleTemplates.isEmpty()) {
            selectedTemplate = CollectionHelper.choice(compatibleTemplates,
                task.getRandomInstance());
            break;
          }
          topicFdlIndex++;
        } while (topicFdlIndex < topicFdls.size());
      } else { //topic+template are defined, only FDL is missing
        AbstractExercise template = cache.getTemplateObjects()
            .get((String) task.getParameters().get("template_actual"));
        for (FdlNode topicFdl : topicFdls) {
          if (template.isCompatibleWith(topicFdl.getSymbol())) {
            selectedFdl = topicFdl.getFdlName();
            break;
          }
        }
      }
      if (selectedTemplate == null) {
        throw new UnfulfillableException("No FDL in topic was compatible to any templates.");
      }
      task = generateFromFdlWithClass(selectedFdl, selectedTemplate, task);
    }
    return task;
  }

  private Class<?> stringToTemplateClass(String s, Cache c) {
    AbstractExercise obj = c.getTemplateObjects().get(s);
    if (obj == null) {
      return null;
    }
    return obj.getClass();
  }

  public List<Class<? extends AbstractExercise>> getCompatibleTemplates(Cache cache,
      ICommonFdlSymbol sym) {
    List<Class<? extends AbstractExercise>> compatible = new ArrayList<>();
    for (AbstractExercise template : cache.getTemplateObjects().values()) {
      if (!template.isEnabled()) {
        continue;
      }
      if (template.isCompatibleWith(sym)) {
        compatible.add(template.getClass());
      }
    }
    return compatible;
  }

  public GenerationTask generateFromFdl(String model, String topic, String fdlName,
      GenerationTask task)
      throws UnfulfillableException, JsonProcessingException {
    if (!cacheMap.containsKey(model)) {
      throw new UnfulfillableException("Given model '" + model + "' not cached.");
    }
    Cache cache = cacheMap.get(model);
    FdlNode fdlNode = cache.getFdlnodes().get(fdlName);
    if (fdlNode == null) {
      throw new UnfulfillableException(
          "Given fdl '" + fdlName + "' does not exist in model '" + model + "'.");
    }
    if (task == null) {
      task = new GenerationTask(model, topic, fdlName);
      task.initMissingWithDefaults();
    }
    task.initParamSetters();
    task.setModeldir(cacheMap.get(model).getFullModelPath());
    Class<?> selectedTemplate = stringToTemplateClass(
        (String) task.getParameters().get("template_actual"), cache);
    if (selectedTemplate == null) {
      List<Class<? extends AbstractExercise>> compatibleTemplates = getCompatibleTemplates(
          cache, fdlNode.getSymbol());
      if (!compatibleTemplates.isEmpty()) {
        selectedTemplate = CollectionHelper.choice(compatibleTemplates,
            task.getRandomInstance());
      }
    }
    if (selectedTemplate == null) {
      throw new UnfulfillableException(cache.getTopicnodes().get(topic));
    }
    task = generateFromFdlWithClass(fdlNode, selectedTemplate, task);
    return task;
  }

  public GenerationTask generateFromFdlWithClass(String fdlnode, Class<?> template,
      GenerationTask task)
      throws JsonProcessingException {
    return generateFromFdlWithClass(cacheMap.get(task.getModelName()).getFdlnodes().get(fdlnode),
        template, task);
  }

  public GenerationTask generateFromFdlWithClass(FdlNode fdlnode, Class<?> template,
      GenerationTask task)
      throws JsonProcessingException {
    if (task == null) {
      task = new GenerationTask("", fdlnode.getTopicNode().getNodeName(), fdlnode.getFdlName());
      task.initMissingWithDefaults();
    }
    if (task.getFdlName() == null || task.getFdlName().isEmpty()) {
      task.setFdlName(fdlnode.getFdlName());
    }
    task.initParamSetters();
    task.getParameters().putIfAbsent("fdl_actual", fdlnode.getFdlName());
    task.getParameters().putIfAbsent("template_actual", template.getSimpleName());
    String model = fdlnode.getTopicNode().getModelname();
    task.setModeldir(cacheMap.get(model).getFullModelPath());
    task.setCacheReference(cacheMap.get(model));
    AbstractExercise templateInstance = TemplateHelper.getInstanceFromClass(template, fdlnode,
        task);
    if (templateInstance == null) {
      task.setTaskStatus(TASK_STATUS.ERROR);
      return task;
    }
    if (templateInstance.isKennen()) {
      task.getParameters().putIfAbsent("kennen", true);
    } else if (templateInstance.isKoennen()) {
      task.getParameters().putIfAbsent("koennen", true);
    } else {
      task.getParameters().putIfAbsent("verstehen", true);
    }
    //todo set parameter exercise_type_actual? + difficulty //in templates?
    boolean success = false;
    for (int attempt = 0; attempt<ATTEMPT_LIMIT && !success; attempt++) {
      try {
        templateInstance.generateAbstractExercise();
        success = true;
      } catch (Exception e) {
        task.getRandomInstance().setSeed(task.getRandomInstance().nextLong());
        templateInstance = TemplateHelper.getInstanceFromClass(template, fdlnode, task);
        if (DEBUG_MODE) {
          task.getParameters().putIfAbsent("fdl_actual", fdlnode.getFdlName());
          task.getParameters().putIfAbsent("template_actual", template.getSimpleName());
          System.out.println("### FAILED EXERCISE GENERATION ###");
          e.printStackTrace();
          System.out.println(task.getParameters());
        }
      }
    }
    if (!success) {
      if (DEBUG_MODE) {
        task.getParameters().putIfAbsent("fdl_actual", fdlnode.getFdlName());
        task.getParameters().putIfAbsent("template_actual", template.getSimpleName());
        System.out.println("### FAILED MAX_ATTEMPTS NUMBER OF TIMES AT EXERCISE GENERATION WITH: ###");
        System.out.println(task.getParameters());
      }
      task.setTaskStatus(TASK_STATUS.ERROR);
      task.getParameters().putIfAbsent("fdl_actual", fdlnode.getFdlName());
      task.getParameters().putIfAbsent("template_actual", template.getSimpleName());
      return task;
    }
    templateInstance.generateFromTask(cfg);
    task.setTimeFinished();
    return task;
  }

  public Map<String, Cache> getCacheMap() {
    return cacheMap;
  }

}
