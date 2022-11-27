package com.algotrading.base.core.marketdata.locators;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FolderCandleDataLocator extends CandleDataLocator {

    private final Function<String, String> secCodeToFilenameFunction;
    private final String[] folders;

    public FolderCandleDataLocator(final Function<String, String> secCodeToFilenameFunction, final String... folders) {
        this.secCodeToFilenameFunction = secCodeToFilenameFunction;
        this.folders = folders;
    }

    @Override
    public FolderCandleDataLocator from(final int from) {
        super.from(from);
        return this;
    }

    @Override
    public FolderCandleDataLocator from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public FolderCandleDataLocator till(final int till) {
        super.till(till);
        return this;
    }

    @Override
    public FolderCandleDataLocator till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    @Override
    public List<File> getFiles(final String secCode) {
        final List<File> files = new ArrayList<>();
        for (final String folder : folders) {
            final File file = new File(folder, secCodeToFilenameFunction.apply(secCode));
            if (file.exists() && file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }
}
