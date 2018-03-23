package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class RequestTemplateRepresentation implements Serializable {

    private Long id;

    private UUID uuid;

    private List<UUID> organisations;

    @NotNull
    @Size(min = 1)
    private String url;

    @JsonProperty("URL")
    public String getUrl() {
        return url;
    }

    @NotNull
    @Size(min = 1)
    private String humanReadable;

    private List<Map<String, String>> collections;

    private String nToken;

}
