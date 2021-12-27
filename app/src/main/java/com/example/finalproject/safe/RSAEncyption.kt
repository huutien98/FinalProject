package com.example.finalproject.safe

import java.io.IOException
import java.security.*
import javax.crypto.Cipher

object RSAEncyption {
    @Throws(NoSuchAlgorithmException::class)
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator =
            KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    @Throws(IOException::class)
    fun encryptData(data: String, pubKey: PublicKey?): ByteArray? {
        val dataToEncrypt = data.toByteArray()
        var encryptedData: ByteArray? = null
        try {
            val cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.ENCRYPT_MODE, pubKey)
            encryptedData = cipher.doFinal(dataToEncrypt)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return encryptedData
    }

    @Throws(IOException::class)
    fun decryptData(data: ByteArray?, privateKey: PrivateKey?): String {
        var decryptedData: ByteArray? = null
        try {
            val cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            decryptedData = cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return try {
            String(decryptedData!!)
        } catch (e: Exception) {
            ""
        }
    }
}