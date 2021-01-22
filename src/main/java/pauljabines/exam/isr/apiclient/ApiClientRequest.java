package pauljabines.exam.isr.apiclient;

import lombok.Getter;

/**
 * @author Paul Benedict Jabines
 */
public class ApiClientRequest {
    public ApiClientRequestBody apiClient;

    public ApiClient toApiClient() {
        ApiClient client = new ApiClient();
        client.setName(apiClient.name);
        client.setType(ApiClient.Type.fromDescription(apiClient.type));

        return client;
    }

    public Status validate() {
        if (apiClient == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        if (apiClient.name == null ||
                apiClient.type == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        try {
            ApiClient.Type.fromDescription(apiClient.type);
        } catch (IllegalArgumentException e) {
            return Status.TYPE_NOT_SUPPORTED;
        }

        return Status.OK;
    }

    public static class ApiClientRequestBody {
        public String name;

        public String type;
    }

    public enum Status {
        TYPE_NOT_SUPPORTED("Type not supported!"),
        NULL_VALUES_ENCOUNTERED("Null values encountered"),
        OK("Ok");

        @Getter
        private String description;

        Status(String description) {
            this.description = description;
        }
    }
}
