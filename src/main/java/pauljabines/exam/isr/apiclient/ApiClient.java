package pauljabines.exam.isr.apiclient;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author Paul Benedict Jabines
 */
@Entity
public class ApiClient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Getter
    @Column(unique = true)
    private String uid = UUID.randomUUID().toString();

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Type type;

    public enum Type {
        IT_USERS("IT User"),
        API_ADMIN("Api Admin");

        @Getter
        private String description;

        Type(String description) {
            this.description = description;
        }

        public static Type fromDescription(String description) {
            for (Type type : Type.values()) {
                if (type.description.equalsIgnoreCase(description)) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Type not found!");
        }
    }
}