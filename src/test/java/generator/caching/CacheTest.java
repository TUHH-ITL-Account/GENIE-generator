package generator.caching;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fdl.exceptions.DslException;
import generator.exceptions.ModelException;
import generator.exercises.inputs.classes.AbstractExercise;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

public class CacheTest {

  @Test
  public void testModel()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    Cache c = new Cache("src/test/resources/knowledgemodels", "TechnischeLogistikSS21", true);
    assertFalse(c.getTopicnodes().isEmpty());
  }

  @Test
  public void testReflection() {
    Reflections reflections = new Reflections("generator.exercises.implementations");
    //Set<Class<?>> implementedClasses = reflections.get(SubTypes.of(AbstractExercise.class).asClass()); //still contains abstract super classes
    //todo: figure out org.reflections filter
    Set<Class<?>> implementedClasses = reflections.getSubTypesOf(AbstractExercise.class).stream()
        .filter(
            c -> !Modifier.isAbstract(c.getModifiers())
        ).collect(Collectors.toSet());

    Class<?> test = implementedClasses.stream().toList().get(0);

    try {
      AbstractExercise exObj = (AbstractExercise) test.getConstructor().newInstance();
      boolean kennen = exObj.isKennen();
      boolean koennen = exObj.isKoennen();
      boolean ver = exObj.isVerstehen();

    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }

    //AbstractExercise test = ((SelectExamplesFromMC) (implementedClasses.stream().toList().get(0)
    //   .getConstructor().newInstance(null)));
  }

  @Test
  public void testFileMechanism() {
    File rel = new File(
        "src/test/resources/knowledgemodels/TechnischeLogistikSS21/TechnischeLogistikSS21.yaml");
    File abs = new File(System.getProperty("user.dir")
        + "/src/test/resources/knowledgemodels/TechnischeLogistikSS21/TechnischeLogistikSS21.yaml");
    File mix = new File(System.getProperty("user.dir")
        + "/src/test/resources/knowledgemodels/TechnischeLogistikSS21",
        "TechnischeLogistikSS21.yaml");

    boolean r = rel.exists();
    boolean a = abs.exists();
    boolean m = mix.exists();

    assertThat("All methods work.", r && a && m);
  }
}
