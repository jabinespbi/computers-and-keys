package pauljabines.exam.isr.computers;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONObject;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ComputersResourceIntegrationTest extends JerseyTest {
    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ComputersKeys_Test");

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        ComputersResource computersResource = new ComputersResource(entityManagerFactory);
        config.register(computersResource);
        return config;
    }

    @Test
    public void create_correctJson_responseIsComputer() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "silver";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());

        final String s = response.readEntity(String.class);
        JSONObject jsonObject = new JSONObject(s);
        JSONObject jsonValue = jsonObject.getJSONObject("computer");

        assertEquals(TYPE, jsonValue.getString("type"));
        assertEquals(MAKER, jsonValue.getString("maker"));
        assertEquals(MODEL, jsonValue.getString("model"));
        assertEquals(LANGUAGE, jsonValue.getString("language"));
        assertThat(jsonValue.get("colors").toString(), containsString(COLOR));
    }

    @Test
    public void create_JsonIncorrectColor_responseIsNotSupported() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "incorrect";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Color is not supported!"));
    }

    @Test
    public void create_JsonWithNull_responseIsNotAcceptable() {
        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("color", "silver");

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Null values encountered!"));
    }

    @Test
    public void create_qFactorJsonPreferred_responseIsJson() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "silver";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request("application/json;q=0.8,application/xml; q=0.2")
                .post(Entity.json(computerJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void create_qFactorXmlPreferred_responseIsXml() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "silver";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request("application/json;q=0.1,application/xml; q=0.9")
                .post(Entity.json(computerJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }

    @Test
    public void get_qFactorJsonPreferred_responseContentTypeIsJson() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "silver";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        target("/create_computer").request().post(Entity.json(computerJson.toString()));

        Response response = target("/computers/" + MAKER + "/" + MODEL).request("application/json;q=0.8,application/xml; q=0.2")
                .get();
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void get_qFactorXmlPreferred_responseContentTypeIsXml() {
        final String TYPE = "laptop";
        final String MAKER = "ASUS";
        final String MODEL = "X507UA";
        final String LANGUAGE = "日本語";
        final String COLOR = "silver";

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("type", TYPE);
        computerJsonValue.put("maker", MAKER);
        computerJsonValue.put("model", MODEL);
        computerJsonValue.put("language", LANGUAGE);
        computerJsonValue.put("color", COLOR);

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        target("/create_computer").request().post(Entity.json(computerJson.toString()));

        Response response = target("/computers/" + MAKER + "/" + MODEL).request("application/json;q=0.4,application/xml; q=0.6")
                .get();
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }
}
