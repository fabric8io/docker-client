
package io.fabric8.docker.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "IP",
    "Mask"
})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@ToString
@EqualsAndHashCode
@Buildable(editableEnabled = true, validationEnabled = true, generateBuilderPackage = true, builderPackage = "io.fabric8.docker.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class NetIPNet {

    private static final String IP = "ip";
    private static final String MASK = "mask";
    private static final Pattern NETIP_PATTERN = Pattern.compile("(?<ip>[^ /]+)/(?<mask>\\d+)");

    /**
     * 
     * 
     */
    @JsonProperty("IP")
    private String ip;
    /**
     * 
     * 
     */
    @JsonProperty("Mask")
    private String mask;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public NetIPNet() {
    }

    /**
     * 
     * @param ip
     * @param mask
     */
    public NetIPNet(String ip, String mask) {
        this.ip = ip;
        this.mask = mask;
    }

    public NetIPNet(String s) {
        if (s == null) {
            throw new IllegalArgumentException(s);
        }

        Matcher m = NETIP_PATTERN.matcher(s);
        if ( m.matches()) {
            this.ip = m.group(IP);
            this.mask = m.group(MASK);
        } else {
            throw new IllegalArgumentException(s);
        }
    }

    /**
     * 
     * 
     * @return
     *     The IP
     */
    @JsonProperty("IP")
    public String getIp() {
        return ip;
    }

    /**
     * 
     * 
     * @param ip
     *     The ip
     */
    @JsonProperty("IP")
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 
     * 
     * @return
     *     The Mask
     */
    @JsonProperty("Mask")
    public String getMask() {
        return mask;
    }

    /**
     * 
     * 
     * @param mask
     *     The Mask
     */
    @JsonProperty("Mask")
    public void setMask(String mask) {
        this.mask = mask;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
