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

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.NoSuchAlgorithmException ;
import java.security.InvalidKeyException;
import javax.crypto.IllegalBlockSizeException ;
import javax.crypto.NoSuchPaddingException ;
import java.security.InvalidAlgorithmParameterException ;
import javax.crypto.BadPaddingException ;
import javax.crypto.ShortBufferException;

import java.util.Arrays ;

public class AES256Cipher
extends java.lang.Object
{

/**************************************************************************/
/* INSTANCE PROPERTIES                                                    */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE CONSTRUCTORS                                                  */
/**************************************************************************/

public AES256Cipher()
{
}

/**************************************************************************/
/* INSTANCE METHODS - ACCESSORS                                           */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE METHODS                                                       */
/**************************************************************************/

/**
 * Encrypt the source bytes and return the result as a new byte array.
 */
public byte[] encrypt(char[] password, byte[] source)
{
	try {
		// derive secret key from password and salt
		SecretKey secretKey = deriveSecretKey(password, generateSalt());

		// create IV
		byte[] iv = generateIV();

		// Initialize GCM Parameters
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

		// tag data
		byte[] aadData = "random".getBytes() ; // Any random data can be used as tag. Some common examples could be domain name...

		byte[] encryptedText = aesEncrypt("Hello world!", secretKey, gcmParamSpec, aadData) ;
		System.out.println("Encrypted Text = " + Base64.getEncoder().encodeToString(encryptedText) ) ;

		byte[] decryptedText = aesDecrypt(encryptedText, secretKey, gcmParamSpec, aadData) ; // Same key, IV and GCM Specs for decryption as used for encryption.
		System.out.println("Decrypted text " + new String(decryptedText)) ;

		// Encrypt the message
/*
		// Cipher e_cipher = Cipher.getInstance("AES/GCM/PKCS5Padding"); // select AES algorithm, with AEAD/GCM mode, and PKCS5Padding padding
		Cipher e_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // select AES algorithm, with AEAD/GCM mode, and PKCS5Padding padding
		e_cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		AlgorithmParameters params = e_cipher.getParameters();
		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] ciphertext = e_cipher.doFinal("Hello, World!".getBytes("UTF-8"));

		// Decrypt the message, given derived key and initialization vector.
		Cipher d_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		d_cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		String plaintext = new String(d_cipher.doFinal(ciphertext), "UTF-8");
		System.out.println(plaintext);
*/
	}
	catch(Exception e) {
		System.out.println("oops: ");
		e.printStackTrace();
	}

	return null;
}

/**
 * Derive secret key from password and salt.
 */
private SecretKey deriveSecretKey(char[] password, byte[] salt) throws Exception
{
	PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, DERIVED_KEY_LENGTH);
	SecretKeyFactory pbkdfKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	return new SecretKeySpec(pbkdfKeyFactory.generateSecret(keySpec).getEncoded(), "AES"); // associate secret-key algorithm
}

/**
 * Generate and return a secure salt.
 */
private byte[] generateSalt() throws NoSuchAlgorithmException
{
	byte[] salt = new byte[SALT_BYTE_LENGTH];
	fillWithSecureRandomBytes(salt);
	/**/System.out.println("generated salt: " + Base64.getEncoder().encodeToString(salt));
	return salt;
}

/**
 * Generate and return an Initialization Vector (IV).
 */
private byte[] generateIV() throws NoSuchAlgorithmException
{
	byte[] iv = new byte[IV_BYTE_LENGTH];
	fillWithSecureRandomBytes(iv);
	/**/System.out.println("generated iv: " + Base64.getEncoder().encodeToString(iv));
	return iv;
}

/**
 * Fill the given byte array with secure random bytes.
 */
private void fillWithSecureRandomBytes(byte[] array) throws NoSuchAlgorithmException
{
	SecureRandom secrnd = SecureRandom.getInstance("SHA1PRNG");
	secrnd.nextBytes(array);
}

public static byte[] aesEncrypt(String message, SecretKey aesKey, GCMParameterSpec gcmParamSpec, byte[] aadData) {
        Cipher c = null ;

        try {
                c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation specifies algortihm, mode of operation and padding
        }catch(NoSuchAlgorithmException noSuchAlgoExc) {System.out.println("Exception while encrypting. Algorithm being requested is not available in this environment " + noSuchAlgoExc); System.exit(1); }
         catch(NoSuchPaddingException noSuchPaddingExc) {System.out.println("Exception while encrypting. Padding Scheme being requested is not available this environment " + noSuchPaddingExc); System.exit(1); }

        
        try {
            c.init(Cipher.ENCRYPT_MODE, aesKey, gcmParamSpec, new SecureRandom()) ;
        } catch(InvalidKeyException invalidKeyExc) {System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc) ; System.exit(1); }
         catch(InvalidAlgorithmParameterException invalidAlgoParamExc) {System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid " + invalidAlgoParamExc) ; System.exit(1); }

       try { 
            c.updateAAD(aadData) ; // add AAD tag data before encrypting
        }catch(IllegalArgumentException illegalArgumentExc) {System.out.println("Exception thrown while encrypting. Byte array might be null " +illegalArgumentExc ); System.exit(1);} 
        catch(IllegalStateException illegalStateExc) {System.out.println("Exception thrown while encrypting. CIpher is in an illegal state " +illegalStateExc); System.exit(1);} 
        catch(UnsupportedOperationException unsupportedExc) {System.out.println("Exception thrown while encrypting. Provider might not be supporting this method " +unsupportedExc); System.exit(1);} 
       
       byte[] cipherTextInByteArr = null ;
       try {
            cipherTextInByteArr = c.doFinal(message.getBytes()) ;
       } catch(IllegalBlockSizeException illegalBlockSizeExc) {System.out.println("Exception while encrypting, due to block size " + illegalBlockSizeExc) ; System.exit(1); }
         catch(BadPaddingException badPaddingExc) {System.out.println("Exception while encrypting, due to padding scheme " + badPaddingExc) ; System.exit(1); }

       return cipherTextInByteArr ;
}


public static byte[] aesDecrypt(byte[] encryptedMessage, SecretKey aesKey, GCMParameterSpec gcmParamSpec, byte[] aadData)
{
	Cipher c = null;

	try {
	   c = Cipher.getInstance(ALGO_TRANSFORMATION_STRING); // Transformation specifies algortihm, mode of operation and padding
	} catch(NoSuchAlgorithmException noSuchAlgoExc) {System.out.println("Exception while decrypting. Algorithm being requested is not available in environment " + noSuchAlgoExc); System.exit(1); }
	 catch(NoSuchPaddingException noSuchAlgoExc) {System.out.println("Exception while decrypting. Padding scheme being requested is not available in environment " + noSuchAlgoExc); System.exit(1); }  

	try {
	    c.init(Cipher.DECRYPT_MODE, aesKey, gcmParamSpec, new SecureRandom()) ;
	} catch(InvalidKeyException invalidKeyExc) {System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc) ; System.exit(1); }
	 catch(InvalidAlgorithmParameterException invalidParamSpecExc) {System.out.println("Exception while encrypting. Algorithm Param being used is not valid. " + invalidParamSpecExc) ; System.exit(1); }

	try {
	    c.updateAAD(aadData) ; // Add AAD details before decrypting
	}catch(IllegalArgumentException illegalArgumentExc) {System.out.println("Exception thrown while encrypting. Byte array might be null " +illegalArgumentExc ); System.exit(1);}
	catch(IllegalStateException illegalStateExc) {System.out.println("Exception thrown while encrypting. CIpher is in an illegal state " +illegalStateExc); System.exit(1);}

	byte[] plainTextInByteArr = null ;
	try {
	    plainTextInByteArr = c.doFinal(encryptedMessage) ;
	} catch(IllegalBlockSizeException illegalBlockSizeExc) {System.out.println("Exception while decryption, due to block size " + illegalBlockSizeExc) ; System.exit(1); }
	 catch(BadPaddingException badPaddingExc) {System.out.println("Exception while decryption, due to padding scheme " + badPaddingExc) ; System.exit(1); }

	return plainTextInByteArr ;
}

/**************************************************************************/
/* INNER CLASSES                                                          */
/**************************************************************************/

/**************************************************************************/
/* STATIC PROPERTIES                                                      */
/**************************************************************************/

public static String ALGO_TRANSFORMATION_STRING = "AES/GCM/PKCS5Padding" ;
public static int SALT_BYTE_LENGTH      = 128;
public static int DERIVED_KEY_LENGTH    = 256;          // select key length for AES
public static int ITERATION_COUNT       = 65536;
public static int IV_BYTE_LENGTH        = 96;
public static int TAG_BIT_LENGTH        = 128;

/**************************************************************************/
/* STATIC INIT & MAIN                                                     */
/**************************************************************************/

} /* END PUBLIC CLASS */
