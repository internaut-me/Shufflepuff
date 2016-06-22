package com.shuffle.bitcoin.impl;

import com.google.inject.Guice;
import com.shuffle.JvmModule;
import com.shuffle.bitcoin.BitcoinCrypto;
import com.shuffle.bitcoin.DecryptionKey;
import com.shuffle.bitcoin.EncryptionKey;

import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;

import static org.junit.Assert.assertEquals;

/**
 * Created by conta on 02.06.16.
 */
public class DecryptionKeyImplTest {
    ECKey ecKey;
    BitcoinCrypto bitcoinCrypto;
    DecryptionKey decryptionKey;
    SecureRandom secureRandom;
    EncryptionKey encryptionKey;

    @Before
    public void setUp() throws Exception {
        // The module also initializes the BouncyCastle crypto
        Guice.createInjector(new JvmModule()).injectMembers(this);
        this.bitcoinCrypto = new BitcoinCrypto();
        this.secureRandom = new SecureRandom();
        this.ecKey = new ECKey(secureRandom);
        this.decryptionKey = new DecryptionKeyImpl(this.ecKey);
    }

    @Test
    public void testECIESAvailability() throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECIES",new BouncyCastleProvider());
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256k1"));

        KeyPair recipientKeyPair = keyPairGenerator.generateKeyPair();
        PublicKey pubKey = recipientKeyPair.getPublic();
        PrivateKey privKey = recipientKeyPair.getPrivate();


        // init the encryption cipher
        Cipher iesCipher = Cipher.getInstance("ECIES");
        iesCipher.init(Cipher.ENCRYPT_MODE, pubKey);

        // use the cipher
        String message = "Hello ECIES World!";
        byte[] encryptedMessage = iesCipher.doFinal(message.getBytes());

        // init the decryption cipher
        iesCipher.init(Cipher.DECRYPT_MODE, privKey);
        String decryptedMessage = new String(iesCipher.doFinal(encryptedMessage));

        System.out.println(message);
        System.out.println(pubKey);
        System.out.println(privKey);
        System.out.println(Arrays.toString(privKey.getEncoded()));
        System.out.println(" -> " + Hex.encodeHexString(encryptedMessage));
        System.out.println(" -> " + decryptedMessage);
    }

    @Test
    public void testToString() throws Exception {
        System.out.println("\nBegin Test toString:");
        String string = this.ecKey.getPrivateKeyAsWiF(bitcoinCrypto.getParams());
        System.out.println("ECKey: " + this.ecKey);
        System.out.println("String ECKey WIF: " + string);
        System.out.println("String DecryptionKey: " + this.decryptionKey.toString());
        assertEquals("toString Method: ", string, this.decryptionKey.toString());

    }

    @Test
    public void testEncryptionKey() throws Exception {

        System.out.println("\nBegin Test encryptionKey:");
        byte[] pub = ECKey.publicKeyFromPrivate(ecKey.getPrivKey(), ecKey.isCompressed());
        EncryptionKey encryptionKey1 = new EncryptionKeyImpl(ECKey.fromPublicOnly(ecKey.getPubKey()));
//
//      PublicKey publicKey = BitcoinCrypto.loadPublicKey(Base64.getEncoder().encodeToString(ecKey.getPubKey()));

        System.out.println("ecKey: " + ecKey.toString());
        System.out.println("ecKey priv: " + ecKey.getPrivateKeyAsHex());
        System.out.println("secureRandom: " + secureRandom.toString());
        System.out.println("decryptionKey: " + decryptionKey.toString());
        System.out.println("ASN.1  " + Arrays.toString(ecKey.toASN1()));

        EncryptionKeyImpl encTest = new EncryptionKeyImpl(pub);
        System.out.println("\nencTest: " + encTest);
        System.out.println("encryptionKey: " + encryptionKey1);
        System.out.println("EncKey.toString from ECKeys Pub: " + encTest.toString());
        System.out.println("EncKey from DecKey to string: " + decryptionKey.EncryptionKey().toString());

        encryptionKey = new EncryptionKeyImpl(ecKey.getPubKey());
        assertEquals(encryptionKey.toString(), decryptionKey.EncryptionKey().toString());

    }

    @Test
    public void testDecrypt() throws Exception {


    }
}