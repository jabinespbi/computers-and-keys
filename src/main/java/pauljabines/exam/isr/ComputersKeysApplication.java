package pauljabines.exam.isr;

import pauljabines.exam.isr.apiclient.ApiClientResource;
import pauljabines.exam.isr.computers.ComputersResource;
import pauljabines.exam.isr.sshkey.SshKeyResource;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class ComputersKeysApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ComputersKeys");

        ComputersResource computersResource = new ComputersResource(entityManagerFactory);
        ApiClientResource apiClientResource = new ApiClientResource(entityManagerFactory);
        SshKeyResource sshKeyResource = new SshKeyResource(entityManagerFactory);

        Set<Object> singletons = new HashSet<>();
        singletons.add(computersResource);
        singletons.add(apiClientResource);
        singletons.add(sshKeyResource);
        return singletons;
    }
}
