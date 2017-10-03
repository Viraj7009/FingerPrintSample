package com.example.virajgaikwad.fingerprintsample;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_STORE_ALIAS = "key_fingerprint";
    private static final String KEY_STORE = "AndroidKeyStore";

    private KeyStore keyStore;
    private Cipher cipher;
    private SecretKey key;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);

        final FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        final FingerPrintChecker checker = new FingerPrintChecker(this, fingerprintManager);

        if (checker.isAbleToUseFingerPrint()) {
            generateAuthenticationKey();

            if (isCipherInitialized()){
                final FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

                FingerPrintAuthenticateCallback callback = new FingerPrintAuthenticateCallback() {
                    @Override
                    public void authenticateSuccess() {
                        textView.setText(getString(R.string.success_login));
                    }
                };
                final FingerPrintHelper fingerPrintHelper = new FingerPrintHelper(this, callback);
                fingerPrintHelper.authenticate(fingerprintManager,cryptoObject);
            }
        }
    }



    @TargetApi(Build.VERSION_CODES.M)
    private void generateAuthenticationKey() {
        getKeyStoreInstance();

        final KeyGenerator keyGenerator = getKeyGenerator();

        try {
            keyStore.load(null);

            final KeyGenParameterSpec parameterSpec = getKeyGenParameterSpec();

            // Initialize the key generator
            keyGenerator.init(parameterSpec);

            // Generate the key. This also returns the generated key for immediate use if needed.
            // For this example we will grab it later on.
            key = keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an instance of the Java {@link KeyStore}
     */
    private void getKeyStoreInstance() {
        try {
            keyStore = KeyStore.getInstance(KEY_STORE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the key generator required to generate the keys uses for encryption/decryption
     */
    private KeyGenerator getKeyGenerator() {
        final KeyGenerator keyGenerator;

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        return keyGenerator;
    }

    /**
     * Generate the {@link KeyGenParameterSpec} required for us to encrypt/decrypt.
     */
    @NonNull
    private KeyGenParameterSpec getKeyGenParameterSpec() {
        // Specify what we are trying to do with the generated key
        final int purposes = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;

        // Specifications for the key generator. How to generate the key
        return new KeyGenParameterSpec.Builder(KEY_STORE_ALIAS, purposes)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build();
    }



    /**
     * Initializes the Cipher object required to perform the fingerprint authentication.
     *
     * @return True if Cipher init was successful. False otherwise.
     */
    @TargetApi (Build.VERSION_CODES.M)
    private boolean isCipherInitialized() {
        try {
            // Get a cipher instance with the following transformation --> AES/CBC/PKCS7Padding
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get cipher instance", e);
        }

        try {
            keyStore.load(null);

            // The key - This key was generated in the {@link #generateAuthenticationKey()} method
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (CertificateException | IOException | NoSuchAlgorithmException |
                InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}

