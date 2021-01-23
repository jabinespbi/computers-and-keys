package pauljabines.exam.isr;

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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Benedict Jabines
 */
public class SshKeyResourceIntegrationTest extends JerseyTest {
    private static final String TYPE = "ssh-ed25519";
    private static final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
    private static final String NAME = "asus";
    private static final String COMMENT = "happy@isr";
    private static final String ACCESS_RIGHTS_IT_SUPPLIER = "it_supplier";

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        ResourceConfig config = new ResourceConfig();
        SshKeyResource sshKeyResource = new SshKeyResource(EmfSingleton.getINSTANCE().getEntityManagerFactory());
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
    public void create_correctJson_responseIsSshKey() {
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());

        final String s = response.readEntity(String.class);
        JSONObject jsonObject = new JSONObject(s);
        JSONObject jsonValue = jsonObject.getJSONObject("sshKey");

        assertEquals(TYPE, jsonValue.getString("type"));
        assertEquals(NAME, jsonValue.getString("name"));
        assertEquals(COMMENT, jsonValue.getString("comment"));
        assertEquals(ACCESS_RIGHTS_IT_SUPPLIER, jsonValue.getString("accessRights"));
    }

    @Test
    public void create_JsonIncorrectType_responseIsNotSupported() {
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", "unknown-type");
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Type is not supported!"));
    }

    @Test
    public void create_JsonIncorrectAccessRights_responseIsNotSupported() {
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", "ssh-ed25519");
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", "invalid");

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 406 ", Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), equalTo("Access rights is not supported!"));
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
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request("application/json;q=0.8,application/xml; q=0.2")
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    @Test
    public void create_qFactorXmlPreferred_responseIsXml() {
        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request("application/json;q=0.1,application/xml; q=0.9")
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_XML, contentType);
    }

    @Test
    public void create_incorrectEdd25519_responseIs400() {
        final String PUBLIC = "aAAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 400 ", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void create_correctEd25519_responseIs201() {
        final String TYPE = "ssh-ed25519";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void create_incorrectRsa_responseIs400() {
        final String TYPE = "ssh-rsa";
        final String PUBLIC = "AAAAC3NzaC1lZDI1NTE5AAAAIOiKKC7lLUcyvJMo1gjvMr56XvOq814Hhin0OCYFDqT4";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 400 ", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void create_correctRsa_responseIs201() {
        final String TYPE = "ssh-rsa";
        final String PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyNG36i8crlmdpYHgV/s/9h+UobLIMlqX8CMjXvIOiJs35ttivYs1F4snp85vj62XtBBt7QVJEJqfHpMsFonCwP8qWEdToulvHpawY9ZJKEsNZt9NpEowMMjCycXFWJyV40WTvcmSU9x3mrViXm2y+kxmTXlAqVPlaZyWX5i249gX9zOrQ1s0KY9j65gqZ/bpcM/okmXK0OABtOnYCZlICU2Kjwccd+HRpvjbR8UWNtodSRz4wYFCtcpre0QEysqhCnG7NQEFKubZXGDxMmnM4f5hXT4vib/xcarVO6ip2OuRcW3HOGcimq1a5/ujdXEgQqXsgYpqrhCHVflGqPBSgQIDAQAB";
        final String COMMENT = "happy@isr";

        JSONObject sshKeyJsonValue = new JSONObject();
        sshKeyJsonValue.put("type", TYPE);
        sshKeyJsonValue.put("name", NAME);
        sshKeyJsonValue.put("publicKey", PUBLIC);
        sshKeyJsonValue.put("comment", COMMENT);
        sshKeyJsonValue.put("accessRights", ACCESS_RIGHTS_IT_SUPPLIER);

        JSONObject sshKeyJson = new JSONObject();
        sshKeyJson.put("sshKey", sshKeyJsonValue);

        Response response = target("/authorized_keys/create").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sshKeyJson.toString()));

        assertEquals("Http Response should be 201 ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }
}
