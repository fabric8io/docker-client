package io.fabric8.docker.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "stream",
        "errorDetail",
        "error"
})
public class ProgressEvent {

    @JsonProperty("stream")
    private String stream;
    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;
    @JsonProperty("error")
    private String error;

    public ProgressEvent() {
    }

    public ProgressEvent(String stream, ErrorDetail errorDetail, String error) {
        this.stream = stream;
        this.error = error;
        this.errorDetail = errorDetail;
    }

    public String getStream() {
        return stream;
    }

    public String getError() {
        return error;
    }

    public ErrorDetail getErrorDetail() {
        return errorDetail;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {

        @JsonProperty("message")
        private String message;

        public ErrorDetail() {
            this(null);
        }
        public ErrorDetail(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
