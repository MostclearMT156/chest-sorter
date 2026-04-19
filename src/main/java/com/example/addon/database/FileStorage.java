package com.example.addon.database;


import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Утилита для чтения/записи файлов.
 */
public class FileStorage {

    public static String readFile(File file) {
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
        )) {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeFile(File file, String data) {
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
        )) {
            writer.write(data);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
