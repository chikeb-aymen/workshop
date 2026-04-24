package com.workshop.architecture.webhook.github;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class GitHubSignatureVerifier {

    private static final String PREFIX_SHA1 = "sha1=";
    private static final String PREFIX = "sha256=";

    public String sign(String secret, byte[] rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody);
            return PREFIX + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute GitHub webhook signature", e);
        }
    }

    public String signSha1(String secret, byte[] rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody);
            return PREFIX_SHA1 + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute GitHub webhook signature", e);
        }
    }

    public boolean verify(String secret, byte[] rawBody, String headerValue) {
        if (headerValue == null || !headerValue.startsWith(PREFIX)) {
            return false;
        }
        String expected = sign(secret, rawBody);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                headerValue.getBytes(StandardCharsets.UTF_8)
        )
    }
}
