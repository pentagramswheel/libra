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

    /**
     * Constructs the handler's file, if one does not already exist.
     * @param name the name and extension of the file.
     */
    public FileHandler(String name) {
        try {
            file = new File(name);
            if (file.createNewFile()) {
                System.out.println("File created: " + name);
            }
        } catch (IOException ioe) {
            System.out.println("An error occurred with loading " + name);
        }
    }

    /**
     * Writes to the created file.
     * @param contents the contents to write to the file.
     */
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
