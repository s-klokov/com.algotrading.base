package com.algotrading.base.helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOHelper {

    private IOHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * По заданному файлу получить поток для записи.
     * Если файл является zip-файлом, то поток будет организован таким образом, чтобы внутри
     * архива был соответствующий csv-файл.
     *
     * @param file файл
     * @return поток для записи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static PrintStream getPrintStream(final File file) throws IOException {
        final String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".zip")) {
            final ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            zipOut.putNextEntry(new ZipEntry(fileName.substring(0, fileName.length() - 4) + ".csv"));
            return new PrintStream(zipOut, false, StandardCharsets.UTF_8);
        } else {
            return new PrintStream(file, StandardCharsets.UTF_8);
        }
    }

    /**
     * По заданному имени файла получить поток для записи.
     * Если файл является zip-файлом, то поток будет организован таким образом, чтобы внутри
     * архива был соответствующий csv-файл.
     *
     * @param fileName имя файла
     * @return поток для записи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static PrintStream getPrintStream(final String fileName) throws IOException {
        return getPrintStream(new File(fileName));
    }
}
