package pauljabines.exam.isr.computers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Computer API.
 *
 * @author Paul Jabines
 */
@Path("/")
public class ComputersResource {
    private final EntityManagerFactory entityManagerFactory;

    public ComputersResource(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @POST
    @Path("/create_computer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(ComputerRequest computerRequest, @HeaderParam("apikey") String apiKey) {
        ComputerRequest.Status status = computerRequest.validate();
        if (status.equals(ComputerRequest.Status.COLOR_NOT_SUPPORTED)) {
            return Response.status(406)
                    .entity("Color is not supported!")
                    .build();
        } else if (status.equals(ComputerRequest.Status.NULL_VALUES_ENCOUNTERED)) {
            return Response.status(406)
                    .entity("Null values encountered!")
                    .build();
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Computer computer = findComputer(computerRequest, entityManager);
        if (computer != null) {
            return addColor(computerRequest.computer.color, computer, entityManager);
        }

        computer = computerRequest.toComputer();

        Response response;
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(computer);
            entityManager.getTransaction().commit();

            response = Response.status(201)
                    .entity(ComputerResponse.toComputerResponse(computer))
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

    private Computer findComputer(ComputerRequest computerRequest, EntityManager entityManager) {
        String query = "SELECT c FROM Computer c where c.type = :type " +
                "AND c.maker = :maker " +
                "AND c.model = :model " +
                "AND c.language = :language";

        try {
            return entityManager.createQuery(query, Computer.class)
                    .setParameter("type", computerRequest.computer.type)
                    .setParameter("maker", computerRequest.computer.maker)
                    .setParameter("model", computerRequest.computer.model)
                    .setParameter("language", computerRequest.computer.language)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Response addColor(String color, Computer computer, EntityManager entityManager) {
        computer.addColor(Color.fromName(color));
        computer.updateTimestamp();

        Response response;
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(computer);
            entityManager.getTransaction().commit();

            response = Response.status(200)
                    .entity(ComputerResponse.toComputerResponse(computer))
                    .build();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            response = Response.status(500)
                    .entity("Internal server error!")
                    .build();
        } finally {
            entityManager.close();
        }

        return response;
    }

    @GET
    @Path("/computers/{maker}/{model}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(
            @PathParam("maker") String maker,
            @PathParam("model") String model) {
        Computer computer = findComputerByMakerModel(maker, model);

        if (computer == null) {
            return Response.status(200)
                    .entity("No computer found!")
                    .build();
        }

        return Response.status(200)
                .entity(ComputerResponse.toComputerResponse(computer))
                .build();
    }

    private Computer findComputerByMakerModel(String maker, String model) {
        String query = "SELECT c FROM Computer c where c.maker = :maker " +
                "AND c.model = :model";
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            return entityManager.createQuery(query, Computer.class)
                    .setParameter("maker", maker)
                    .setParameter("model", model)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
