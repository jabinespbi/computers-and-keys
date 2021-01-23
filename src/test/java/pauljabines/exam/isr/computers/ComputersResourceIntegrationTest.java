package pauljabines.exam.isr.computers;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONObject;
import org.junit.Test;
import pauljabines.exam.isr.sshkey.SshKeyResource;

import javax.persistence.EntityManager;
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
    private static final String PUBLIC_KEY = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
    private static final String TYPE = "laptop";
    private static final String MODEL = "X507UA";
    private static final String MAKER = "asus";
    private static final String LANGUAGE = "日本語";
    private static final String COLOR = "silver";

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        ComputersResource computersResource = new ComputersResource(EmfSingleton.getINSTANCE().getEntityManagerFactory());
        SshKeyResource sshKeyResource = new SshKeyResource(EmfSingleton.getINSTANCE().getEntityManagerFactory());

        ResourceConfig config = new ResourceConfig();
        config.register(computersResource);
        config.register(sshKeyResource);

        return config;
    }

    @Override
    public void tearDown() {
        EntityManager entityManager = EmfSingleton.getINSTANCE()
                .getEntityManagerFactory()
                .createEntityManager();

        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM SshKey").executeUpdate();
            entityManager.createQuery("DELETE FROM Computer").executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Test
    public void create_correctJson_responseIsComputer() {
        createSshKey();
        Response response = createComputer();

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
        createSshKey();

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
                .header("apikey", PUBLIC_KEY)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Color is not supported!"));
    }

    @Test
    public void create_JsonWithNull_responseIsNotAcceptable() {
        createSshKey();

        JSONObject computerJsonValue = new JSONObject();
        computerJsonValue.put("color", "silver");

        JSONObject computerJson = new JSONObject();
        computerJson.put("computer", computerJsonValue);

        Response response = target("/create_computer").request(MediaType.APPLICATION_JSON)
                .header("apikey", PUBLIC_KEY)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Null values encountered!"));
    }

    @Test
    public void create_qFactorJsonPreferred_responseIsJson() {
        createSshKey();

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
                .header("apikey", PUBLIC_KEY)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void create_qFactorXmlPreferred_responseIsXml() {
        createSshKey();

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
                .header("apikey", PUBLIC_KEY)
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }

    @Test
    public void create_wrongSshKey_responseIsForbidden() {
        createSshKey();

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
                .header("apikey", "Invalid")
                .post(Entity.json(computerJson.toString()));

        assertEquals("Http Response should be 403 ", Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
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

    private void createSshKey() {
        final String TYPE = "ssh-ed25519";
        final String NAME = "asus";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC_KEY);
        sshKeyJsonValue.put("comment", COMMENT);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));
    }

    @Test
    public void getComputersByMaker_qFactorJsonPreferred_responseContentTypeIsJson() {
        createSshKey();
        createComputer();
        createComputer();
        createComputer();

        Response response = target("/computers/ASUS")
                .request("application/json;q=0.8,application/xml; q=0.2")
                .header("apikey", PUBLIC_KEY)
                .get();

        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    public Response createComputer() {
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

        return target("/create_computer").request()
                .header("apikey", PUBLIC_KEY)
                .post(Entity.json(computerJson.toString()));
    }
}
