package pauljabines.exam.isr.apiclient;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Paul Benedict Jabines
 */
@XmlRootElement
public class ApiClientResponse {
    public ApiClientResponseBody apiClient;

    public static ApiClientResponse toApiClientResponse(ApiClient apiClient) {
        ApiClientResponse apiClientResponse = new ApiClientResponse();
        apiClientResponse.apiClient = new ApiClientResponseBody();
        apiClientResponse.apiClient.name = apiClient.getName();
        apiClientResponse.apiClient.type = apiClient.getType().getDescription();

        return apiClientResponse;
    }

    public static class ApiClientResponseBody {
        public String name;
        public String type;
    }
}
