package bot.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author  Wil Aquino
 * Date:    December 28, 2021
 * Project: LaunchPoint Bot
 * Module:  FileHandler.java
 * Purpose: Handles files used throughout the project.
 */
public class FileHandler {

    /** Text file that contains all file data. */
    private File file;

    public FileHandler(String name) {
        try {
            File file = new File(name);
            if (file.createNewFile()) {
                System.out.println("File created: " + name);
            } else {
                System.out.println(name + " already exists.");
            }
        } catch (IOException ioe) {
            System.out.println("An error occurred with loading " + name);
        }
    }

    public void writeContents(String contents) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(contents);
        } catch (IOException ioe) {
            System.out.println(
                    "An error occurred with writing to " + file.getName());
        }
    }
}
