package pauljabines.exam.isr.sshkey;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Paul Benedict Jabines
 */
@XmlRootElement
public class SshKeyResponse {

    public SshKeyResponseBody sshKey;

    public static SshKeyResponse toSshKeyResponse(SshKey sshKey) {
        SshKeyResponse sshKeyResponse = new SshKeyResponse();
        sshKeyResponse.sshKey = new SshKeyResponseBody();
        sshKeyResponse.sshKey.name = sshKey.getName();
        sshKeyResponse.sshKey.type = sshKey.getType().getDescription();
        sshKeyResponse.sshKey.publicKey = sshKey.getPublicKey();
        sshKeyResponse.sshKey.comment = sshKey.getComment();
        sshKeyResponse.sshKey.accessRights = sshKey.getAccessRights().getName();

        return sshKeyResponse;
    }

    public static class SshKeyResponseBody {
        public String name;

        public String type;

        public String publicKey;

        public String comment;

        public String accessRights;
    }
}
