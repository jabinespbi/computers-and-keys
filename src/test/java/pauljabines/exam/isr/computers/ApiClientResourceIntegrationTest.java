package pauljabines.exam.isr.computers;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONObject;
import org.junit.Test;
import pauljabines.exam.isr.apiclient.ApiClientResource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Benedict Jabines
 */
public class ApiClientResourceIntegrationTest extends JerseyTest {
    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        ApiClientResource apiClientResource = new ApiClientResource(EmfSingleton.getINSTANCE().getEntityManagerFactory());
        config.register(apiClientResource);
        return config;
    }

    @Test
    public void create_correctJson_responseIsApiClient() {
        final String NAME = "PAUL JABINES CLOUD";
        final String TYPE = "IT User";

        JSONObject apiClientJsonValue = new JSONObject();
        apiClientJsonValue.put("name", NAME);
        apiClientJsonValue.put("type", TYPE);

        JSONObject apiClientJson = new JSONObject();
        apiClientJson.put("apiClient", apiClientJsonValue);

        Response response = target("/api_client/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(apiClientJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());

        final String s = response.readEntity(String.class);
        JSONObject jsonObject = new JSONObject(s);
        JSONObject jsonValue = jsonObject.getJSONObject("apiClient");

        assertEquals(NAME, jsonValue.getString("name"));
        assertEquals(TYPE, jsonValue.getString("type"));
    }

    @Test
    public void create_JsonIncorrectType_responseIsNotSupported() {
        final String NAME = "ISR";
        final String TYPE = "IT";

        JSONObject apiClientJsonValue = new JSONObject();
        apiClientJsonValue.put("name", NAME);
        apiClientJsonValue.put("type", TYPE);

        JSONObject apiClientJson = new JSONObject();
        apiClientJson.put("apiClient", apiClientJsonValue);

        Response response = target("/api_client/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(apiClientJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Type is not supported!"));
    }

    @Test
    public void create_JsonWithNull_responseIsNotAcceptable() {
        JSONObject apiClientJsonValue = new JSONObject();
        apiClientJsonValue.put("name", "test");

        JSONObject apiClientJson = new JSONObject();
        apiClientJson.put("computer", apiClientJsonValue);

        Response response = target("/api_client/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(apiClientJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Null values encountered!"));
    }

    @Test
    public void create_qFactorJsonPreferred_responseIsJson() {
        final String NAME = "name";
        final String TYPE = "Api Admin";

        JSONObject apiClientJsonValue = new JSONObject();
        apiClientJsonValue.put("name", NAME);
        apiClientJsonValue.put("type", TYPE);

        JSONObject apiClientJson = new JSONObject();
        apiClientJson.put("apiClient", apiClientJsonValue);

        Response response = target("/api_client/create").request("application/json;q=0.8,application/xml; q=0.2")
                .post(Entity.json(apiClientJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void create_qFactorXmlPreferred_responseIsXml() {
        final String NAME = "name";
        final String TYPE = "Api Admin";

        JSONObject apiClientJsonValue = new JSONObject();
        apiClientJsonValue.put("name", NAME);
        apiClientJsonValue.put("type", TYPE);

        JSONObject apiClientJson = new JSONObject();
        apiClientJson.put("apiClient", apiClientJsonValue);

        Response response = target("/api_client/create").request("application/json;q=0.1,application/xml; q=0.9")
                .post(Entity.json(apiClientJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }
}
