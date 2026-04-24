import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class WebhookSigner {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Produces "sha256=<hex-digest>" - the value to put in X-Webhook-Signature header.
     */
    public String sign(String secretKey, String rawBody) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }

    /**
     * Constant-time comparison - prevents timing attacks.
     */
    public boolean verify(String secretKey, String rawBody, String receivedSignature) {
        String expected = sign(secretKey, rawBody);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }
}