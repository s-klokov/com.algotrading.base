package com.algotrading.base.util;

import com.algotrading.base.helpers.IOHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Чтение конфигурационного файла, содержащего JSON-объект.
 * <p>
 * Строки, начинающиеся с пробельных символов, рассматриваются как комментарии и игнорируются.
 */
public class JSONConfig {

    /**
     * Прочитать конфигурационный файл, удалить из него комментарии и преобразовать в JSON-объект.
     *
     * @param file файл
     * @return JSON-объект
     * @throws IOException        если произошла ошибка ввода-вывода
     * @throws ParseException     если не удалось выполнить парсинг
     * @throws ClassCastException если результат не является JSON-объектом
     */
    public static JSONAware read(final File file) throws IOException, ParseException, ClassCastException {
        final StringBuilder sb = new StringBuilder();
        try (final BufferedReader br = IOHelper.getBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.stripLeading().startsWith("//")) {
                    continue;
                }
                sb.append(line);
            }
        }
        return (JSONAware) new JSONParser().parse(sb.toString());
    }

    public static boolean getBoolean(final JSONObject json, final String key) {
        return (boolean) json.get(key);
    }

    public static long getLong(final JSONObject json, final String key) {
        return (long) json.get(key);
    }

    public static int getInt(final JSONObject json, final String key) {
        return (int) getLong(json, key);
    }

    public static double getDouble(final JSONObject json, final String key) {
        final Object o = json.get(key);
        if (o instanceof Long) {
            return (double) ((Long) o);
        } else {
            return (double) o;
        }
    }

    public static String getString(final JSONObject json, final String key) {
        return (String) json.get(key);
    }

    public static String getStringNonNull(final JSONObject json, final String key) {
        return Objects.requireNonNull((String) json.get(key));
    }

    public static JSONObject getJSONObject(final JSONObject json, final String key) {
        return (JSONObject) json.get(key);
    }

    public static JSONArray getJSONArray(final JSONObject json, final String key) {
        return (JSONArray) json.get(key);
    }

    public static boolean getOrDefault(final JSONObject json, final String key, final boolean defaultValue) {
        try {
            return (boolean) json.get(key);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static long getOrDefault(final JSONObject json, final String key, final long defaultValue) {
        try {
            return (long) json.get(key);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    public static String getOrDefault(final JSONObject json, final String key, final String defaultValue) {
        try {
            final String value = (String) json.get(key);
            return (value != null) ? value : defaultValue;
        } catch (final Exception e) {
            return defaultValue;
        }
    }
}
