package pauljabines.exam.isr.sshkey;

import lombok.Getter;
import org.mindrot.jbcrypt.BCrypt;

/**
 * @author Paul Benedict Jabines
 */
public class SshKeyRequest {
    private static final int BCRYPT_ROUNDS = 10;
    public SshKeyRequestBody sshKey;

    public SshKey toSshKey() {
        SshKey sshKey = new SshKey();
        sshKey.setName(this.sshKey.name);
        sshKey.setType(SshKey.Type.fromDescription(this.sshKey.type));

        String salt = BCrypt.gensalt(BCRYPT_ROUNDS);
        String hashedKey = BCrypt.hashpw(this.sshKey.publicKey, salt);

        sshKey.setPublicKey(hashedKey);
        sshKey.setComment(this.sshKey.comment);
        sshKey.setAccessRights(SshKey.AccessRights.fromName(this.sshKey.accessRights));

        return sshKey;
    }

    public Status validate() {
        if (sshKey == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        if (sshKey.name == null ||
                sshKey.type == null ||
                sshKey.publicKey == null ||
                sshKey.comment == null ||
                sshKey.accessRights == null) {
            return Status.NULL_VALUES_ENCOUNTERED;
        }

        SshKey.Type type;
        try {
            type = SshKey.Type.fromDescription(sshKey.type);
        } catch (IllegalArgumentException e) {
            return Status.TYPE_NOT_SUPPORTED;
        }

        try {
            SshKey.AccessRights.fromName(sshKey.accessRights);
        } catch (IllegalArgumentException e) {
            return Status.ACCESS_RIGHTS_NOT_SUPPORTED;
        }

        if (!type.getSshKeyValidator().isValid(sshKey.publicKey)) {
            return Status.KEY_INVALID;
        }

        return Status.OK;
    }

    public static class SshKeyRequestBody {
        public String name;

        public String type;

        public String publicKey;

        public String comment;

        public String accessRights;
    }

    public enum Status {
        TYPE_NOT_SUPPORTED("Type not supported!"),
        ACCESS_RIGHTS_NOT_SUPPORTED("Access rights not supported!"),
        KEY_INVALID("The content of the public key is invalid for the type ‘ssh-rsa’"),
        NULL_VALUES_ENCOUNTERED("Null values encountered"),
        OK("Ok");

        @Getter
        private String description;

        Status(String description) {
            this.description = description;
        }
    }
}
