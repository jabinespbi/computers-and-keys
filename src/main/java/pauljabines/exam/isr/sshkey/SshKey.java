package pauljabines.exam.isr.sshkey;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Paul Benedict Jabines
 */
@Entity
public class SshKey implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Getter
    @Column(unique = true)
    private String uid = UUID.randomUUID().toString();

    @Getter
    @Setter
    private Type type;

    @Getter
    @Setter
    private String publicKey;

    @Getter
    @Setter
    private String comment;

    @Getter
    private Timestamp createdTimestamp = Timestamp.from(Instant.now());

    @Getter
    private Timestamp updatedTimestamp = Timestamp.from(Instant.now());

    public void updateTimestamp() {
        updatedTimestamp = Timestamp.from(Instant.now());
    }

    public enum Type {
        SSH_ED25519("ssh-ed25519", new Ed25519SshKeyValidator()),
        SSH_RSA("ssh-rsa", new RsaSshKeyValidator());

        @Getter
        private String description;

        @Getter
        private SshKeyValidator sshKeyValidator;

        Type(String description, SshKeyValidator sshKeyValidator) {
            this.description = description;
            this.sshKeyValidator = sshKeyValidator;
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
