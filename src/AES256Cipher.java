/* --------------------------------------------------------------------------
 * Description : AES256Cipher
 * Author      : Lowell List
 * Date        : 07 Nov 2017
 * Origin OS   : Mac OS X 10.12.6
 * --------------------------------------------------------------------------
 * Copyright (c)  2017 Lowell List
 * -------------------------------------------------------------------------- */

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES256Cipher
extends java.lang.Object
{

/**************************************************************************/
/* INSTANCE PROPERTIES                                                    */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE CONSTRUCTORS                                                  */
/**************************************************************************/

public AES256Cipher() {

    try {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] salt = generateSalt();
        /**/System.out.println(salt);
        PBEKeySpec spec = new PBEKeySpec("password".toCharArray(), salt, 65536, 256);
        /**/System.out.println(factory);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        /* Encrypt the message. */
        Cipher e_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        e_cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = e_cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = e_cipher.doFinal("Hello, World!".getBytes("UTF-8"));

        /* Decrypt the message, given derived key and initialization vector. */
        Cipher d_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        d_cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        String plaintext = new String(d_cipher.doFinal(ciphertext), "UTF-8");
        System.out.println(plaintext);
    }
    catch(Exception e) {
        System.out.println("oops: ");
        e.printStackTrace();
    }
}

/**************************************************************************/
/* INSTANCE METHODS - ACCESSORS                                           */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE METHODS                                                       */
/**************************************************************************/

private byte[] generateSalt() throws Exception {
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    byte[] salt = new byte[8];
    sr.nextBytes(salt);
    return salt;
}

/**************************************************************************/
/* INNER CLASSES                                                          */
/**************************************************************************/

/**************************************************************************/
/* STATIC PROPERTIES                                                      */
/**************************************************************************/

/**************************************************************************/
/* STATIC INIT & MAIN                                                     */
/**************************************************************************/

} /* END PUBLIC CLASS */
