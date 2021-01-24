package pauljabines.exam.isr.sshkey;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Paul Benedict Jabines
 */
@Path("/authorized_keys")
public class SshKeyResource {
    private final EntityManagerFactory entityManagerFactory;

    public SshKeyResource(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(SshKeyRequest sshKeyRequest) {
        SshKeyRequest.Status status = sshKeyRequest.validate();
        if (status.equals(SshKeyRequest.Status.TYPE_NOT_SUPPORTED)) {
            return Response.status(406, "Type is not supported!")
                    .build();
        } else if (status.equals(SshKeyRequest.Status.NULL_VALUES_ENCOUNTERED)) {
            return Response.status(406, "Null values encountered!")
                    .build();
        } else if (status.equals(SshKeyRequest.Status.KEY_INVALID)) {
            return Response.status(400, "The content of the public key is invalid for the type 'ssh-rsa'")
                    .build();
        } else if (status.equals(SshKeyRequest.Status.ACCESS_RIGHTS_NOT_SUPPORTED)) {
            return Response.status(406, "Access rights is not supported!")
                    .build();
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        SshKey sshKey = sshKeyRequest.toSshKey();
        Response response;
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(sshKey);
            entityManager.getTransaction().commit();

            response = Response.status(201)
                    .entity(SshKeyResponse.toSshKeyResponse(sshKey))
                    .build();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            response = Response.status(500,"Internal server error! ")
                    .build();
        } finally {
            entityManager.close();
        }

        return response;
    }
}
