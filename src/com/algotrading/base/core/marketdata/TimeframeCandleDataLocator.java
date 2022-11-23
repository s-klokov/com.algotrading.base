package com.algotrading.base.core.marketdata;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeframeCandleDataLocator extends CandleDataLocator {

    private static final Pattern TWO_DATE_PATTERN = Pattern.compile("_2?0?(\\d{6})_2?0?(\\d{6}).*");
    private static final Pattern ONE_DATE_PATTERN = Pattern.compile("_2?0?(\\d{6}).*");

    private final int timeframe;
    private final TimeUnit timeUnit;
    private final String[] folders;
    private boolean isFuturesModeEnabled = true;

    public TimeframeCandleDataLocator(final int timeframe, final TimeUnit timeUnit,
                                      final String... folders) {
        this.timeframe = timeframe;
        this.timeUnit = timeUnit;
        this.folders = folders;
    }

    @Override
    public TimeframeCandleDataLocator from(final int yyyymmdd) {
        super.from(yyyymmdd);
        return this;
    }

    @Override
    public TimeframeCandleDataLocator from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public TimeframeCandleDataLocator till(final int yyyymmdd) {
        super.till(yyyymmdd);
        return this;
    }

    @Override
    public TimeframeCandleDataLocator till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    public TimeframeCandleDataLocator enableFuturesMode(final boolean isFuturesModeEnabled) {
        this.isFuturesModeEnabled = isFuturesModeEnabled;
        return this;
    }

    @Override
    public List<File> getFiles(final String secCode) {
        final List<File> files = new ArrayList<>();

        final String timeframeName = getTimeframeName();
        if (timeframeName == null) {
            return files;
        }

        final Set<String> secFolderNameSet = new HashSet<>();
        final String secPrefix = isFuturesModeEnabled ? Futures.getPrefix(secCode) : secCode;
        secFolderNameSet.add(secCode);
        secFolderNameSet.add(secPrefix);

        for (final String folder : folders) {
            for (final String secFolderName : secFolderNameSet) {
                final File secFolder = new File(folder, secFolderName);
                if (!secFolder.exists() || !secFolder.isDirectory()) {
                    continue;
                }

                final File timeframeFolder = new File(secFolder, timeframeName);
                if (!timeframeFolder.exists() || !timeframeFolder.isDirectory()) {
                    continue;
                }

                final File[] filesList = timeframeFolder.listFiles((dir, name) -> name.startsWith(secCode + '_'));
                if (filesList == null) {
                    continue;
                }

                for (final File file : filesList) {
                    final String suffix = file.getName().substring(secCode.length());
                    if ((isSuffixMatchesOneDatePattern(suffix) || isSuffixMatchesTwoDatePattern(suffix))
                            && !files.contains(file)) {
                        files.add(file);
                    }
                }
            }
        }

        return files;
    }

    private String getTimeframeName() {
        if (timeUnit == TimeUnit.MINUTES) {
            return String.valueOf(timeframe);
        } else if (timeUnit == TimeUnit.SECONDS) {
            return timeframe + "s";
        } else if (timeframe == 1 && timeUnit == TimeUnit.DAYS) {
            return "D";
        } else if (timeframe == 1 && timeUnit == TimeUnit.HOURS) {
            return "60";
        } else {
            return null;
        }
    }

    private boolean isSuffixMatchesOneDatePattern(final String suffix) {
        final Matcher matcher = ONE_DATE_PATTERN.matcher(suffix);
        if (!matcher.matches()) {
            return false;
        }
        final int yyyymmdd = 2000_00_00 + Integer.parseInt(matcher.group(1));
        return betweenFromTill(yyyymmdd);
    }

    private boolean isSuffixMatchesTwoDatePattern(final String suffix) {
        final Matcher matcher = TWO_DATE_PATTERN.matcher(suffix);
        if (!matcher.matches()) {
            return false;
        }
        final int yyyymmdd1 = 2000_00_00 + Integer.parseInt(matcher.group(1));
        final int yyyymmdd2 = 2000_00_00 + Integer.parseInt(matcher.group(2));
        return intersectsWithFromTill(yyyymmdd1, yyyymmdd2);
    }

    private boolean betweenFromTill(final int yyyymmdd) {
        return from <= yyyymmdd && yyyymmdd <= till;
    }

    private boolean intersectsWithFromTill(final int yyyymmdd1, final int yyyymmdd2) {
        return yyyymmdd1 <= till && yyyymmdd2 >= from;
    }
}
