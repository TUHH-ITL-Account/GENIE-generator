package generator;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fdl.exceptions.DslException;
import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.VariableGroupsSymbol;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.caching.FdlNode;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import generator.types.GenerationTask;
import generator.types.GenerationTask.EXERCISE_TYPE;
import generator.types.GenerationTask.SOLUTION_TYPE;
import generator.types.GenerationTask.TASK_STATUS;
import generator.util.SymjaHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.math.MathException;

public class Custom {

  @Test
  public void testSpecific()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException, UnfulfillableException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
    try {
      cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    // cfg.setAPIBuiltinEnabled(true);

    Generator gen = new Generator();
    String model = "TechnischeLogistikSS22";
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        model, true);
    String fdlName = "Antriebsleistung_Eq";
    String topic = gen.getCacheMap().get(model).getFdlnodes().get(fdlName).getTopicNode()
        .getNodeName();
    gen.ATTEMPT_LIMIT = 1;
    GenerationTask dummyTask = new GenerationTask(model, topic, fdlName);
    for(int i=0;i<500;i++) {
      dummyTask = new GenerationTask(model, topic, fdlName);

      dummyTask.initMissingWithDefaults();
      //dummyTask.setCacheReference(c);
      dummyTask.getParameters().put("template_actual", "PoolCalculation");
      dummyTask.getParameters().put("additional_fdls", "Hubkraft_Eq,Hubleistung_Eq,Überwindung_Rollwiderstand_Eq");
      dummyTask.getParameters().put("difficulty", 1);
      dummyTask.getRandomInstance().setSeed(2746215089365041182L);

      gen.generateFromFdl(model, topic, fdlName, dummyTask);
    }
    //System.out.println(dummyTask.getParameters());


    /*SelectCharacterFromMC exercise = new SelectCharacterFromMC(node, dummyTask);
    exercise.generateAbstractExercise();
    exercise.generateFullHtmlExercise(cfg);
    exercise.generateFullHtmlSolution(cfg);
    exercise.generateJsonSolution();*/

    String htmlExercise = dummyTask.getFullHtmlExercise();
    String htmlSolution = dummyTask.getFullHtmlSolution();

    assertThat("htmlExercise is not null", htmlExercise, notNullValue());
    assertThat("htmlSolution is not null", htmlSolution, notNullValue());
    assertThat("htmlExercise is not empty", htmlExercise, not(""));
    assertThat("htmlSolution is not empty", htmlSolution, not(""));
    assertThat("exercise is not equal to solution", htmlExercise, not(htmlSolution));

    writeIntoFile("custom_ex.html", htmlExercise);
    writeIntoFile("custom_sol.html", htmlSolution);
  }


  public void testParse() throws DslException {
    Main.parseFdl(
        "C:\\Users\\Delta\\Desktop\\Generating\\generator\\src\\test\\resources\\knowledgemodels\\TechnischeLogistikSS21\\fdls\\Planungsvarianten_Char.fdl");
  }

  @Test
  public void testWM()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Main.tryCaching(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        "TechnischeLogistikSS22");
  }

  protected void writeIntoFile(String filename, String content) {
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
      System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  @Test
  public void testChainCalculation()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException {
    Generator gen = new Generator();
    gen.setup();
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        "TechnischeLogistikSS22", true);

    GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS22", "xxx",
        "LängeneinheitbezogeneGutlast_Eq1");
    dummyTask.initMissingWithDefaults();
    dummyTask.setCacheReference(gen.getCacheMap().get("TechnischeLogistikSS22"));
    dummyTask.getParameters().put("template_actual", "ChainCalculation");
    dummyTask.getParameters().put("additional_fdls", "LängeneinheitbezogeneGutlast_Eq2");

    GenerationTask task = gen.generateFromFdl("TechnischeLogistikSS22", "x",
        "LängeneinheitbezogeneGutlast_Eq1", dummyTask);

    String htmlSolution = task.getFullHtmlSolution();
    writeIntoFile("custom_sol.html", htmlSolution);
  }

  @Test
  public void testGenerateFromTopicNoFDLGiven()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException {
    Generator gen = new Generator();
    gen.setup();
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        "TechnischeLogistikSS22", true);

    GenerationTask gTask = new GenerationTask("TechnischeLogistikSS22");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("difficulty", 1);
    parameters.put("topic_actual", "Fördersysteme");
    //parameters.put("template_actual","SelectCharacterFromMC");
    parameters.put("noAI", false);
    parameters.put("paramSetters", new HashMap<>());
    parameters.put("seed", 7397613477117415324L);
    gTask.setParameters(parameters);
    gTask.setExType(EXERCISE_TYPE.FULL_HTML);
    gTask.setSolType(SOLUTION_TYPE.FULL_HTML);
    gTask.initMissingWithDefaults();
    gen.generateFromTopic("TechnischeLogistikSS22", "Fördersysteme", gTask);
  }

  @Test
  public void generateFromCourseWildly()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, UnfulfillableException {
    Generator gen = new Generator();
    gen.DEBUG_MODE = false;
    gen.setup();
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        "TechnischeLogistikSS22", true);

    /*GenerationTask dummyTask = new GenerationTask("TechnischeLogistikSS22", "Anschlagmittel",
        "Seilkraft_bei_zenLast_Eq");
    dummyTask.initMissingWithDefaults();
    dummyTask.setCacheReference(gen.getCacheMap().get("TechnischeLogistikSS22"));
    dummyTask.getParameters().put("template_actual", "ChainCalculation");
    dummyTask.getParameters().put("additional_fdls", List.of("Länge_Strang_zenLast_Eq"));*/
    //GenerationTask task = gen.generateFromFdl("TechnischeLogistikSS22", "Anschlagmittel", "Seilkraft_bei_zenLast_Eq", dummyTask);

    List<GenerationTask> failedTasks = new ArrayList<>();
    for (int i = 0; i < 5000; i++) {
      /*GenerationTask task = new GenerationTask("TechnischeLogistikSS22");
      task.setCacheReference(gen.getCacheMap().get("TechnischeLogistikSS22"));
      task.initMissingWithDefaults();
      task = gen.generateFromCourse("TechnischeLogistikSS22", task);*/
      //GenerationTask task = gen.generateFromCourse("TechnischeLogistikSS22", null);
      GenerationTask task = gen.generateFromFdl("TechnischeLogistikSS22", "X", "Fachlast_Eq", null);
      if (task.getTaskStatus() == TASK_STATUS.ERROR) {
        failedTasks.add(task);
      }
    }

    int i = 0;
    for (GenerationTask t : failedTasks) {
      System.out.println(
          i + ") " + t.getParameters().get("template_actual") + " " + t.getParameters()
              .get("fdl_actual")
              + " " + t.getParameters().get("seed"));
      i++;
    }
  }

  @Test
  public void testVarGroupsCorrect()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException, UnfulfillableException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
    try {
      cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    // cfg.setAPIBuiltinEnabled(true);

    Generator gen = new Generator();
    String model = "TechnischeLogistikSS22";
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        model, true);
    VariableGroupsSymbol vg = ((VariableGroupsSymbol)gen.getCacheMap().get(model).getVarGroupsFdlNode().getSymbol());
    for(List<SimpleEntry<String, String>> group : vg.getVariableGroups()) {
      for (SimpleEntry<String, String> pair : group) {
        EquationSymbol sym = ((EquationSymbol) gen.getCacheMap().get(model).getFdlnodes()
            .get(pair.getValue()).getSymbol());
        if (!sym.getAliasMap().containsKey(pair.getKey())) {
          //System.out.println(pair.getKey()+" "+pair.getValue());
          System.out.println(
              "### Missing " + pair.getKey() + " from " + pair.getValue() + ". Try:");
          System.out.println(sym.getAliasMap().keySet());
        }
      }
    }
    for(FdlNode node : gen.getCacheMap().get(model).getFdlnodes().values()) {
      if(node.getSymbol() instanceof EquationSymbol) {
        EquationSymbol sym = (EquationSymbol)node.getSymbol();
        for(String id : sym.getAliasMap().keySet()) {
          if(!vg.getVariableToGroupsMap().containsKey(new SimpleEntry<>(id,node.getFdlName()))) {
            System.out.println("### Missing "+id+" from "+node.getFdlName());
          }
        }
      }
    }
  }

  public void testInBulkStudie()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException, UnfulfillableException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
    try {
      cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    // cfg.setAPIBuiltinEnabled(true);

    List<String> toTest = new ArrayList<>();
    toTest.add("Kraft_Überwindung_Reibwiderstand_Eq");
    toTest.add("Volumenstrom_Einzelgefässe_Eq,Massenstrom1_Eq,Nennleistung_des_Antriebs_Eq");
    toTest.add("Nennleistung_des_Antriebs_Eq");
    toTest.add("LängeneinheitbezogeneGutlast_Eq2,Kraft_Überwindung_Gesamtwiderstand_Eq,Kraft_Überwindung_Hubwiderstand_Eq,Kraft_Überwindung_Reibwiderstand_Eq");
    toTest.add("Becherwerk_Gesamtwiderstand_aufwärts_Eq");
    toTest.add("Nennleistung_Becherwerk_Eq,LängenbezogeneEigenlast_Eq");
    toTest.add("Förderleistung_Schneckenförderer_Eq");
    toTest.add("Massenstrom2_Eq,Füllungsgrad_Schneckenförderer_Eq");
    toTest.add("Staplerzahl_Eq");
    toTest.add("Transportkosten_Eq,Stapler_Fördermenge_Eq");
    toTest.add("Massenstrom_Routenzug_Eq");
    toTest.add("Transportkosten_Routenzug_Eq,Nutzlast_Routenzug_Eq");
    toTest.add("Gegengewicht_Kippsicherheit_Eq");
    toTest.add("Kippsicherheit_Eq");
    toTest.add("Seilkraft_bei_zenLast_Eq");
    toTest.add("Höhe_Balken_Eq,Länge_Strang_zenLast_Eq");
    toTest.add("BalkenSP_Lot_Abstand_Eq");
    toTest.add("Auslenkung_Balken_dezenLast_Eq,Ersatzkraft_Eq");
    toTest.add("Hubkraft_Eq");
    toTest.add("Hubleistung_Eq,Antriebsleistung_Eq,Überwindung_Rollwiderstand_Eq");
    toTest.add("Gesamter_Flächenbedarf_Eq");
    toTest.add("Flächennutzungsgrad_Eq,Grundflächenbedarf_Eq");
    toTest.add("Regalmeter_Eq");
    toTest.add("Lagerfläche_Eq,Regalfläche_Eq,Anzahl_Felder_Eq,Fächer_pro_Feld_Eq");
    toTest.add("Kommissionierzeit_WzP_Eq");
    toTest.add("Bearbeitungszeit_Eq");
    toTest.add("Kommissionierleistung_Kommissionierer_Eq");
    toTest.add("Berechnung_Kommissioniereranzahl_Eq,Berechnung_Kommissionierzeit_pro_Tag_Eq");
    toTest.add("Mittlere_Gesamtstrecke_Eq");
    toTest.add("Mittlere_Fahrtzeit_Eq");
    toTest.add("Beschleunigungszeit_Standardfahrrampe_Eq");
    toTest.add("Beschleunigungsweg_Standardfahrrampe_Eq,Zusammenführungszeit_Standardfahrrampe_Eq");

    Generator gen = new Generator();
    String model = "TechnischeLogistikSS22";
    gen.cacheModel(
        "C:\\Users\\Delta\\Desktop\\Generating\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        model, true);
    for(int j=0;j<500;j++) {
      for (int i = 0; i < toTest.size(); i = i + 2) {
        String fdlName = toTest.get(i);
        String topic = gen.getCacheMap().get(model).getFdlnodes().get(fdlName).getTopicNode()
            .getNodeName();
        GenerationTask dummyTask = new GenerationTask(model, topic, fdlName);

        dummyTask.initMissingWithDefaults();
        dummyTask.getParameters().put("template_actual", "PoolCalculation");
        dummyTask.getParameters().put("additional_fdls", toTest.get(i + 1));
        //dummyTask.getParameters().put("difficulty", 7);
        //dummyTask.getRandomInstance().setSeed(5097376160741927330L);

        gen.generateFromFdl(model, topic, fdlName, dummyTask);

        //System.out.println(dummyTask.getParameters());

        String htmlExercise = dummyTask.getFullHtmlExercise();
        String htmlSolution = dummyTask.getFullHtmlSolution();

        assertThat("htmlExercise is not null", htmlExercise, notNullValue());
        assertThat("htmlSolution is not null", htmlSolution, notNullValue());
        assertThat("htmlExercise is not empty", htmlExercise, not(""));
        assertThat("htmlSolution is not empty", htmlSolution, not(""));
        assertThat("exercise is not equal to solution", htmlExercise, not(htmlSolution));
      }
    }
    //writeIntoFile("custom_ex.html", htmlExercise);
    //writeIntoFile("custom_sol.html", htmlSolution);
  }

  public void tReg() {
    String t = "{x->-123.123},{x->123.123}}";
    Pattern pattern = Pattern.compile("\\{x->(.*?)}");
    Matcher matcher = pattern.matcher(t);
    List<String> finds = new ArrayList<>();
    while(matcher.find()) {
      finds.add(matcher.group(1));
    }
    System.out.println(finds);
  }

  @Test
  public void testEnum() {
    enum TENUM {
      ASD, FGH
    }
    System.out.println(TENUM.values()[0] == TENUM.ASD);
  }

  class A {
    protected void run() {
      test1();
      test2();
    }

    protected void test1() {
      System.out.println("a1");
    }
    protected void test2() {
      System.out.println("a2");
    }
  }

  class B extends A {
    @Override
    protected void test1() {
      System.out.println("b1");
    }

    protected void runSuper() {
      run();
      super.run();
      super.test1();
    }
  }

  @Test
  public void testSuperMethod() {
    Map<String,Object> m = new HashMap<>();
    System.out.println(m.get("asd") instanceof Boolean);
  }
}
