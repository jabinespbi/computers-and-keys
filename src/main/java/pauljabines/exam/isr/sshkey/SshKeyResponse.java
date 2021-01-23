package pauljabines.exam.isr.sshkey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Paul Benedict Jabines
 */
@XmlRootElement
public class SshKeyResponse {

    @XmlElement(name = "ssh-key")
    public SshKeyResponseBody sshKey;

    public static SshKeyResponse toSshKeyResponse(SshKey sshKey) {
        SshKeyResponse sshKeyResponse = new SshKeyResponse();
        sshKeyResponse.sshKey = new SshKeyResponseBody();
        sshKeyResponse.sshKey.type = sshKey.getType().getDescription();
        sshKeyResponse.sshKey.publicKey = sshKey.getPublicKey();
        sshKeyResponse.sshKey.comment = sshKey.getComment();

        return sshKeyResponse;
    }

    public static class SshKeyResponseBody {
        public String type;

        @XmlElement(name = "public")
        public String publicKey;

        public String comment;
    }
}
