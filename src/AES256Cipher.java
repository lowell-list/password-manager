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
import java.util.Properties;

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

/**************************************************************************/
/* INNER CLASSES                                                          */
/**************************************************************************/

/**************************************************************************/
/* STATIC PROPERTIES                                                      */
/**************************************************************************/

private static String ALGO_TRANSFORMATION_STRING    = "AES/GCM/PKCS5Padding" ;        // AES algorithm, with AEAD/GCM mode, and PKCS5Padding padding
private static int SALT_BYTE_LENGTH                 = 128;
private static int DERIVED_KEY_LENGTH               = 256;                            // key length for AES
private static int ITERATION_COUNT                  = 65536;
private static int IV_BYTE_LENGTH                   = 96;
private static int TAG_BIT_LENGTH                   = 128;
private static String ADDITIONAL_AUTHENTICATED_DATA = "AES256Cipher";                 // this plain text is packaged with the encrypted data and authenticated
private static String CIPHER_NAME                   = "AES-256";

// property names
public static String PRPNAM_CIPHERTEXT              = "cipher_text";
public static String PRPNAM_IV                      = "iv";
public static String PRPNAM_AAD_DATA                = "aad_data";
public static String PRPNAM_SALT                    = "salt";
public static String PRPNAM_CIPHERNAME              = "cipher_name";

/**************************************************************************/
/* STATIC METHODS - PUBLIC
/**************************************************************************/

/**
 * Encrypt the source bytes with the secret password and return the result as a Properties object with the following keys:
 *   PRPNAM_CIPHERTEXT - the encrypted bytes, in base 64
 *   PRPNAM_IV         - Initialization Vector bytes, in base 64
 *   PRPNAM_AAD_DATA   - additional authenticated data, plain text
 *   PRPNAM_SALT       - salt bytes used for encryption, in base 64
 *   PRPNAM_CIPHERNAME - name of the cipher, plain text
 *
 * @param password  secret password to use for encryption
 * @param source    source bytes to encrypt
 * @return new Properties object with all PRPNAM_ keys or null on error
*/
public static Properties encrypt(char[] password, byte[] source)
{
	// local variables
	byte[] cphtxt = null; // cipher text
	byte[] slt    = null; // salt
	byte[] inivct = null; // Initialization Vector
	byte[] aaddta = null; // AAD data

	// encrypt
	try {
		// create salt, IV, and additional authenticated data
		slt = generateSalt();
		inivct = generateIV();
		aaddta = ADDITIONAL_AUTHENTICATED_DATA.getBytes("UTF-8");

		// derive secret key from password and salt
		SecretKey srtkey = deriveSecretKey(password, slt);

		// encrypt source bytes
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, inivct);   // init GCM parameters
		Cipher cph = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);                    // get cipher
		cph.init(Cipher.ENCRYPT_MODE, srtkey, gcmParamSpec, new SecureRandom());        // init cipher
		cph.updateAAD(aaddta);                                                          // add AAD tag data before encrypting
		cphtxt = cph.doFinal(source);                                                   // encrypt
	}
	catch(Exception e) {
		System.out.println("there was a problem encrypting");
		e.printStackTrace();
		return null;
	}

	// create and return Properties
	Properties prp = new Properties();
	prp.setProperty(PRPNAM_CIPHERTEXT ,Base64.getEncoder().encodeToString(cphtxt));
	prp.setProperty(PRPNAM_IV         ,Base64.getEncoder().encodeToString(inivct));
	prp.setProperty(PRPNAM_AAD_DATA   ,ADDITIONAL_AUTHENTICATED_DATA);
	prp.setProperty(PRPNAM_SALT       ,Base64.getEncoder().encodeToString(slt));
	prp.setProperty(PRPNAM_CIPHERNAME ,CIPHER_NAME);
	return prp;
}

/**
 * Decrypt the given ciphertext contained in the Properties object using the given password.
 * If an error occurs during decryption, the error is printed and null is returned.
 *
 * @param password    secret password to use for decryption
 * @param properties  Properties object which contains all expected PRPNAM_ keys, usually generated with the encrypt() method
 * @return new array of decrypted bytes or null on error
 */
public static byte[] decrypt(char[] password, Properties properties)
{
	// decrypt
	try {
		// extract data from Properties
		byte[] cphtxt = Base64.getDecoder().decode(properties.getProperty(PRPNAM_CIPHERTEXT));
		byte[] slt    = Base64.getDecoder().decode(properties.getProperty(PRPNAM_SALT));
		byte[] inivct = Base64.getDecoder().decode(properties.getProperty(PRPNAM_IV));
		byte[] aaddta = properties.getProperty(PRPNAM_AAD_DATA).getBytes("UTF-8");
		String cphnam = properties.getProperty(PRPNAM_CIPHERNAME);

		// derive secret key from password and salt
		SecretKey srtkey = deriveSecretKey(password, slt);

		// decrypt ciphertext
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, inivct);   // init GCM parameters
		Cipher cph = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);                    // get cipher
		cph.init(Cipher.DECRYPT_MODE, srtkey, gcmParamSpec, new SecureRandom());        // init cipher
		cph.updateAAD(aaddta);                                                          // add AAD tag data before decrypting
		return cph.doFinal(cphtxt);                                                     // decrypt
	}
	catch(Exception e) {
		System.out.println("there was a problem decrypting");
		e.printStackTrace();
	}
	return null;
}

/**************************************************************************/
/* STATIC METHODS - PRIVATE
/**************************************************************************/

/**
 * Derive secret key from password and salt.
 */
private static SecretKey deriveSecretKey(char[] password, byte[] salt) throws Exception
{
	PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, DERIVED_KEY_LENGTH);
	SecretKeyFactory pbkdfKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	return new SecretKeySpec(pbkdfKeyFactory.generateSecret(keySpec).getEncoded(), "AES"); // associate secret-key algorithm
}

/**
 * Generate and return a secure salt.
 */
private static byte[] generateSalt() throws NoSuchAlgorithmException
{
	byte[] salt = new byte[SALT_BYTE_LENGTH];
	fillWithSecureRandomBytes(salt);
	return salt;
}

/**
 * Generate and return an Initialization Vector (IV).
 */
private static byte[] generateIV() throws NoSuchAlgorithmException
{
	byte[] iv = new byte[IV_BYTE_LENGTH];
	fillWithSecureRandomBytes(iv);
	return iv;
}

/**
 * Fill the given byte array with secure random bytes.
 */
private static void fillWithSecureRandomBytes(byte[] array) throws NoSuchAlgorithmException
{
	SecureRandom secrnd = SecureRandom.getInstance("SHA1PRNG");
	secrnd.nextBytes(array);
}

/**************************************************************************/
/* STATIC INIT & MAIN                                                     */
/**************************************************************************/

} /* END PUBLIC CLASS */
