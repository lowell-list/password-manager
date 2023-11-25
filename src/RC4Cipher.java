/* --------------------------------------------------------------------------
 * Description : RC4Cipher
 * Author      : Lowell List
 * Date        : 04 Mar 2002
 * Origin OS   : Windows 2000
 * --------------------------------------------------------------------------
 * Copyright (c) 2002 Lowell List
 * -------------------------------------------------------------------------- */

public class RC4Cipher
extends java.lang.Object
{

/**************************************************************************/
/* INSTANCE PROPERTIES                                                    */
/**************************************************************************/

private byte[]      state;                        // RC4 state array
private int         index1,index2;                // RC4 state indeces

/**************************************************************************/
/* INSTANCE CONSTRUCTORS                                                  */
/**************************************************************************/

public RC4Cipher(byte[] key) {
    state=new byte[256];
    initState(key,0,key.length);
    }

/**************************************************************************/
/* INSTANCE METHODS - ACCESSORS                                           */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE METHODS                                                       */
/**************************************************************************/

private void initState(byte[] key, int off, int len) {
    int             xa,xk,xs;                               // loop variables: general, key, and state indices
    byte            tmp;                                    // temporary byte used for swapping

    for(xs=0; xs<256; xs++) { state[xs]=(byte)xs; }         // initialize RC4 state and state indices
    index1=0; index2=0;
    for(xa=0,xs=0,xk=off; xs<256; xs++,xk++) {              // perform RC4 "mixing operation" on the state array
        if(xk>=off+len) { xk=off; }
        //xa=(xa+key[xk]+state[xs])&0xFF;                   // AP: wrong output using IE4 JVM, and diags hide problem!!
        xa+=key[xk]; xa+=state[xs]; xa&=0xFF;               // AP: use this instead
        tmp=state[xa]; state[xa]=state[xs]; state[xs]=tmp;
        }
    for(xk=off; xk<off+len; xk++) { key[xk]=0; }            // destroy key by overwriting with 0s
    }

public void encrypt(byte[] src, byte[] tgt) {
    transform(src,0,src.length,tgt,0,tgt.length);
    }

public void encrypt(byte[] src, int so, int sl, byte[] tgt, int to, int tl) {
    transform(src,so,sl,tgt,to,tl);
    }

public void decrypt(byte[] src, byte[] tgt) {
    transform(src,0,src.length,tgt,0,tgt.length);
    }

public void decrypt(byte[] src, int so, int sl, byte[] tgt, int to, int tl) {
    transform(src,so,sl,tgt,to,tl);
    }

/**
 * Transforms bytes using the RC4 cipher algorithm. The number of bytes transformed will be
 * either sl or tl, whichever is smaller.
 *
 * @param src  source array - the bytes to transform
 * @param so   source array offset
 * @param sl   source array length
 * @param tgt  target array - receives transformed bytes
 * @param to   target array offset
 * @param tl   target array length
 */
private void transform(byte[] src, int so, int sl, byte[] tgt, int to, int tl) {
    byte            ti,tj;              // temp variables
    int             xs,xt;              // source and target indices
    int             se,te;              // source and target: end indices

    se=so+sl; te=to+tl;
    for(xs=so,xt=to; xs<se && xt<te; xs++,xt++) {
        index1=(index1+1)&0xFF;         // increment index1 and wrap
        ti=state[index1];
        index2=(index2+ti)&0xFF;        // change index2 and wrap
        tj=state[index2];
        state[index1]=tj;               // swap state values
        state[index2]=ti;
        tj=state[(tj+ti)&0xFF];         // generate byte for encryption/decryption
        tgt[xt]=(byte)(src[xs]^tj);     // XOR buffer byte with encryption/decryption byte
        }
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
