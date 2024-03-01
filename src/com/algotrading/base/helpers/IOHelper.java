package com.algotrading.base.helpers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
        try (final BufferedReader br = Files.newBufferedReader(file.toPath())) {
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
            final URI uri;
            try {
                uri = new URI(request);
            } catch (final URISyntaxException e) {
                throw new IOException("Incorrect request string", e);
            }
            final HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
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
