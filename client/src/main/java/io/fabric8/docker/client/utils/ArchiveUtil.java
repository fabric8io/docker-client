package io.fabric8.docker.client.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class ArchiveUtil {

    public static void putTarEntry(TarArchiveOutputStream tarArchiveOutputStream, TarArchiveEntry tarArchiveEntry,
        Path inputPath) throws IOException {
        tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
        Files.copy(inputPath, tarArchiveOutputStream);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    public static TarArchiveOutputStream buildTarStream(File outputPath) throws IOException {
        FileOutputStream fout = new FileOutputStream(outputPath);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(bout);
        return new TarArchiveOutputStream(bzout);
    }

    public static void tar(Path inputPath, Path outputPath) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new FileNotFoundException("File not found " + inputPath);
        }

        try (TarArchiveOutputStream tarArchiveOutputStream = buildTarStream(outputPath.toFile())) {
            if (!Files.isDirectory(inputPath)) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(inputPath.toFile());
                if (inputPath.toFile().canExecute()) {
                    tarEntry.setMode(tarEntry.getMode() | 0755);
                }
                putTarEntry(tarArchiveOutputStream, tarEntry, inputPath);
            } else {
                Files.walkFileTree(inputPath,
                    new TarDirWalker(inputPath, tarArchiveOutputStream));
            }
            tarArchiveOutputStream.flush();
        }
    }
}
