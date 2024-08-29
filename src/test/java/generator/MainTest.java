package generator;

import fdl.exceptions.DslException;
import generator.exceptions.ModelException;
import generator.exceptions.UnfulfillableException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Disabled;

public class MainTest {


  public void testParseFdlDir() {
    Main.parseFdls(
        "C:\\Users\\admin\\Desktop\\Generating\\Code\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik\\TechnischeLogistikSS22\\fdls");
  }


  public void testCheckAllTemplatesForFdls()
      throws ModelException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Main.checkAllTemplatesForFdls(
        "C:\\Users\\admin\\Desktop\\Generating\\Code\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik",
        "TechnischeLogistikSS22");
  }

  @Disabled
  public void testParseFdl() throws DslException {
    Main.parseFdl(
        //"C:\\Users\\admin\\Desktop\\Generating\\Code\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik\\TechnischeLogistikSS22\\fdls\\Überwindung_Rollwiderstand_Eq.fdl"
        "C:\\Users\\admin\\Desktop\\Generating\\Code\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik\\TechnischeLogistikSS22\\fdls\\Unstetigförderer_Kategorien_Fig.fdl"
    );
  }


  public void testCaching()
      throws IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ModelException {
    Main.tryCaching(
        "C:/Users/admin/Desktop/Generating/Code/knowledgerepository/tuhh/Management-Wissenschaften und Technologie (W)/Technische Logistik (W6)/Technische Logistik",
        "TechnischeLogistikSS22"
    );
  }

  @Disabled
  public void testRunCli()
      throws ModelException, UnfulfillableException, IOException, DslException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    //Main.runCLI("H:\\Hauke\\Studium\\knowledgerepository\\tuhh\\Management-Wissenschaften und Technologie (W)\\Technische Logistik (W6)\\Technische Logistik", "TechnischeLogistikSS22");
  }

}
