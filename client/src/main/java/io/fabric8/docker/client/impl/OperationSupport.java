/**
 * Copyright (C) 2015 Red Hat, Inc.
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
 */
package io.fabric8.docker.client.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import io.fabric8.docker.api.model.Container;
import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.client.utils.URLUtils;
import io.fabric8.docker.client.utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OperationSupport {


  public static final MediaType MEDIA_TYPE_RAW_STREAM = MediaType.parse("application/vnd.docker.raw-stream");
  public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
  protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  protected static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  
  protected final OkHttpClient client;
  protected final Config config;
  protected final String resourceType;
  protected final String name;
  protected final String operationType;

  public OperationSupport() {
    this(null, null, null, null, null);
  }

  public OperationSupport(OkHttpClient client, Config config, String resourceType, String name, String operationType) {
    this.client = client;
    this.config = config;
    this.resourceType = resourceType;
    this.name = name;
    this.operationType = operationType;
  }


  public String getResourceType() {
    return resourceType;
  }

  public String getName() {
    return name;
  }

  public URL getRootUrl() {
    try {
      return new URL(URLUtils.join(config.getMasterUrl().toString()));
    } catch (MalformedURLException e) {
      throw DockerClientException.launderThrowable(e);
    }
  }

  public URL getResourceUrl(String name) throws MalformedURLException {
    if (name != null && !name.isEmpty()) {
      return new URL(URLUtils.join(getRootUrl().toString(), resourceType, name));
    } else {
      return new URL(URLUtils.join(getRootUrl().toString(), resourceType));
    }
  }

  public URL getResourceUrl() throws MalformedURLException {
   return getResourceUrl(name);
  }

  public URL getOperationUrl() throws MalformedURLException {
    return getOperationUrl(operationType);
  }

  public URL getOperationUrl(String operationType) throws MalformedURLException {
    if (operationType != null && !operationType.isEmpty()) {
      return new URL(URLUtils.join(getResourceUrl().toString(), operationType));
    } else {
      return new URL(URLUtils.join(getResourceUrl().toString()));
    }
  }

  protected <T> String checkName(T item) {
    String operationName = getName();


    String itemName = null;

    if (item instanceof Image) {
      itemName = ((Image)item).getId();
    } else if (item instanceof Container) {
      itemName = ((Container)item).getId();
    }

    if (Utils.isNullOrEmpty(operationName) && Utils.isNullOrEmpty(itemName)) {
      return null;
    } else if (Utils.isNullOrEmpty(itemName)) {
      return operationName;
    } else if (Utils.isNullOrEmpty(operationName)) {
      return itemName;
    } else if (itemName.equals(operationName)) {
      return itemName;
    }
    throw new DockerClientException("Name mismatch. Item name:" + itemName + ". Operation name:" + operationName + ".");
  }

  protected <T> void handleDelete(T resource) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    handleDelete(getResourceUrl(checkName(resource)));
  }

  protected void handleDelete(URL requestUrl) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    Request.Builder requestBuilder = new Request.Builder().delete(null).url(requestUrl);
    handleResponse(requestBuilder, 200, (Class) null);
  }

  protected <T, I> T handleCreate(I resource, Class<T> outputType, String ...dirs) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON_MAPPER.writeValueAsString(resource));
    Request.Builder requestBuilder = new Request.Builder().post(body).url(URLUtils.join(getResourceUrl().toString(), URLUtils.join(dirs)));
    return handleResponse(requestBuilder, 201, outputType);
  }

  protected <T> T handleReplace(T updated, Class<T> type) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, JSON_MAPPER.writeValueAsString(updated));
    Request.Builder requestBuilder = new Request.Builder().put(body).url(getResourceUrl(checkName(updated)));
    return handleResponse(requestBuilder, 200, type);
  }

  protected <T> T handleGet(URL resourceUrl, Class<T> type) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    Request.Builder requestBuilder = new Request.Builder().get().url(resourceUrl);
    return handleResponse(requestBuilder, 200, type);
  }

  protected <T> List<T> handleList(URL resourceUrl, Class<T> type) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    Request.Builder requestBuilder = new Request.Builder().get().url(resourceUrl);
    return handleResponse(requestBuilder, 200, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, type));
  }

  protected <T> T handleResponse(Request.Builder requestBuilder, int successStatusCode, Class<T> type) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    return handleResponse(requestBuilder, successStatusCode, JSON_MAPPER.getTypeFactory().constructType(type));
  }

  protected <T> T handleResponse(Request.Builder requestBuilder, int successStatusCode, JavaType type) throws ExecutionException, InterruptedException, DockerClientException, IOException {
    Request request = requestBuilder.build();
    Response response = null;
    try {
      response = client.newCall(request).execute();
    } catch (Exception e) {
      throw requestException(request, e);
    }
    assertResponseCode(request, response, successStatusCode);
    if (type != null) {
      return JSON_MAPPER.readValue(response.body().byteStream(), type);
    } else {
      return null;
    }
  }

  /**
   * Checks if the response status code is the expected and throws the appropriate DockerClientException if not.
   *
   * @param request            The {#link Request} object.
   * @param response           The {@link Response} object.
   * @param expectedStatusCode The expected status code.
   * @throws DockerClientException When the response code is not the expected.
   */
  protected void assertResponseCode(Request request, Response response, int expectedStatusCode) {
    int statusCode = response.code();
    if (statusCode == expectedStatusCode) {
      return;
    } else {
        throw requestFailure(request, response);
    }
  }

  DockerClientException requestFailure(Request request, Response response) {
    StringBuilder sb = new StringBuilder();
    sb.append("Failure executing: ").append(request.method())
      .append(" at: ").append(request.urlString()).append(".")
      .append(" Status:").append(response.code());
    return new DockerClientException(sb.toString(), response.code());
  }

  DockerClientException requestException(Request request, Exception e) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error executing: ").append(request.method())
      .append(" at: ").append(request.urlString())
      .append(". Cause: ").append(e.getMessage());

    return new DockerClientException(sb.toString(), e);
  }

   protected <T> T unmarshal(InputStream is, Class<T> type) throws DockerClientException {
    try (BufferedInputStream bis = new BufferedInputStream(is)) {
      bis.mark(-1);
      int intch;
      do {
        intch = bis.read();
      } while (intch > -1 && Character.isWhitespace(intch));
      bis.reset();

      ObjectMapper mapper = JSON_MAPPER;
      if (intch != '{') {
        mapper = YAML_MAPPER;
      }
      return mapper.readValue(bis, type);
    } catch (IOException e) {
      throw DockerClientException.launderThrowable(e);
    }
  }

  public Config getConfig() {
    return config;
  }
}
