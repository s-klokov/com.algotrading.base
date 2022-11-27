package com.algotrading.base.core.marketdata.locators;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileCandleDataLocator extends CandleDataLocator {

    private final String[] filenames;
    private boolean checkFilesExist = false;

    public FileCandleDataLocator(final String... filenames) {
        this.filenames = filenames;
    }

    @Override
    public FileCandleDataLocator from(final int from) {
        super.from(from);
        return this;
    }

    @Override
    public FileCandleDataLocator from(final LocalDate localDate) {
        super.from(localDate);
        return this;
    }

    @Override
    public FileCandleDataLocator till(final int till) {
        super.till(till);
        return this;
    }

    @Override
    public FileCandleDataLocator till(final LocalDate localDate) {
        super.till(localDate);
        return this;
    }

    public FileCandleDataLocator checkFilesExist(final boolean checkFilesExist) {
        this.checkFilesExist = checkFilesExist;
        return this;
    }

    @Override
    public List<File> getFiles(final String secCode) {
        final List<File> files = new ArrayList<>();
        for (final String filename : filenames) {
            final File file = new File(filename);
            if (file.exists() && file.isFile()) {
                files.add(file);
            } else if (checkFilesExist) {
                final String message = "File not found: " + filename;
                throw new RuntimeException(message, new FileNotFoundException(message));
            }
        }
        return files;
    }
}
