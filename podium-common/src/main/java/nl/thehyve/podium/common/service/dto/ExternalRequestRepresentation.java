package nl.thehyve.podium.common.service.dto;

import lombok.Data;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ExternalRequestRepresentation implements Serializable{

    @JsonProperty("URL")
    String url;
    String humanReadable;

    List<Map<String, String>> collections;

    String nToken;
}
