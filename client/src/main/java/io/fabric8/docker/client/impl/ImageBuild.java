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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.Utils;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.BuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.CpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.CpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.CpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.CpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.FromPathInterface;
import io.fabric8.docker.dsl.image.MemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.NoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.PullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.RemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.SupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.SwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.UsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrFromPathInterface;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class ImageBuild extends OperationSupport implements
        RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        MemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        RemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        PullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        NoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        UsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        CpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        BuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        CpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        CpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        SwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        SupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        CpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle>,
        UsingListenerOrFromPathInterface<OutputHandle> {

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
    private final EventListener listener;

    public ImageBuild(OkHttpClient client, Config config) {
        this(client, config, null, DEFAULT_DOCKERFILE, false, null, false, true, false, false, 0, 0, 0, 0, "0", "0", NULL_LISTENER);
    }

    public ImageBuild(OkHttpClient client, Config config, String repositoryName, String dockerFile, Boolean noCache, String buildArgs, Boolean pulling, Boolean alwaysRemoveIntermediate, Boolean removeIntermediateOnSuccess, Boolean supressingVerboseOutput, Integer cpuPeriodMicros, Integer cpuQuotaMicros, Integer cpuShares, Integer cpus, String memorySize, String swapSize, EventListener listener) {
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
        this.listener = listener;
    }

    @Override
    public OutputHandle fromFolder(String path) {
        try {
            final Path root = Paths.get(path);
            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), TEMP_PREFIX, TEMP_SUFFIX).toFile();

            try (FileOutputStream fout = new FileOutputStream(tempFile);
                 BufferedOutputStream bout = new BufferedOutputStream(fout);
                 BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(bout);
                 final TarArchiveOutputStream tout = new TarArchiveOutputStream(bzout)) {
                    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final Path relativePath = root.relativize(file);
                        final TarArchiveEntry entry = new TarArchiveEntry(file.toFile());
                        entry.setName(relativePath.toString());
                        entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
                        entry.setSize(attrs.size());
                        tout.putArchiveEntry(entry);
                        Files.copy(file, tout);
                        tout.closeArchiveEntry();
                        return FileVisitResult.CONTINUE;
                    }

                });
                fout.flush();
            }
            return fromTar(tempFile.getAbsolutePath());

        } catch (IOException e) {
            throw DockerClientException.launderThrowable(e);
        }
    }

    @Override
    public OutputHandle fromTar(InputStream is) {
        try {
            File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), TEMP_PREFIX, TEMP_SUFFIX).toFile();
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
                    .header("X-Registry-Config", new String(org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(JSON_MAPPER.writeValueAsString(config.getAuthConfigs()).getBytes("UTF-8")), "UTF-8"))
                    .post(body)
                    .url(sb.toString()).build();

            ImageBuildHandle handle = new ImageBuildHandle(config.getImageBuildTimeout(), TimeUnit.MILLISECONDS, listener);
            client.newCall(request).enqueue(handle);

            return handle;
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }


    @Override
    public MemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> alwaysRemovingIntermediate() {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, true, false, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public RemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> pulling() {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, true, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public MemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> removingIntermediateOnSuccess() {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, false, true, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public NoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> supressingVerboseOutput() {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, true, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public UsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withBuildArgs(String buildArgs) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public CpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withCpuPeriod(int cpuPeriodMicros) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public BuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withCpuQuota(int cpuQuotaMicros) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public CpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withCpuShares(int cpuShares) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public CpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withCpus(int cpus) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public SwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withMemory(String memorySize) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public PullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withNoCache() {
        return new ImageBuild(client, config, repositoryName, dockerFile, true, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public SupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withRepositoryName(String repositoryName) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public CpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> withSwap(String swapSize) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public UsingListenerOrFromPathInterface<OutputHandle> usingDockerFile(String dockerFile) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }

    @Override
    public FromPathInterface<OutputHandle> usingListener(EventListener listener) {
        return new ImageBuild(client, config, repositoryName, dockerFile, noCache, buildArgs, pulling, alwaysRemoveIntermediate, removeIntermediateOnSuccess, supressingVerboseOutput, cpuPeriodMicros, cpuQuotaMicros, cpuShares, cpus, memorySize, swapSize, listener);
    }
}
