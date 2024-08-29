package generator.exercises.implementations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileWritingTest {

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
}
