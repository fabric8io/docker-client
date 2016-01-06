package io.fabric8.docker.client.impl;

import com.squareup.okhttp.OkHttpClient;
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.dsl.image.FilterOrFiltersOrAllImagesOrEndImagesInterface;
import io.fabric8.docker.client.dsl.image.ImageInspectOrHistoryOrPushOrTagOrDeleteInterface;
import io.fabric8.docker.client.dsl.image.ImagesInterface;
import io.fabric8.docker.client.dsl.image.RepoOrTagOrFromImageOrFromSourceInterface;
import io.fabric8.docker.client.dsl.image.RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrFromPathInterface;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class ImageOperationImpl extends OperationSupport implements ImagesInterface {


    public ImageOperationImpl(OkHttpClient client, Config config) {
        super(client, config, IMAGES_RESOURCE, null, null);
    }

    @Override
    public RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrFromPathInterface<InputStream> build() {
        return new ImageBuild(client, config);
    }

    @Override
    public RepoOrTagOrFromImageOrFromSourceInterface<String> create() {
        return null;
    }

    @Override
    public FilterOrFiltersOrAllImagesOrEndImagesInterface<List<Image>> list() {
        return new ImageList(client,config, null, new HashMap<String,String[]>());
    }

    @Override
    public ImageInspectOrHistoryOrPushOrTagOrDeleteInterface<Void, List<ImageHistory>, String, ImageDelete> withName(String name) {
        return null;
    }
}
