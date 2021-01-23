package pauljabines.exam.isr.sshkey;

import java.util.Base64;

/**
 * @author Paul Benedict Jabines
 */
public class RsaSshKeyValidator implements SshKeyValidator {
    @Override
    public boolean isValid(String text) {
        if (isValidBase64(text)) {
            return text.length() == 128 ||
                    text.length() == 168 ||
                    text.length() == 216 ||
                    text.length() == 304 ||
                    text.length() == 392 ||
                    text.length() == 564 ||
                    text.length() == 736;
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
