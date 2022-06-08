package com.oone.paymentSdk

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.spec.KeySpec
import android.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class data {
    fun makePbeKey(password: CharArray?): SecretKey? {
        val iterations = 100
        val keyLength = 128
        val salt = "salt"
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(password, salt.toByteArray(), iterations, keyLength)
        val key: SecretKey = factory.generateSecret(spec)
        val secret: SecretKey = SecretKeySpec(key.getEncoded(), "AES")
//        System.out.println("secret " + android.util.Base64.encodeToString(secret.getEncoded(),Base64.DEFAULT))
        return secret
    }

    fun cbcEncrypt(key: SecretKey, data: String): ByteArray? {
        /* Encrypt the message. */
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = makeIV(key)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val ciphertext: ByteArray = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return ciphertext
    }

    fun makeIV(key: SecretKey): ByteArray {

        val base64Secret: String = android.util.Base64.encodeToString(key.getEncoded(),Base64.DEFAULT)
        //base64 to byte array
        val decode: ByteArray = android.util.Base64.decode(base64Secret,Base64.DEFAULT)

        //byte array to hex
        val hexSecret = java.lang.String.format("%02x", BigInteger(1, decode))
//        println("hexSecret$hexSecret")
        val cut16 = hexSecret.substring(0, 16)
        val cutToByte: ByteArray = cut16.toByteArray(StandardCharsets.UTF_8)
//        println(cutToByte.size)
        return cutToByte
    }

    fun cbcDecrypt(key: SecretKey, cipherText: ByteArray?): String? {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = makeIV(key)
//        println("IV" + iv.contentToString())
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
    }
}