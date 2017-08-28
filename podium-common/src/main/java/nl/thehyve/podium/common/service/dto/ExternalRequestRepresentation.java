package nl.thehyve.podium.common.service.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

@Data
public class ExternalRequestRepresentation implements Serializable{

    //TO DO: Fix this so it works when the url field is called URL
    String url;
    String humanReadable;

    ArrayList<Map<String, String>> collections;

    String nToken;
}
