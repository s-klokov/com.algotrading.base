package com.algotrading.base.helpers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOHelper {

    private IOHelper(){
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

    /**
     * Получить объект типа {@code BufferedReader} с кодировкой UTF-8.
     *
     * @param file файл
     * @return объект типа {@code BufferedReader}
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static BufferedReader getBufferedReader(final File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    /**
     * Получить объект типа {@code BufferedReader} с кодировкой UTF-8.
     *
     * @param fileName имя файла
     * @return объект типа {@code BufferedReader}
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static BufferedReader getBufferedReader(final String fileName) throws IOException {
        return getBufferedReader(new File(fileName));
    }

    /**
     * Прочитать из файла все непустые строки и применить к каждой из них метод trim.
     *
     * @param fileName имя файла
     * @return список строк
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static List<String> readNonEmptyTrimmedLines(final String fileName) throws IOException {
        return readNonEmptyTrimmedLines(new File(fileName));
    }

    /**
     * Прочитать из файла все непустые строки и применить к каждой из них метод trim.
     *
     * @param file файл
     * @return список строк
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static List<String> readNonEmptyTrimmedLines(final File file) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader br = getBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * Получить ответ от web-сервера на запрос GET.
     *
     * @param request запрос к web-серверу
     * @param timeout таймаут в миллисекундах
     * @return список полученных строк (пустой в случае ошибок)
     */
    public static List<String> getResponse(final String request, final int timeout) {
        final List<String> lines = new ArrayList<>();
        try {
            final URL url = new URL(request);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.connect();
            final int code = con.getResponseCode();
            if (code >= 400) {
                return lines;
            }
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), 65536)) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (final IOException e) {
            lines.clear();
            return lines;
        }
    }
}
