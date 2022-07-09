package bot.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  Wil Aquino
 * Date:    December 28, 2021
 * Project: Libra
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
            fw.close();
        } catch (IOException ioe) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Could not write to " + file.getName());
        }
    }

    /**
     * Retrievs the first line of the file.
     */
    public String readFirstLine() {
        try {
            Scanner load = new Scanner(file);
            String message = load.nextLine();
            load.close();

            return message;
        } catch (FileNotFoundException ioe) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Could not read " + file.getName());
            return null;
        }
    }

    /** Retrieves all lines of text from the file. */
    public List<String> readContents() {
        List<String> lines = new ArrayList<>();

        try {
            Scanner load = new Scanner(file);
            while (load.hasNext()) {
                lines.add(load.nextLine());
            }
            load.close();

            return lines;
        } catch (FileNotFoundException ioe) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Could not read " + file.getName());
            return null;
        }
    }
}
