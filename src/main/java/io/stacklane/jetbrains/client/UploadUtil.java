package io.stacklane.jetbrains.client;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
final class UploadUtil {
    private static Path DIR = null;

    /**
     * No need to synchronize, low volume / single user.
     */
    private static Path getSnapshotDir() throws IOException{
        if (DIR == null) {
            DIR = Files.createTempDirectory(UploadClientSettings.TEMP_DIR_PREFIX);
        }

        return DIR;
    }

    /**
     * @return Number of files that will be found/collected during a call to {@link #zip}.
     */
    public static long getZippableFileTotal(Path projectDir, Predicate<Path> filter) throws IOException{
        final long toUpdate = Files.walk(projectDir)
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> !UploadClientSettings.isIgnored(path))
                .filter(filter)
                .count();
        return toUpdate;
    }

    public static Path zip(Path projectDir, Predicate<Path> filter) throws IOException {
        final Path zipFile = Files.createTempFile(getSnapshotDir(), "Snapshot", ".zip");

        final AtomicReference<IOException> io = new AtomicReference<>();
        final AtomicInteger count = new AtomicInteger();
        final AtomicLong bytes = new AtomicLong();
        final ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile));

        try {
            Files.walk(projectDir)
                .filter(path -> !Files.isDirectory(path))
                .filter(path -> !UploadClientSettings.isIgnored(path))
                .filter(filter)
                .forEach(path -> {
                    if (io.get() != null) return; // TODO revise error handling

                    final ZipEntry zipEntry = new ZipEntry(projectDir.relativize(path).toString());

                    try {
                        count.getAndIncrement();
                        if (count.get() > UploadClientSettings.MAX_FILES){
                            io.set(new IOException("Exceeded maximum project files (" + UploadClientSettings.MAX_FILES + " files)"));
                            return;
                        }
                        bytes.getAndAdd(path.toFile().length());
                        if (bytes.get() > UploadClientSettings.MAX_BYTES){
                            io.set(new IOException("Exceeded maximum project size (" + UploadClientSettings.MAX_BYTES + " bytes)"));
                            return;
                        }
                        zs.putNextEntry(zipEntry);
                        Files.copy(path, zs);
                        zs.closeEntry();
                    } catch (IOException e) {
                        io.set(e);
                    }
                });
        } finally {

            try {
                zs.close(); // as a filtering stream, this closes underlying file stream too
            } catch (Throwable ignore){}

        }

        if (io.get() != null) throw io.get();

        return zipFile;
    }

}
