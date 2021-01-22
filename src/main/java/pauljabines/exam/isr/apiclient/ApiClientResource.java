package pauljabines.exam.isr.apiclient;

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
@Path("/api_client")
public class ApiClientResource {
    private final EntityManagerFactory entityManagerFactory;

    public ApiClientResource(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(ApiClientRequest apiClientRequest) {
        ApiClientRequest.Status status = apiClientRequest.validate();
        if (status.equals(ApiClientRequest.Status.TYPE_NOT_SUPPORTED)) {
            return Response.status(406)
                    .entity("Type is not supported!")
                    .build();
        } else if (status.equals(ApiClientRequest.Status.NULL_VALUES_ENCOUNTERED)) {
            return Response.status(406)
                    .entity("Null values encountered!")
                    .build();
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        ApiClient apiClient = apiClientRequest.toApiClient();
        Response response;
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(apiClient);
            entityManager.getTransaction().commit();

            response = Response.status(201)
                    .entity(ApiClientResponse.toApiClientResponse(apiClient))
                    .build();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            response = Response.status(500)
                    .entity("Internal server error! ")
                    .build();
        } finally {
            entityManager.close();
        }

        return response;
    }
}
