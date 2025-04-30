package kr.ssok.bank.common.util;

import jakarta.annotation.PostConstruct;
import kr.ssok.bank.common.constant.FailureStatusCode;
import kr.ssok.bank.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AESUtil {

    @Value("${encryption.key}")
    private String key;

    private SecretKeySpec keySpec;

    private static final String ALGORITHM = "AES";

    @PostConstruct
    public void init() {
        this.keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BaseException(FailureStatusCode.AES_ENCRYPT_FAILED);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new BaseException(FailureStatusCode.AES_DECRYPT_FAILED);
        }
    }
}