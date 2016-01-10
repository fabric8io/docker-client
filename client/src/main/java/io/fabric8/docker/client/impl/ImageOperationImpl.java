package io.fabric8.docker.client.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.api.model.ImageHistory;
import io.fabric8.docker.api.model.ImageInspect;
import io.fabric8.docker.api.model.SearchResult;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.OutputHandle;
import io.fabric8.docker.dsl.image.FilterOrFiltersOrAllImagesOrEndImagesInterface;
import io.fabric8.docker.dsl.image.ImageInspectOrHistoryOrPushOrTagOrDeleteInterface;
import io.fabric8.docker.dsl.image.ImageInterface;
import io.fabric8.docker.dsl.image.RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrTagOrAsRepoInterface;
import io.fabric8.docker.dsl.image.UsingListenerOrTagOrFromImageInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageOperationImpl extends OperationSupport implements ImageInterface {

    private static final JavaType IMAGE_SEARCH_RESULT_LIST = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, SearchResult.class);
    private static final String SEARCH_OPERATION = "search";
    private static final String TERM = "term";

    public ImageOperationImpl(OkHttpClient client, Config config) {
        super(client, config, IMAGES_RESOURCE, null, null);
    }

    @Override
    public RepositoryNameOrSupressingVerboseOutputOrNoCacheOrPullingOrRemoveIntermediateOrMemoryOrSwapOrCpuSharesOrCpusOrCpuPeriodOrCpuQuotaOrBuildArgsOrUsingDockerFileOrUsingListenerOrFromPathInterface<OutputHandle> build() {
        return new ImageBuild(client, config);
    }

    @Override
    public FilterOrFiltersOrAllImagesOrEndImagesInterface<List<Image>> list() {
        return new ImageList(client,config, null, new HashMap<String,String[]>());
    }

    @Override
    public ImageInspectOrHistoryOrPushOrTagOrDeleteInterface<ImageInspect, List<ImageHistory>, OutputHandle, Boolean, ImageDelete> withName(String name) {
        return new ImageNamedOperationImpl(client, config, name);
    }

    @Override
    public UsingListenerOrTagOrAsRepoInterface<OutputHandle> importFrom(String source) {
        return new ImageImport(client, config, source);
    }

    @Override
    public UsingListenerOrTagOrFromImageInterface<OutputHandle> pull() {
        return new ImagePull(client, config);
    }

    @Override
    public List<SearchResult> search(String term) {
        try {
            StringBuilder sb = new StringBuilder()
                    .append(getOperationUrl(SEARCH_OPERATION))
                    .append(Q).append(TERM).append(EQUALS).append(term);

            Request request = new Request.Builder().get().url(new URL(sb.toString())).get().build();
            Response response = null;
            try {
                OkHttpClient clone = client.clone();
                clone.setReadTimeout(config.getImageSearchTimeout(), TimeUnit.MILLISECONDS);
                response = clone.newCall(request).execute();
                assertResponseCodes(request, response, 200);
            } catch (Exception e) {
                throw requestException(request, e);
            }
            return JSON_MAPPER.readValue(response.body().byteStream(), IMAGE_SEARCH_RESULT_LIST);
        } catch (Exception e) {
            throw DockerClientException.launderThrowable(e);
        }
    }
}
