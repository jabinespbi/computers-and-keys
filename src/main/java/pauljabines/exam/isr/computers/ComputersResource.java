package pauljabines.exam.isr.computers;

import org.mindrot.jbcrypt.BCrypt;
import pauljabines.exam.isr.sshkey.SshKey;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
        if (apiKey == null) {
            return Response.status(403)
                    .entity("Forbidden!")
                    .build();
        }

        if (!shouldGrantAccess(apiKey)) {
            return Response.status(403)
                    .entity("Forbidden!")
                    .build();
        }

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

        return createComputer(computerRequest);
    }

    private Response createComputer(ComputerRequest computerRequest) {
        Computer computer = findComputer(computerRequest);
        if (computer != null) {
            return addColor(computerRequest.computer.color, computer);
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();

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

            throw e;
        } finally {
            entityManager.close();
        }

        return response;
    }

    private Computer findComputer(ComputerRequest computerRequest) {
        String query = "SELECT c FROM Computer c where c.type = :type " +
                "AND c.maker = :maker " +
                "AND c.model = :model " +
                "AND c.language = :language";

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Computer computer = null;
        try {
            computer = entityManager.createQuery(query, Computer.class)
                    .setParameter("type", computerRequest.computer.type)
                    .setParameter("maker", computerRequest.computer.maker)
                    .setParameter("model", computerRequest.computer.model)
                    .setParameter("language", computerRequest.computer.language)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        } finally {
            entityManager.close();
        }

        return computer;
    }

    private Response addColor(String color, Computer computer) {
        computer.addColor(Color.fromName(color));
        computer.updateTimestamp();

        EntityManager entityManager = entityManagerFactory.createEntityManager();
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

            throw e;
        } finally {
            entityManager.close();
        }

        return response;
    }

    @GET
    @Path("/computers/{maker}/{model}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMakerModel(
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
        Computer computer = null;
        try {
            computer = entityManager.createQuery(query, Computer.class)
                    .setParameter("maker", maker)
                    .setParameter("model", model)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        } finally {
            entityManager.close();
        }

        return computer;
    }

    private boolean shouldGrantAccess(String apiKey) {
        String query = "SELECT s FROM SshKey s";

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<SshKey> sshKeys = new ArrayList<>();

        try {
            List<SshKey> sshKeysFromDb = entityManager.createQuery(query, SshKey.class)
                    .getResultList();

            sshKeys.addAll(sshKeysFromDb);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            throw e;
        } finally {
            entityManager.close();
        }

        for (SshKey sshKey : sshKeys) {
            if (BCrypt.checkpw(apiKey, sshKey.getPublicKey())) {
                return true;
            }
        }

        return false;
    }

    @GET
    @Path("/computers/{maker}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getComputersByMaker(@PathParam("maker") String maker, @HeaderParam("apikey") String apiKey) {
        if (!shouldGrantAccess(apiKey)) {
            return Response.status(403)
                    .entity("Forbidden!")
                    .build();
        }

        List<Computer> computers = findComputerByMaker(maker);
        List<ComputerResponse> computerResponses = new ArrayList<>();

        for (Computer computer : computers) {
            ComputerResponse computerResponse = ComputerResponse.toComputerResponse(computer);
            computerResponses.add(computerResponse);
        }

        return Response.status(200)
                .entity(computerResponses)
                .build();
    }

    private List<Computer> findComputerByMaker(String maker) {
        String query = "SELECT c FROM Computer c where c.maker = :maker";

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Computer> computers = new ArrayList<>();
        try {
            List<Computer> computersFromDb = entityManager.createQuery(query, Computer.class)
                    .setParameter("maker", maker)
                    .getResultList();

            computers.addAll(computersFromDb);
        } catch (NoResultException ignored) {
        } finally {
            entityManager.close();
        }

        return computers;
    }
}
