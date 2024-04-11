package com.algotrading.base.core;

import com.algotrading.base.core.values.DoubleValue;
import com.algotrading.base.core.values.IntValue;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Как компромисс между производительностью и удобством человека, метки времени t представлены в виде
 * примитивов типа long формата "yyyymmddHHMMSSmil", где "mil" означает миллиисекунды.
 * Предполагается, что значения yyyy, mm, dd, HH, MM, SS, mil принадлежат корректным диапазонам.
 * Зимнее/летнее время и временные зоны в расчёт не берутся.
 */
public class TimeCodes {

    private TimeCodes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Получить метку времени t.
     *
     * @param year  год в формате YYYY
     * @param month месяц (1..12)
     * @param day   день (1..31)
     * @param hour  час (0..23)
     * @param min   минута (0..59)
     * @param sec   секунда (0..59)
     * @param ms    миллисекунды (0..999)
     * @return метка времени
     */
    public static long t(final int year,
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
     * Получить метку времени t.
     *
     * @param c календарь
     * @return метка времени
     */
    public static long t(final Calendar c) {
        return t(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
    }

    /**
     * Получить метку времени t.
     *
     * @param localDateTime объект типа {@link LocalDateTime}
     * @return метка времени
     */
    public static long t(final LocalDateTime localDateTime) {
        return t(localDateTime.getYear(),
                localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHour(),
                localDateTime.getMinute(),
                localDateTime.getSecond(),
                localDateTime.getNano() / 1_000_000);
    }

    /**
     * Получить метку времени t.
     *
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   время в формате HHMMSS
     * @param ms       миллисекунды (0..999)
     * @return метка времени
     */
    public static long t(final int yyyymmdd, final int hhmmss, final int ms) {
        return (yyyymmdd * 1_00_00_00L + hhmmss) * 1000L + ms;
    }

    /**
     * Получить метку времени t.
     *
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   время в формате HHMMSS
     * @return метка времени
     */
    public static long t(final int yyyymmdd, final int hhmmss) {
        return t(yyyymmdd, hhmmss, 0);
    }

    /**
     * Получить метку времени t.
     * <p>
     * Метод может выбросить исключение DateTimeParseException.
     *
     * @param tAsString строка, задающая момент времени
     * @param formatter форматтер для парсинга, например, DateTimeFormatter.ofPattern("dd.MM.yyyy H:m:s.SSS")
     *                  или DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")
     * @return метка времени
     */
    public static long t(final String tAsString, final DateTimeFormatter formatter) {
        return t(LocalDateTime.parse(tAsString, formatter));
    }

    /**
     * Получить метку времени t.
     *
     * @param yyyymmdd дата в формате "YYYYMMDD"
     * @param hhmmssms время в формате"HHMMSS" as "HHMMSS.MIL"
     * @return метка времени
     */
    public static long t(final String yyyymmdd, final String hhmmssms) {
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
     * @return количество миллисекунд между t и полуночью 1 января 1970 г.
     */
    @SuppressWarnings("MagicConstant")
    public static long timeInMillis(long t) {
        final int ms = (int) (t % 1000L);
        t /= 1000L;
        final int sec = (int) (t % 100L);
        t /= 100L;
        final int min = (int) (t % 100L);
        t /= 100L;
        final int hour = (int) (t % 100L);
        t /= 100L;
        final int day = (int) (t % 100L);
        t /= 100L;
        final int month = (int) (t % 100L);
        t /= 100L;
        final int year = (int) t;
        final Calendar c = new GregorianCalendar(year, month - 1, day, hour, min, sec);
        return c.getTimeInMillis() + ms;
    }

    /**
     * Преобразовать метку времени в объект типа {@link LocalDateTime}.
     *
     * @param t метка времени
     * @return объект типа {@link LocalDateTime}
     */
    public static LocalDateTime localDateTime(long t) {
        final int ms = (int) (t % 1000L);
        t /= 1000L;
        final int sec = (int) (t % 100L);
        t /= 100L;
        final int min = (int) (t % 100L);
        t /= 100L;
        final int hour = (int) (t % 100L);
        t /= 100L;
        final int day = (int) (t % 100L);
        t /= 100L;
        final int month = (int) (t % 100L);
        t /= 100L;
        final int year = (int) t;
        return LocalDateTime.of(year, month, day, hour, min, sec, ms * 1_000_000);
    }

    /**
     * Вычислить длительность промежутка времени между двумя метками времени.
     *
     * @param tStart метка времени начала промежутка
     * @param tEnd   метка времени конца промежутка
     * @return длительность промежутка времени в миллисекундах
     */
    public static long duration(long tStart, long tEnd) {
        if (tStart == tEnd) {
            return 0;
        } else if (tStart / 1000000000L == tEnd / 1000000000L) { // same date
            long d = (tEnd % 1000L) - (tStart % 1000L); // ms
            tStart /= 1000L;
            tEnd /= 1000L;
            if (tStart == tEnd) return d;
            d += ((tEnd % 100L) - (tStart % 100L)) * 1000L; // sec
            tStart /= 100L;
            tEnd /= 100L;
            if (tStart == tEnd) return d;
            d += ((tEnd % 100L) - (tStart % 100L)) * 60L * 1000L; // min
            tStart /= 100L;
            tEnd /= 100L;
            if (tStart == tEnd) return d;
            d += ((tEnd % 100L) - (tStart % 100L)) * 60L * 60L * 1000L; // hour
            return d;
        } else { // different dates
            return timeInMillis(tEnd) - timeInMillis(tStart);
        }
    }

    /**
     * @param t метка времени
     * @return год YYYY
     */
    public static int year(final long t) {
        return (int) (t / 10000000000000L);
    }

    /**
     * @param t метка времени
     * @return месяц (1..12)
     */
    public static int month(final long t) {
        return (int) ((t / 100000000000L) % 100L);
    }

    /**
     * @param t метка времени
     * @return день (1..31)
     */
    public static int day(final long t) {
        return (int) ((t / 1000000000L) % 100L);
    }

    /**
     * @param t метка времени
     * @return час (0..23)
     */
    public static int hour(final long t) {
        return (int) ((t / 10000000L) % 100L);
    }

    /**
     * @param t метка времени
     * @return минута (0..59)
     */
    public static int min(final long t) {
        return (int) ((t / 100000L) % 100L);
    }

    /**
     * @param t метка времени
     * @return секунда (0..59)
     */
    public static int sec(final long t) {
        return (int) ((t / 1000L) % 100L);
    }

    /**
     * @param t метка времени
     * @return миллисекунда (0..999)
     */
    public static int ms(final long t) {
        return (int) (t % 1000L);
    }

    /**
     * @param t метка времени
     * @return дата в формате yyyymmdd
     */
    public static int yyyymmdd(final long t) {
        return (int) (t / 1000000000L);
    }

    /**
     * @param t метка времени
     * @return время в формате hhmmss
     */
    public static int hhmmss(final long t) {
        return (int) ((t / 1000L) % 1000000L);
    }

    /**
     * @param t метка времени
     * @return время в формате hhmm
     */
    public static int hhmm(final long t) {
        return (int) ((t / 100000L) % 10000L);
    }

    /**
     * @param t метка времени
     * @return день недели
     */
    public static DayOfWeek dayOfWeek(final long t) {
        return LocalDate.of(year(t), month(t), day(t)).getDayOfWeek();
    }

    /**
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmss   time в формате HHMMSS
     * @return метка времени
     */
    public static long t(final IntValue yyyymmdd, final IntValue hhmmss) {
        return yyyymmdd.get() * 1_000000_000L + hhmmss.get() * 1000L;
    }

    /**
     * @param yyyymmdd дата в формате YYYYMMDD
     * @param hhmmssms время в формате HHMMSS.MIL
     * @return метка времени
     */
    public static long t(final IntValue yyyymmdd, final DoubleValue hhmmssms) {
        return yyyymmdd.get() * 1_000000_000L + Math.round(hhmmssms.get() * 1000);
    }

    /**
     * @param t метка времени
     * @return строковое представление метки времени
     */
    public static String asString(final long t) {
        return String.format("%4d-%02d-%02d %02d:%02d:%02d.%03d",
                year(t), month(t), day(t),
                hour(t), min(t), sec(t), ms(t));
    }

    /**
     * @param t метка времени
     * @return строковое представление метки времени с точностью до минут
     */
    public static String asStringHHMM(final long t) {
        return String.format("%4d-%02d-%02d %02d:%02d",
                year(t), month(t), day(t),
                hour(t), min(t));
    }

    /**
     * @param t метка времени
     * @return строковое представление метки времени с точностью до секунд
     */
    public static String asStringHHMMSS(final long t) {
        return String.format("%4d-%02d-%02d %02d:%02d:%02d",
                year(t), month(t), day(t),
                hour(t), min(t), sec(t));
    }

    /**
     * Получить метку времени начала таймфрейма.
     *
     * @param t      метка времени
     * @param period период времени (делитель 60 для минут и секунд, делитель 24 для часов)
     * @param unit   единица измерения времени ({@link TimeUnit#HOURS}, {@link TimeUnit#MINUTES},
     *               {@link TimeUnit#SECONDS}) или {@link TimeUnit#DAYS}
     * @return метка времени начала таймфрейма
     */
    public static long getTimeFrameStart(final long t, final int period, final TimeUnit unit) {
        switch (unit) {
            case DAYS:
                if (period != 1) {
                    throw new IllegalArgumentException("period=" + period + "day");
                }
                return t - t % 1_00_00_00_000L;
            case HOURS:
                if (period < 1 || (24 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "hour");
                }
                final long yymmdd000000000 = t - t % 1_00_00_00_000L;
                int hour = hour(t);
                hour = (hour / period) * period;
                return yymmdd000000000 + hour * 1_00_00_000L;
            case MINUTES:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "min");
                }
                final long yymmddhh0000000 = t - t % 1_00_00_000L;
                int min = min(t);
                min = (min / period) * period;
                return yymmddhh0000000 + min * 1_00_000L;
            case SECONDS:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "sec");
                }
                final long yymmddhhmm00000 = t - t % 1_00_000L;
                int sec = sec(t);
                sec = (sec / period) * period;
                return yymmddhhmm00000 + sec * 1_000L;
            default:
                throw new IllegalArgumentException("Illegal time unit " + unit);
        }
    }

    /**
     * Получить метку времени конца таймфрейма.
     *
     * @param t      метка времени
     * @param period период времени (делитель 60 для минут и секунд, делитель 24 для часов)
     * @param unit   единица измерения времени ({@link TimeUnit#HOURS}, {@link TimeUnit#MINUTES}
     *               или {@link TimeUnit#SECONDS})
     * @return метка времени конца таймфрейма
     */
    public static long getTimeFrameEnd(final long t, final int period, final TimeUnit unit) {
        switch (unit) {
            case DAYS:
                if (period != 1) {
                    throw new IllegalArgumentException("period=" + period + "day");
                }
                return t - t % 1_00_00_00_000L;
            case HOURS:
                if (period < 1 || (24 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "hour");
                }
                final long yymmdd000000000 = t - t % 1_00_00_00_000L;
                int hour = hour(t);
                hour = (hour / period) * period;
                return yymmdd000000000 + (hour + period - 1) * 1_00_00_000L;
            case MINUTES:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "min");
                }
                final long yymmddhh0000000 = t - t % 1_00_00_000L;
                int min = min(t);
                min = (min / period) * period;
                return yymmddhh0000000 + (min + period - 1) * 1_00_000L;
            case SECONDS:
                if (period < 1 || (60 % period != 0)) {
                    throw new IllegalArgumentException("period=" + period + "sec");
                }
                final long yymmddhhmm00000 = t - t % 1_00_000L;
                int sec = sec(t);
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
     * @param t метка времени для UTC
     * @return unix-время
     */
    public static long getUnixTime(final long t) {
        return getUnixTime(yyyymmdd(t), hhmmss(t));
    }

    /**
     * Получить по unix-времени метку времени для UTC.
     *
     * @param unixTime unix-время
     * @return метка временя для UTC
     */
    public static long fromUnixTime(final long unixTime) {
        return t(LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.UTC));
    }
}
