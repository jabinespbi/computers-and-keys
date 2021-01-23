package pauljabines.exam.isr.computers;

import lombok.Getter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Paul Benedict Jabines
 */
public class EmfSingleton {
    @Getter
    private static EmfSingleton INSTANCE = new EmfSingleton();

    private EmfSingleton() {
    }

    @Getter
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ComputersKeys_Test");
}
