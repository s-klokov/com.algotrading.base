package com.algotrading.base.core.marketdata;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FolderCandleDataLocator extends CandleDataLocator {

    private final String[] folders;
    private final String filenameExtension;

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

    public FolderCandleDataLocator(final String filenameExtension, final String... folders) {
        this.filenameExtension = filenameExtension;
        this.folders = folders;
    }

    @Override
    public List<File> getFiles(final String secCode) {
        final List<File> files = new ArrayList<>();
        for (final String folder : folders) {
            final File file = new File(folder, secCode + "." + filenameExtension);
            if (file.exists() && file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }
}
