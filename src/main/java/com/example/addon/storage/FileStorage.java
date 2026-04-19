package com.example.addon.storage;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Универсальный модуль для работы с файлами.
 * Используется ChestDatabase и другими подсистемами.
 *
 * Возможности:
 *  - чтение файла в строку
 *  - запись строки в файл
 *  - автоматическое создание директорий
 *  - безопасная обработка ошибок
 */
public class FileStorage {

    /**
     * Читает файл и возвращает его содержимое как строку.
     * Возвращает null, если файл не существует или произошла ошибка.
     */
    public static String readFile(File file) {
        if (file == null || !file.exists()) return null;

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
            System.err.println("[FileStorage] Ошибка чтения файла: " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Записывает строку в файл.
     * Создаёт директории автоматически.
     * Возвращает true при успехе.
     */
    public static boolean writeFile(File file, String data) {
        if (file == null) return false;

        try {
            ensureDirectoryExists(file.getParentFile());

            try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
            )) {
                writer.write(data);
            }

            return true;

        } catch (IOException e) {
            System.err.println("[FileStorage] Ошибка записи файла: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Создаёт директорию, если её нет.
     */
    public static void ensureDirectoryExists(File dir) {
        if (dir == null) return;

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("[FileStorage] Не удалось создать директорию: " + dir.getAbsolutePath());
            }
        }
    }
}
