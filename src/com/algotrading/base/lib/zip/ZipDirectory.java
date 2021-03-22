package com.algotrading.base.lib.zip;

import java.io.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Создание zip-архива указанной папки.
 */
public class ZipDirectory {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final byte[] BUFFER = new byte[BUFFER_SIZE];

    public static void main(final String[] args) {
        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println("args[0]: zip-file name");
            System.out.println("args[1]: directory name");
            System.out.println("args[2]: include filename regexp");
            System.out.println("args[3]: exclude filename regexp");
        } else {
            final String zipFileName = args[0];
            final String directoryName = args[1];
            final Pattern includePattern = Pattern.compile(args[2]);
            final Pattern excludePattern = Pattern.compile(args[3]);
            System.out.println("Adding " + directoryName + " to " + zipFileName + "...");
            System.out.println("Include: " + args[2]);
            System.out.println("Exclude: " + args[3]);
            try (final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName), BUFFER_SIZE))) {
                addDirToZipArchive(zos, new File(directoryName), null, includePattern, excludePattern);
                zos.flush();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int addDirToZipArchive(final ZipOutputStream zos,
                                         final File fileToZip,
                                         final String parentDirectoryName,
                                         final Pattern includePattern,
                                         final Pattern excludePattern) throws IOException {
        if (fileToZip == null || !fileToZip.exists()) {
            return 0;
        }

        final String filename = fileToZip.getName();
        String zipEntryName = filename;
        if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
            zipEntryName = parentDirectoryName + "/" + filename;
        }

        if (fileToZip.isDirectory()) {
            System.out.println("+" + zipEntryName);
            int count = 0;
            for (final File file : fileToZip.listFiles()) {
                count += addDirToZipArchive(zos, file, zipEntryName, includePattern, excludePattern);
            }
            if (count == 0) {
                zos.putNextEntry(new ZipEntry(zipEntryName + "/"));
                zos.closeEntry();
                count++;
            }
            return count;
        } else if (fileToZip.isFile()
                   && includePattern.matcher(filename).matches()
                   && !excludePattern.matcher(filename).matches()) {
            System.out.println("   " + zipEntryName);
            try (final FileInputStream fis = new FileInputStream(fileToZip)) {
                zos.putNextEntry(new ZipEntry(zipEntryName));
                int length;
                while ((length = fis.read(BUFFER)) > 0) {
                    zos.write(BUFFER, 0, length);
                }
                zos.closeEntry();
            }
            return 1;
        }
        return 0;
    }
}
