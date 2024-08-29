# Developer guide to the Genie exercise generator

This guide is based on Windows, but everything *should* be available/work on Linux/Mac.

## Prerequisites

### Java

#### Install Open JDK

Download at https://jdk.java.net/17/. Extract to the preferred location, e.g. C:/Java/jdk-17.x

JDK17 is preferred due to long term support, but JDK 16 should also work. (Yes, lower Versions do NOT work)

#### Set JAVA_HOME

1. In Windows settings search for "Environment Variables" and select "Edit the system environment
   variables"
2. Click the Environment Variables button
3. Under System Variables, click New
4. In the Variable Name field enter JAVA_HOME
5. Variable Value field, enter your JDK installation path, e.g. C:/Java/jdk-17.x

### IntelliJ

IntelliJ is a powerful Java IDE which also incorporates Gradle and git. Download
at https://www.jetbrains.com/de-de/idea/ and install it.

Download Google's Java code
style: https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml  
Import it under File - Settings - Search "Java" - Code Styles - Java - Click cog next to Schemes -
Import IntelliJ scheme

Use IntelliJ instead of passing everything to Gradle:  
Go to File - Settings - Build, Execution, Deployment - Build Tools - Gradle  
and select 'IntelliJ IDEA' under 'Build and run using:' and 'Run tests using:'

### Install fdl-dsl project

1. Clone the fdl-dsl repository (master branch unless you have a reason to install dev versions)
   (if you dont have a git client you can download it as a zip and extract it somewhere)
2. Open the project in IntelliJ (right-click directory and ~"Open as IntelliJ project")
3. In the top right press "Gradle"
4. Press the elephant icon ("Execute Gradle Task")
5. Enter "gradle publishToMavenLocal" and hit enter (careful: gradle keyword is already given)
6. Close IntelliJ with fdl-dsl on completion

### Install Symja (computer algebra system library)
1. Download/Clone https://github.com/axkr/symja_android_library
2. Open project in IntelliJ
3. Open the Maven tab at the top right
4. Press the 'm' button
5. Execute "mvn install"

### Get generator project

1. Clone generator repository (this repo)
2. Open in IntelliJ and run "gradle clean install"
3. Work

## Implementing exercise types aka templates

### Generally

During exercise generation all classes in src/main/java/generator/exercises/implementation are taken
into account when selecting an exercise type. When adding new classes note that:

- classes must not be abstract as they are instantiated during generation
- classes should/must be extensions of classes in exercises.inputs.classes, which represent the
  input type of the answer. This means:
    - implementing abstract methods
    - defining which KKV dimensions are targeted and which FDL-type is primarily used
    - defining a 0 argument constructor, i.e. constructor()
    - defining a constructor(FdlNode, GenerationTask)
    - defining name of the freemarker template to be used (if not set the name of the class will be
      assumed to also be the template's name)
- standard implementations can naturally be overridden if needed

### Workflow

1. Select FDL you want to test your template on.
    1. If it does not exist it needs to be created. If so, it also needs to be added to the
       knowledge model as FDL node and connected accordingly.
2. Add/Copy a class in exercises.implementations; see subsection above
3. Add/Copy a testing class in test.java.generator.exercises.implementations
    1. If test successful, there should be html files in the target directory

## Useful links:

- Freemarker docs: https://freemarker.apache.org/docs/index.html
- HTML docs: https://developer.mozilla.org/en-US/docs/Web/HTML

## Other notes:

- 'SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".' is not an error and has no
  impact on the code; same as other logging/SLF4J messages
- The FDL-DSL exception pipeline needs serious work, so if you run into uninformative unhandled
  generator.exceptions you probably have a faulty model 