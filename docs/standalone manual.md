# Standalone Manual for the Generator

This manual describes how to use the GENIE generator in combination with a knowledge model to
generate exercises.

## Prerequisites:

1. Java 15+ installed (get newest version here: https://jdk.java.net/ or here: https://www.oracle.com/java/technologies/downloads/#jdk19-windows (easier via msi/exe-file))
2. Have a knowledge model at hand. This belongs into a folder named `knowledgemodels`, in the same
   directory as the jar.

## Getting the executable / jar

### Downloading the jar

Check the official git repository's releases [here](https://collaborating.tuhh.de/w-6/forschung/generating/knowledgerepository/-/tree/master/GENIE%20CLI).

### Building the jar yourself

1. todo

## Using the Generator

1. Open Windows Powershell or Commandline (Windows) or Terminal (macOS)
2. Enter the command `cd [path of the directory where the jar is located]` (e.g. `cd C:\Users\Me\Desktop\GeneratingStuff`) -> press Enter
3. To start the Generator, enter the command `java -jar GENIE-Generator.jar` -> press Enter
4. Now you are in the main menu and can enter the command (see commands below).

   After every command press Enter to execute the command.

### Generating Exercises:

Before being able to generate, a knowledge model must be loaded. Use the `use model` command
for this. If the model has already been cached it will be used, otherwise the tool will try caching 
it.

To generate an exercise there exist different methods, which take different sources into account.
You can generate an exercise from either a single FDL, a single FDL using a certain template, a
topic, or a course. The see command list below for the commands. The exercises will be generated
into the `target` directory in the same directory as the jar as `topic`.

To change the parameters (e.g. difficulty), enter `-p` at the end of the generate command.

### Checking FDLs and Knowledge Models

The generator also provides a few options to *mostly syntactically* check FDLs and knowledge models 
you created:

The `parse fdl(s)` commands attempt to parse the FDLs. 

The `check all templates for fdls` checks if all FDLs don't produce unexpected errors for 
templates they should work with.

The `try caching` command will try to load an entire model, as if it were in production.

## List of Commands:

| Command | Description |
|---------|-------------|
| `help` | List all possible commands. |
| `exit` | Exits the application. |
| `list templates` | List all possible task types. |
| `check all templates for fdls` | Tries processing all templates for a FDL. |
| `parse fdl` | Tries parsing a single FDL. |
| `parse fdls` | Tries parsing all FDLs in a given directory. |
| `try caching` | Executes caching of a knowledge model, but does **not** keep it in cache. The first asked input is the directory in which the model directory (second input) lies. Example: Arg1: "C:/User/Me/Desktop/Models", Arg2: "TL_SS22" |
| `use model` | Selects a knowledge model to use. If the model is not cached yet it will loaded. The first asked input is the directory in which the model directory (second input) lies. Example: Arg1: "C:/User/Me/Desktop/Models", Arg2: "TL_SS22"|
| `generate from fdl [-p]` | Generate an exercise from a specific FDL. Add `-p` to change parameters. |
| `generate with template [-p]` | Generate an exercise from a specific FDL using a specified template. Add `-p` to change parameters. |
| `generate from topic [-p]` | Generate an exercise from any FDL of a specified topic. Add `-p` to change parameters. |
| `generate from course [-p]` | Generate an exercise from any FDL in any topic of a specified course. Add `-p` to change parameters. |
