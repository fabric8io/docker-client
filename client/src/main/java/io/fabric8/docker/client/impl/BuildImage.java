/*
 * Copyright (C) 2016 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.docker.client.impl;

import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.DockerIgnorePathMatcher;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.BuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.CpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.CpuQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.CpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.CpusCpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.FromPathInterface;
import io.fabric8.docker.dsl.image.MemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.NoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.PullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.RedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.RemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.RepositoryNameSupressingVerboseOutputNoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingFromPathInterface;
import io.fabric8.docker.dsl.image.SupressingVerboseOutputNoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingFromPathInterface;
import io.fabric8.docker.dsl.image.SwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.UsingDockerFileListenerRedirectingWritingOutputFromPathInterface;
import io.fabric8.docker.dsl.image.UsingListenerRedirectingWritingOutputFromPathInterface;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import static io.fabric8.docker.client.utils.ArchiveUtil.buildTarStream;
import static io.fabric8.docker.client.utils.ArchiveUtil.putTarEntry;

public class BuildImage extends BaseImageOperation implements
        RepositoryNameSupressingVerboseOutputNoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingFromPathInterface<OutputHandle>,
        MemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        RemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        NoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        UsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        CpuQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        BuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        CpusCpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        CpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        SwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        PullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        SupressingVerboseOutputNoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingFromPathInterface<OutputHandle>,
        CpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        UsingListenerRedirectingWritingOutputFromPathInterface<OutputHandle>,
        RedirectingWritingOutputFromPathInterface<OutputHandle>,
        FromPathInterface<OutputHandle> {

    private static final String BUILD_OPERATION = "build";

    private static final String DOCKER_FILE = "dockerfile";
    private static final String REMOTE_DOCKER_FILE = "remote";
    private static final String REPOSITORY_NAME = "t";
    private static final String SUPRESS_VERBOSE_OUT = "q";
    private static final String NOCACHE = "nocache";
    private static final String PULL = "pull";
    private static final String REMOVE_INTERMEDIATE_ON_SUCCESS = "rm";
    private static final String ALWAYS_REMOVE_INTERMEDIATE = "forcerm";
    private static final String MEMORY = "memory";
    private static final String MEMSWAP = "memswap";
    private static final String CPU_SHARES = "cpushares";
    private static final String CPUS = "cpusetcpus";
    private static final String CPU_PERIOD = "cpuperiod";
    private static final String CPU_QUOTA = "cpuquota";
    private static final String BUILD_ARGS = "buildargs";

    private static final String DEFAULT_DOCKERFILE = "Dockerfile";
    private static final String DOCKER_IGNORE = ".dockerignore";
    private static final Charset UTF_8 = Charset.forName("UTF-8");


    private final String dockerFile;
    private final String repositoryName;
    private final String buildArgs;
    private final Boolean noCache;
    private final Boolean pulling;
    private final Boolean alwaysRemoveIntermediate;
    private final Boolean removeIntermediateOnSuccess;
    private final Boolean supressingVerboseOutput;
    private final Integer cpuPeriodMicros;
    private final Integer cpuQuotaMicros;
    private final Integer cpuShares;
    private final Integer cpus;
    private final String memorySize;
    private final String swapSize;
    private final OutputStream out;
    private final EventListener listener;

    public BuildImage(OkHttpClient client, Config config) {
        this(client, config, null, DEFAULT_DOCKERFILE, false, null, false, true, false, false, 0, 0, 0, 0, "0", "0", null, NULL_LISTENER);
    }

    public BuildImage(OkHttpClient client, Config config, String repositoryName, String dockerFile, Boolean noCache, String buildArgs, Boolean pulling, Boolean alwaysRemoveIntermediate, Boolean removeIntermediateOnSuccess, Boolean supressingVerboseOutput, Integer cpuPeriodMicros, Integer cpuQuotaMicros, Integer cpuShares, Integer cpus, String memorySize, String swapSize, OutputStream out, EventListener listener) {
        super(client, config, BUILD_OPERATION, null, null);
        this.dockerFile = dockerFile;
        this.buildArgs = buildArgs;
        this.pulling = pulling;
        this.alwaysRemoveIntermediate = alwaysRemoveIntermediate;
        this.removeIntermediateOnSuccess = removeIntermediateOnSuccess;
        this.supressingVerboseOutput = supressingVerboseOutput;
        this.cpuPeriodMicros = cpuPeriodMicros;
        this.cpuQuotaMicros = cpuQuotaMicros;
        this.cpuShares = cpuShares;
        this.cpus = cpus;
        this.memorySize = memorySize;
        this.swapSize = swapSize;
        this.repositoryName = repositoryName;
        this.noCache = noCache;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public OutputHandle fromFolder(String path) {
        try {
            final Path root = Paths.get(path);
            final Path dockerIgnore = root.resolve(DOCKER_IGNORE);
            final List<String> ignorePatterns = new ArrayList<>();
            if (dockerIgnore.toFile().exists()) {
                for (String p : Files.readAllLines(dockerIgnore, UTF_8)) {
                    ignorePatterns.add(path.endsWith(File.separator) ? path + p : path + File.separator + p);
                }
            }

            final DockerIgnorePathMatcher dockerIgnorePathMatcher = new DockerIgnorePathMatcher(ignorePatterns);

            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), DOCKER_PREFIX, BZIP2_SUFFIX).toFile();

            try ( final TarArchiveOutputStream tout = buildTarStream(tempFile)) {
                 Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

                     @Override
                     public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                         if (dockerIgnorePathMatcher.matches(dir)) {
                             return FileVisitResult.SKIP_SUBTREE;
                         }
                         return FileVisitResult.CONTINUE;
                     }

                     @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (dockerIgnorePathMatcher.matches(file)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        final Path relativePath = root.relativize(file);
                        final TarArchiveEntry entry = new TarArchiveEntry(file.toFile());
                        entry.setName(relativePath.toString());
                        entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
                        entry.setSize(attrs.size());
                        putTarEntry(tout, entry, file);
                        return FileVisitResult.CONTINUE;
                    }
                });
                tout.flush();
            }
            return fromTar(tempFile.getAbsolutePath());

        } catch (IOException e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public OutputHandle fromTar(InputStream is) {
        try {
            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), DOCKER_PREFIX, BZIP2_SUFFIX).toFile();
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fromTar(tempFile.getAbsolutePath());
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public OutputHandle fromTar(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getOperationUrl());
            sb.append(Q).append(DOCKER_FILE).append(EQUALS).append(dockerFile);

            if (alwaysRemoveIntermediate) {
                sb.append(A).append(ALWAYS_REMOVE_INTERMEDIATE).append(EQUALS).append(alwaysRemoveIntermediate);
            } else {
                sb.append(A).append(REMOVE_INTERMEDIATE_ON_SUCCESS).append(EQUALS).append(removeIntermediateOnSuccess);
            }

            sb.append(A).append(PULL).append(EQUALS).append(pulling);
            sb.append(A).append(SUPRESS_VERBOSE_OUT).append(EQUALS).append(supressingVerboseOutput);
            sb.append(A).append(NOCACHE).append(EQUALS).append(noCache);


            if (cpuPeriodMicros >= 0) {
                sb.append(A).append(CPU_PERIOD).append(EQUALS).append(cpuPeriodMicros);
            }

            if (cpuQuotaMicros >= 0) {
                sb.append(A).append(CPU_QUOTA).append(EQUALS).append(cpuQuotaMicros);
            }

            if (cpuShares >= 0) {
                sb.append(A).append(CPU_SHARES).append(EQUALS).append(cpuShares);
            }

            if (cpus > 0) {
                sb.append(A).append(CPUS).append(EQUALS).append(cpus);
            }

            if (Utils.isNotNullOrEmpty(memorySize)) {
                sb.append(A).append(MEMORY).append(EQUALS).append(memorySize);
            }

            if (Utils.isNotNullOrEmpty(swapSize)) {
                sb.append(A).append(MEMSWAP).append(EQUALS).append(swapSize);
            }

            if (Utils.isNotNullOrEmpty(buildArgs)) {
                sb.append(A).append(BUILD_ARGS).append(EQUALS).append(buildArgs);
            }

            if (Utils.isNotNullOrEmpty(repositoryName)) {
                sb.append(A).append(REPOSITORY_NAME).append(EQUALS).append(repositoryName);
            }

            RequestBody body = RequestBody.create(MEDIA_TYPE_TAR, new File(path));
            Request request = new Request.Builder()
                    .header("X-Registry-Config", new String(Base64.encodeBase64(JSON_MAPPER.writeValueAsString(config.getAuthConfigs()).getBytes("UTF-8")), "UTF-8"))
                    .post(body)
                    .url(sb.toString()).build();

            OkHttpClient clone = client.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
            BuildImageHandle handle = new BuildImageHandle(out, config.getImageBuildTimeout(), TimeUnit.MILLISECONDS, listener);
            clone.newCall(request).enqueue(handle);
            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }


    @Override
    public MemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> alwaysRemovingIntermediate() {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, true, false, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public RemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> pulling() {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, true, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public MemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> removingIntermediateOnSuccess() {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, false, true, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public NoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> supressingVerboseOutput() {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, true, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public UsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withBuildArgs(String buildArgs) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public CpuQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withCpuPeriod(int cpuPeriodMicros) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public BuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withCpuQuota(int cpuQuotaMicros) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public CpusCpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withCpuShares(int cpuShares) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public CpuPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withCpus(int cpus) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public SwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withMemory(String memorySize) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public PullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withNoCache() {
        return new BuildImage(client, config, repositoryName, dockerFile, true, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public SupressingVerboseOutputNoCachePullingRemoveIntermediateMemorySwapCpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingFromPathInterface<OutputHandle> withRepositoryName(String repositoryName) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public CpuSharesCpusPeriodQuotaBuildArgsUsingDockerFileListenerRedirectingWritingOutputFromPathInterface<OutputHandle> withSwap(String swapSize) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public UsingListenerRedirectingWritingOutputFromPathInterface<OutputHandle> usingDockerFile(String dockerFile) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public RedirectingWritingOutputFromPathInterface<OutputHandle> usingListener(EventListener listener) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }

    @Override
    public FromPathInterface<OutputHandle> redirectingOutput() {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, new PipedOutputStream(), listener);
    }

    @Override
    public FromPathInterface<OutputHandle> writingOutput(OutputStream out) {
        return new BuildImage(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, out, listener);
    }
}
