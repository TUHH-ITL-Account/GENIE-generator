package generator.types;

import generator.caching.Cache;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GenerationTask {

  private final String modelName;
  private String topicName;
  private String fdlName;

  private Cache cacheReference;

  private Map<String, Object> parameters;
  private Map<String, String> paramSetters;

  private Date timeStarted;
  private Date timeFinished;

  private TASK_STATUS taskStatus;
  private Exception failureException;

  private EXERCISE_TYPE exType;
  private SOLUTION_TYPE solType;

  private String partHtmlExercise;
  private String fullHtmlExercise;
  private Map<String, String> solutionMap;
  private String jsonSolution;
  private String partHtmlSolution;
  private String fullHtmlSolution;

  private Random randomInstance;

  private String modeldir;

  /**
   * Used e.g. when generating for a course, but topic & FDL are yet to be determined
   *
   * @param modelName name of the knowledge model
   */
  public GenerationTask(String modelName) {
    this.modelName = modelName;
  }

  /**
   * Used e.g. when generating for a topic of a course, but FDL is yet to be determined.
   *
   * @param modelName name of the knowledge model
   * @param topicName name of the topic in the knowledge model
   */
  public GenerationTask(String modelName, String topicName) {
    this.modelName = modelName;
    this.topicName = topicName;
  }

  /**
   * Used e.g. when generating for a topic of a course with a given FDL.
   *
   * @param modelName name of the knowledge model
   * @param topicName name of the topic in the knowledge model
   * @param fdlName   name of the FDL in the knowledge model
   */
  public GenerationTask(String modelName, String topicName, String fdlName) {
    this.modelName = modelName;
    this.topicName = topicName;
    this.fdlName = fdlName;
  }

  public void initMissingWithDefaults() {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.putIfAbsent("topic_actual", topicName);
    initParamSetters();
    if (randomInstance == null) {
      if (parameters.containsKey("seed")) {
        randomInstance = new Random((long) parameters.get("seed"));
      } else {
        randomInstance = new Random();
        long seed = randomInstance.nextLong();
        randomInstance.setSeed(seed);
        parameters.put("seed", seed);
      }
    }
    if (this.exType == null) {
      this.exType = EXERCISE_TYPE.FULL;
    }
    if (this.solType == null) {
      this.solType = SOLUTION_TYPE.FULL;
    }
    if (modeldir == null) {
      modeldir = "";
    }
  }

  public void initParamSetters() {
    if (paramSetters != null) {
      return;
    }
    @SuppressWarnings("unchecked")
    Map<String, String> parSetters = (Map<String, String>) parameters.get("paramSetters");
    if (parSetters == null) {
      paramSetters = new HashMap<>();
      parameters.put("paramSetters", paramSetters);
    } else {
      paramSetters = parSetters;
    }
  }

  public String getModelName() {
    return modelName;
  }

  public String getFdlName() {
    return fdlName;
  }

  public void setFdlName(String fdlName) {
    this.fdlName = fdlName;
  }

  public String getTopicName() {
    return topicName;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public Date getTimeStarted() {
    return timeStarted;
  }

  public void setTimeStarted() {
    this.timeStarted = new Date();
  }

  public Date getTimeFinished() {
    return timeFinished;
  }

  public void setTimeFinished() {
    this.timeFinished = new Date();
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public TASK_STATUS getTaskStatus() {
    return taskStatus;
  }

  public void setTaskStatus(TASK_STATUS taskStatus) {
    this.taskStatus = taskStatus;
  }

  public String getPartHtmlExercise() {
    return partHtmlExercise;
  }

  public void setPartHtmlExercise(String partHtmlExercise) {
    this.partHtmlExercise = partHtmlExercise;
  }

  public String getPartHtmlSolution() {
    return partHtmlSolution;
  }

  public void setPartHtmlSolution(String partHtmlSolution) {
    this.partHtmlSolution = partHtmlSolution;
  }

  public Exception getFailureException() {
    return failureException;
  }

  public void setFailureException(Exception failureException) {
    this.failureException = failureException;
  }

  public EXERCISE_TYPE getExType() {
    return exType;
  }

  public void setExType(EXERCISE_TYPE exType) {
    this.exType = exType;
  }

  public SOLUTION_TYPE getSolType() {
    return solType;
  }

  public void setSolType(SOLUTION_TYPE solType) {
    this.solType = solType;
  }

  public String getFullHtmlExercise() {
    return fullHtmlExercise;
  }

  public void setFullHtmlExercise(String fullHtmlExercise) {
    this.fullHtmlExercise = fullHtmlExercise;
  }

  public Map<String, String> getSolutionMap() {
    return solutionMap;
  }

  public void setSolutionMap(Map<String, String> solutionMap) {
    this.solutionMap = solutionMap;
  }

  public String getJsonSolution() {
    return jsonSolution;
  }

  public void setJsonSolution(String jsonSolution) {
    this.jsonSolution = jsonSolution;
  }

  public String getFullHtmlSolution() {
    return fullHtmlSolution;
  }

  public void setFullHtmlSolution(String fullHtmlSolution) {
    this.fullHtmlSolution = fullHtmlSolution;
  }

  public Random getRandomInstance() {
    return randomInstance;
  }

  public void setRandomInstance(Random randomInstance) {
    this.randomInstance = randomInstance;
  }

  public String getModeldir() {
    return modeldir;
  }

  public void setModeldir(String modeldir) {
    this.modeldir = modeldir;
  }

  public Map<String, String> getParamSetters() {
    return paramSetters;
  }

  public Cache getCacheReference() {
    return cacheReference;
  }

  public void setCacheReference(Cache cacheReference) {
    this.cacheReference = cacheReference;
  }

  public enum TASK_STATUS {
    PROCESSING, FINISHED, ERROR
  }

  public enum EXERCISE_TYPE {
    FULL, FULL_HTML, PART_HTML, TEX
  }

  public enum SOLUTION_TYPE {
    FULL, FULL_HTML, PART_HTML, TEX, JSON
  }
}
