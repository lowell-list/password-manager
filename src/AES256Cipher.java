/* --------------------------------------------------------------------------
 * Description : AES256Cipher
 * Author      : Lowell List
 * Date        : 07 Nov 2017
 * Origin OS   : Mac OS X 10.12.6
 * --------------------------------------------------------------------------
 * Copyright (c) 2017 Lowell List
 * -------------------------------------------------------------------------- */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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

// misc settings
private static String ALGO_TRANSFORMATION_STRING    = "AES/CBC/PKCS5Padding" ;        // AES algorithm, with CBC mode, and PKCS5Padding padding
private static int SALT_BYTE_LENGTH                 = 128;
private static int DERIVED_KEY_LENGTH               = 256;                            // key length for AES
private static int ITERATION_COUNT                  = 65536;
private static int IV_BYTE_LENGTH                   = 16;
private static int RANDOM_HEADER_BYTE_LENGTH        = 4;
private static String ADDITIONAL_AUTHENTICATED_DATA = "AES256Cipher";                 // this plain text is packaged with the encrypted data and authenticated
private static String CIPHER_NAME                   = "AES-256";                      // name of the cipher used by this class

// property names
public static String PRPNAM_CIPHERNAME              = "cipher_name";
public static String PRPNAM_AAD_DATA                = "aad_data";
public static String PRPNAM_IV                      = "iv";
public static String PRPNAM_SALT                    = "salt";
public static String PRPNAM_ENCRYPTED_BYTES         = "encrypted_bytes";

/**************************************************************************/
/* STATIC METHODS - PUBLIC
/**************************************************************************/

/**
 * Encrypt the message bytes with the secret password and return the result as a serialized
 * Properties object that contains the following keys:
 *   PRPNAM_CIPHERNAME      - name of the cipher, plain text
 *   PRPNAM_AAD_DATA        - additional authenticated data, plain text
 *   PRPNAM_IV              - Initialization Vector bytes, in base 64
 *   PRPNAM_SALT            - salt bytes used for encryption, in base 64
 *   PRPNAM_ENCRYPTED_BYTES - the encrypted bytes, in base 64
 *
 * @param password  secret password to use for encryption
 * @param message   plain text message bytes to encrypt
 * @return serialized Properties object as a byte array or null on error
*/
public static byte[] encrypt(char[] password, byte[] message)
{
	// local variables
	byte[] inivct = null; // Initialization Vector
	byte[] slt    = null; // salt
	byte[] encbyt = null; // encrypted bytes
	byte[] rndbyt = null; // random header bytes

	// encrypt
	try {
		// create salt, IV, and additional authenticated data
		slt = generateSalt();
		inivct = generateIV();
		rndbyt = new byte[RANDOM_HEADER_BYTE_LENGTH];
		fillWithSecureRandomBytes(rndbyt);

		// derive secret key from password and salt
		SecretKey srtkey = deriveSecretKey(password, slt);

		// encrypt message bytes
		IvParameterSpec iv = new IvParameterSpec(inivct);                               // create IV
		Cipher cph = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);                    // get cipher
		cph.init(Cipher.ENCRYPT_MODE, srtkey, iv);                                      // initialize cipher
		encbyt = cph.doFinal(message);                                                  // encrypt
	}
	catch(Exception e) {
		System.out.println("there was a problem encrypting");
		e.printStackTrace();
		return null;
	}

	// create Properties
	Properties prp = new Properties();
	prp.setProperty(PRPNAM_CIPHERNAME      ,CIPHER_NAME);
	prp.setProperty(PRPNAM_AAD_DATA        ,ADDITIONAL_AUTHENTICATED_DATA);
	prp.setProperty(PRPNAM_IV              ,Base64.getEncoder().encodeToString(inivct));
	prp.setProperty(PRPNAM_SALT            ,Base64.getEncoder().encodeToString(slt));
	prp.setProperty(PRPNAM_ENCRYPTED_BYTES ,Base64.getEncoder().encodeToString(encbyt));

	// serialize Properties
	ByteArrayOutputStream bytoutstm = new ByteArrayOutputStream();
	try {
		bytoutstm.write(rndbyt); // first, write some random bytes
		prp.store(bytoutstm,"envelope");
	}
	catch(Exception e) {
		System.out.println("could not serialize Properties");
		e.printStackTrace();
		return null;
	}

	// return Properties as byte array
	return bytoutstm.toByteArray();
}

/**
 * Decrypt the given encrypted bytes contained in the serialized Properties object using the given password.
 * If an error occurs during decryption, the error is printed and null is returned.
 *
 * @param password              secret password to use for decryption
 * @param serializedProperties  serialized Properties object which contains all expected 
 *                              PRPNAM_ keys, generated by the encrypt() method
 * @return decrypted message bytes or null on error
 */
public static byte[] decrypt(char[] password, byte[] serializedProperties)
{
	// decrypt
	try {
		// load Properties from byte array
		ByteArrayInputStream bytinpstm =
			new ByteArrayInputStream(serializedProperties,RANDOM_HEADER_BYTE_LENGTH,serializedProperties.length-RANDOM_HEADER_BYTE_LENGTH);
		Properties prp = new Properties();
		prp.load(bytinpstm);

		// extract data from Properties
		byte[] inivct = Base64.getDecoder().decode(prp.getProperty(PRPNAM_IV));
		byte[] slt    = Base64.getDecoder().decode(prp.getProperty(PRPNAM_SALT));
		byte[] encbyt = Base64.getDecoder().decode(prp.getProperty(PRPNAM_ENCRYPTED_BYTES));

		// ensure that authenticated data is unaltered
		if(!prp.getProperty(PRPNAM_AAD_DATA).equals(ADDITIONAL_AUTHENTICATED_DATA)) {
			System.out.println("unexpected authenticated data");
			return null;
		}

		// derive secret key from password and salt
		SecretKey srtkey = deriveSecretKey(password, slt);

		// decrypt ciphertext
		IvParameterSpec iv = new IvParameterSpec(inivct);                               // create IV
		Cipher cph = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);                    // get cipher
		cph.init(Cipher.DECRYPT_MODE, srtkey, iv);                                      // initialize cipher
		return cph.doFinal(encbyt);                                                     // decrypt
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
