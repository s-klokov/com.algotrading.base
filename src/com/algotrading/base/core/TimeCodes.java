package com.algotrading.base.core;

import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Как компромисс между производительностью и удобством человека, временные метки timeCodes представлены в виде
 * примитивов типа long формата "yyyymmddHHMMSSmil", где "mil" означает миллиисекунды.
 * Предполагается, что значения yyyy, mm, dd, HH, MM, SS, mil принадлежат корректным диапазонам.
 * Зимнее/летнее время и временные зоны в расчёт не берутся.
 */
public class TimeCodes {

    private TimeCodes() {
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param year  год в формате YYYY
     * @param month месяц (1..12)
     * @param day   день (1..31)
     * @param hour  час (0..23)
     * @param min   минута (0..59)
     * @param sec   секунда (0..59)
     * @param ms    миллисекунды (0..999)
     * @return временная метка
     */
    public static long timeCode(final int year,
                                final int month,
                                final int day,
                                final int hour,
                                final int min,
                                final int sec,
                                final int ms) {
        long t = year;
        t = t * 100L + month;
        t = t * 100L + day;
        t = t * 100L + hour;
        t = t * 100L + min;
        t = t * 100L + sec;
        t = t * 1000L + ms;
        return t;
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param c календарь
     * @return временная метка
     */
    public static long timeCode(final Calendar c) {
        return timeCode(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param localDateTime объект типа {@link LocalDateTime}
     * @return временная метка
     */
    public static long timeCode(final LocalDateTime localDateTime) {
        return timeCode(localDateTime.getYear(),
                localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHour(),
                localDateTime.getMinute(),
                localDateTime.getSecond(),
                localDateTime.getNano() / 1_000_000);
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   время в формате HHMMSS
     * @param ms       миллисекунды (0..999)
     * @return временная метка
     */
    public static long timeCode(final int yyyymmdd, final int hhmmss, final int ms) {
        return (yyyymmdd * 1_00_00_00L + hhmmss) * 1000L + ms;
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   время в формате HHMMSS
     * @return временная метка
     */
    public static long timeCode(final int yyyymmdd, final int hhmmss) {
        return timeCode(yyyymmdd, hhmmss, 0);
    }

    /**
     * Получить временную метку timeCode.
     * <p>
     * Метод может выбросить исключение DateTimeParseException.
     *
     * @param timeCodeString строка, задающая момент времени
     * @param formatter      форматтер для парсинга, например, DateTimeFormatter.ofPattern("dd.MM.yyyy H:m:s.SSS")
     *                       или DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")
     * @return временная метка
     */
    public static long timeCode(final String timeCodeString, final DateTimeFormatter formatter) {
        return timeCode(LocalDateTime.parse(timeCodeString, formatter));
    }

    /**
     * Получить временную метку timeCode.
     *
     * @param yyyymmdd дата в формате "YYYYMMDD"
     * @param hhmmssms время в формате"HHMMSS" as "HHMMSS.MIL"
     * @return временная метка
     */
    public static long timeCode(final String yyyymmdd, final String hhmmssms) {
        int i;
        int date = 0;
        for (i = 0; i < yyyymmdd.length(); i++) {
            final char ch = yyyymmdd.charAt(i);
            if ('0' <= ch && ch <= '9') {
                date *= 10;
                date += (ch - '0');
            }
        }
        int time = 0;
        boolean hasMillis = false;
        for (i = 0; i < hhmmssms.length(); i++) {
            final char ch = hhmmssms.charAt(i);
            if ('0' <= ch && ch <= '9') {
                time *= 10;
                time += (ch - '0');
            } else if (ch == '.' || ch == ',') {
                hasMillis = true;
                break;
            }
        }
        if (hasMillis) {
            int msDigits = 0;
            for (; i < hhmmssms.length() && msDigits < 3; i++) {
                final char ch = hhmmssms.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    time *= 10;
                    time += (ch - '0');
                    msDigits++;
                }
            }
            while (msDigits < 3) {
                time *= 10;
                msDigits++;
            }
        } else {
            time *= 1000;
        }
        return date * 1_000000_000L + time;
    }

    /**
     * @return количество миллисекунд между timeCode и полуночью 1 января 1970 г.
     */
    @SuppressWarnings("MagicConstant")
    public static long timeInMillis(long timeCode) {
        final int ms = (int) (timeCode % 1000L);
        timeCode /= 1000L;
        final int sec = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int min = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int hour = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int day = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int month = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int year = (int) timeCode;
        final Calendar c = new GregorianCalendar(year, month - 1, day, hour, min, sec);
        return c.getTimeInMillis() + ms;
    }

    /**
     * Преобразовать временную метку в объект типа {@link LocalDateTime}.
     *
     * @param timeCode временная метка
     * @return в объект типа {@link LocalDateTime}
     */
    public static LocalDateTime localDateTime(long timeCode) {
        final int ms = (int) (timeCode % 1000L);
        timeCode /= 1000L;
        final int sec = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int min = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int hour = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int day = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int month = (int) (timeCode % 100L);
        timeCode /= 100L;
        final int year = (int) timeCode;
        return LocalDateTime.of(year, month, day, hour, min, sec, ms * 1_000_000);
    }

    /**
     * Вычислить длительность промежутка времени между двумя временными метками.
     *
     * @param startTimeCode временная метка начала промежутка
     * @param endTimeCode   временная метка конца промежутка
     * @return длительность промежутка времени в миллисекундах
     */
    public static long duration(long startTimeCode, long endTimeCode) {
        if (startTimeCode == endTimeCode) {
            return 0;
        } else if (startTimeCode / 1000000000L == endTimeCode / 1000000000L) { // same date
            long d = (endTimeCode % 1000L) - (startTimeCode % 1000L); // ms
            startTimeCode /= 1000L;
            endTimeCode /= 1000L;
            if (startTimeCode == endTimeCode) return d;
            d += ((endTimeCode % 100L) - (startTimeCode % 100L)) * 1000L; // sec
            startTimeCode /= 100L;
            endTimeCode /= 100L;
            if (startTimeCode == endTimeCode) return d;
            d += ((endTimeCode % 100L) - (startTimeCode % 100L)) * 60L * 1000L; // min
            startTimeCode /= 100L;
            endTimeCode /= 100L;
            if (startTimeCode == endTimeCode) return d;
            d += ((endTimeCode % 100L) - (startTimeCode % 100L)) * 60L * 60L * 1000L; // hour
            return d;
        } else { // different dates
            return timeInMillis(endTimeCode) - timeInMillis(startTimeCode);
        }
    }

    /**
     * @param timeCode временная метка
     * @return год YYYY
     */
    public static int year(final long timeCode) {
        return (int) (timeCode / 10000000000000L);
    }

    /**
     * @param timeCode временная метка
     * @return месяц (1..12)
     */
    public static int month(final long timeCode) {
        return (int) ((timeCode / 100000000000L) % 100L);
    }

    /**
     * @param timeCode временная метка
     * @return день (1..31)
     */
    public static int day(final long timeCode) {
        return (int) ((timeCode / 1000000000L) % 100L);
    }

    /**
     * @param timeCode временная метка
     * @return час (0..23)
     */
    public static int hour(final long timeCode) {
        return (int) ((timeCode / 10000000L) % 100L);
    }

    /**
     * @param timeCode временная метка
     * @return минута (0..59)
     */
    public static int min(final long timeCode) {
        return (int) ((timeCode / 100000L) % 100L);
    }

    /**
     * @param timeCode временная метка
     * @return секунда (0..59)
     */
    public static int sec(final long timeCode) {
        return (int) ((timeCode / 1000L) % 100L);
    }

    /**
     * @param timeCode временная метка
     * @return миллисекунда (0..999)
     */
    public static int ms(final long timeCode) {
        return (int) (timeCode % 1000L);
    }

    /**
     * @param timeCode временная метка
     * @return дата в формате yyyymmdd
     */
    public static int yyyymmdd(final long timeCode) {
        return (int) (timeCode / 1000000000L);
    }

    /**
     * @param timeCode временная метка
     * @return время в формате hhmmss
     */
    public static int hhmmss(final long timeCode) {
        return (int) ((timeCode / 1000L) % 1000000L);
    }

    /**
     * @param timeCode временная метка
     * @return время в формате hhmm
     */
    public static int hhmm(final long timeCode) {
        return (int) ((timeCode / 100000L) % 10000L);
    }

    /**
     * @param timeCode временная метка
     * @return день недели
     */
    public static DayOfWeek dayOfWeek(final long timeCode) {
        return LocalDate.of(year(timeCode), month(timeCode), day(timeCode)).getDayOfWeek();
    }

    /**
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   time в формате HHMMSS
     * @return timeCode
     */
    public static long timeCode(final IntValue yyyymmdd, final IntValue hhmmss) {
        return yyyymmdd.get() * 1_000000_000L + hhmmss.get() * 1000L;
    }

    /**
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmssms время в формате HHMMSS.MIL
     * @return timeCode
     */
    public static long timeCode(final IntValue yyyymmdd, final DoubleValue hhmmssms) {
        return yyyymmdd.get() * 1_000000_000L + Math.round(hhmmssms.get() * 1000);
    }

    /**
     * @param timeCode временная метка
     * @return строковое представление временной метки
     */
    public static String timeCodeString(final long timeCode) {
        return String.format("%4d-%02d-%02d %02d:%02d:%02d.%03d",
                year(timeCode), month(timeCode), day(timeCode),
                hour(timeCode), min(timeCode), sec(timeCode), ms(timeCode));
    }

    /**
     * @param timeCode временная метка
     * @return строковое представление временной метки
     */
    public static String timeCodeStringHHMM(final long timeCode) {
        return String.format("%4d-%02d-%02d %02d:%02d",
                year(timeCode), month(timeCode), day(timeCode),
                hour(timeCode), min(timeCode));
    }

    /**
     * @param timeCode временная метка
     * @return строковое представление временной метки
     */
    public static String timeCodeStringHHMMSS(final long timeCode) {
        return String.format("%4d-%02d-%02d %02d:%02d:%02d",
                year(timeCode), month(timeCode), day(timeCode),
                hour(timeCode), min(timeCode), sec(timeCode));
    }

    /**
     * Получить временную метку начала таймфрейма.
     *
     * @param timeCode временная метка
     * @param period   период времени (делитель 60 для минут и секунд, делитель 24 для часов)
     * @param unit     единица измерения времени ({@link TimeUnit#HOURS}, {@link TimeUnit#MINUTES},
     *                 {@link TimeUnit#SECONDS}) или {@link TimeUnit#DAYS}
     * @return временная метка начала таймфрейма
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static long getTimeFrameStart(final long timeCode, final int period, final TimeUnit unit) {
        switch (unit) {
            case DAYS:
                if (period != 1) {
                    throw new IllegalArgumentException("period=" + period + "day");
                }
                return timeCode - timeCode % 1_00_00_00_000L;
            case HOURS:
                if (period < 1 || (24 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "hour");
                }
                final long yymmdd000000000 = timeCode - timeCode % 1_00_00_00_000L;
                int hour = hour(timeCode);
                hour = (hour / period) * period;
                return yymmdd000000000 + hour * 1_00_00_000L;
            case MINUTES:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "min");
                }
                final long yymmddhh0000000 = timeCode - timeCode % 1_00_00_000L;
                int min = min(timeCode);
                min = (min / period) * period;
                return yymmddhh0000000 + min * 1_00_000L;
            case SECONDS:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "sec");
                }
                final long yymmddhhmm00000 = timeCode - timeCode % 1_00_000L;
                int sec = sec(timeCode);
                sec = (sec / period) * period;
                return yymmddhhmm00000 + sec * 1_000L;
            default:
                throw new IllegalArgumentException("Illegal time unit " + unit);
        }
    }

    /**
     * Получить временную метку конца таймфрейма.
     *
     * @param timeCode временная метка
     * @param period   период времени (делитель 60 для минут и секунд, делитель 24 для часов)
     * @param unit     единица измерения времени ({@link TimeUnit#HOURS}, {@link TimeUnit#MINUTES}
     *                 или {@link TimeUnit#SECONDS})
     * @return временная метка конца таймфрейма
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static long getTimeFrameEnd(final long timeCode, final int period, final TimeUnit unit) {
        switch (unit) {
            case DAYS:
                if (period != 1) {
                    throw new IllegalArgumentException("period=" + period + "day");
                }
                return timeCode - timeCode % 1_00_00_00_000L;
            case HOURS:
                if (period < 1 || (24 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "hour");
                }
                final long yymmdd000000000 = timeCode - timeCode % 1_00_00_00_000L;
                int hour = hour(timeCode);
                hour = (hour / period) * period;
                return yymmdd000000000 + (hour + period - 1) * 1_00_00_000L;
            case MINUTES:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "min");
                }
                final long yymmddhh0000000 = timeCode - timeCode % 1_00_00_000L;
                int min = min(timeCode);
                min = (min / period) * period;
                return yymmddhh0000000 + (min + period - 1) * 1_00_000L;
            case SECONDS:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "sec");
                }
                final long yymmddhhmm00000 = timeCode - timeCode % 1_00_000L;
                int sec = sec(timeCode);
                sec = (sec / period) * period;
                return yymmddhhmm00000 + (sec + period - 1) * 1_000L;
            default:
                throw new IllegalArgumentException("Illegal time unit " + unit);
        }
    }

    /**
     * Получить unix-время по UTC-времени в формате (yyyymmdd, hhmmss).
     *
     * @param yyyymmdd дата
     * @param hhmmss   время
     * @return unix-время
     */
    public static long getUnixTime(final int yyyymmdd, final int hhmmss) {
        int t = yyyymmdd;
        final int day = t % 100;
        t /= 100;
        final int month = t % 100;
        t /= 100;
        final int year = t;
        t = hhmmss;
        final int sec = t % 100;
        t /= 100;
        final int min = t % 100;
        t /= 100;
        final int hour = t;
        return ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("Z")).toEpochSecond();
    }

    /**
     * Получить unix-время по метке времени для UTC.
     *
     * @param timeCode метка времени для UTC
     * @return unix-время
     */
    public static long getUnixTime(final long timeCode) {
        return getUnixTime(yyyymmdd(timeCode), hhmmss(timeCode));
    }

    /**
     * Получить по unix-времени метку времени для UTC.
     *
     * @param unixTime unix-время
     * @return метка временя для UTC
     */
    public static long fromUnixTime(final long unixTime) {
        return timeCode(LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.UTC));
    }
}
