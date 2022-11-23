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
        return (FolderCandleDataLocator) super.from(from);
    }

    @Override
    public FolderCandleDataLocator from(final LocalDate localDate) {
        return (FolderCandleDataLocator) super.from(localDate);
    }

    @Override
    public FolderCandleDataLocator till(final int till) {
        return (FolderCandleDataLocator) super.till(till);
    }

    @Override
    public FolderCandleDataLocator till(final LocalDate localDate) {
        return (FolderCandleDataLocator) super.till(localDate);
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
