package pauljabines.exam.isr.computers;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONObject;
import org.junit.Test;
import pauljabines.exam.isr.sshkey.SshKeyResource;

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
public class SshKeyResourceIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        SshKeyResource sshKeyResource = new SshKeyResource(EmfSingleton.getINSTANCE().getEntityManagerFactory());
        config.register(sshKeyResource);
        return config;
    }

    @Test
    public void create_correctJson_responseIsSshKey() {
        final String TYPE = "ssh-ed25519";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());

        final String s = response.readEntity(String.class);
        JSONObject jsonObject = new JSONObject(s);
        JSONObject jsonValue = jsonObject.getJSONObject("sshKey");

        assertEquals(TYPE, jsonValue.getString("type"));
        assertEquals(COMMENT, jsonValue.getString("comment"));
    }

    @Test
    public void create_JsonIncorrectType_responseIsNotSupported() {
        final String TYPE = "unknown-type";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Type is not supported!"));
    }

    @Test
    public void create_JsonWithNull_responseIsNotAcceptable() {
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("public", "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4");

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Null values encountered!"));
    }

    @Test
    public void create_qFactorJsonPreferred_responseIsJson() {
        final String TYPE = "ssh-ed25519";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request("application/json;q=0.8,application/xml; q=0.2")
                .post(Entity.json(sshKeyJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void create_qFactorXmlPreferred_responseIsXml() {
        final String TYPE = "ssh-ed25519";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request("application/json;q=0.1,application/xml; q=0.9")
                .post(Entity.json(sshKeyJson.toString()));
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }
}
