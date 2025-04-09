package comp3111.examsystem.tools;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileUtil is a helper class for reading/writing the database from/to txt files.
 */
public class FileUtil {

    /**
     * Writes a string to a txt file.
     * @param content The string to be written.
     * @param fileName The file name of the file.
     * @return true if successful, false otherwise.
     */
    public static boolean writeTxtFile(String content, File fileName) {
        boolean flag = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Reads data from a txt file line by line.
     * @param fileName The file name of the file to be read.
     * @return A list of strings representing the lines read from the file.
     */
    public static List<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                list.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return list;
    }
}
