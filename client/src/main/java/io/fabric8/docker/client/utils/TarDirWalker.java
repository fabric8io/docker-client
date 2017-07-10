package io.fabric8.docker.client.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class TarDirWalker extends SimpleFileVisitor<Path> {
    private Path basePath;
    private TarArchiveOutputStream tarArchiveOutputStream;

    public TarDirWalker(Path basePath, TarArchiveOutputStream tarArchiveOutputStream) {
        this.basePath = basePath;
        this.tarArchiveOutputStream = tarArchiveOutputStream;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (!dir.equals(basePath)) {
            tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(basePath.relativize(dir).toFile()));
            tarArchiveOutputStream.closeArchiveEntry();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        TarArchiveEntry tarEntry = new TarArchiveEntry(basePath.relativize(file).toFile());
        tarEntry.setSize(attrs.size());
        if (file.toFile().canExecute()) {
            tarEntry.setMode(tarEntry.getMode() | 0755);
        }
        ArchiveUtil.putTarEntry(tarArchiveOutputStream, tarEntry, file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        tarArchiveOutputStream.close();
        throw exc;
    }
}
