package pauljabines.exam.isr.sshkey;

import java.util.Base64;

/**
 * @author Paul Benedict Jabines
 */
public class Ed25519SshKeyValidator implements SshKeyValidator {
    @Override
    public boolean isValid(String text) {
        if (isValidBase64(text)) {
            return text.length() == 68;
        }

        return false;
    }

    private boolean isValidBase64(String text) {
        try {
            Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
