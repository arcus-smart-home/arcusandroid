/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.presentation.ble

import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESCrypt
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class
    )
    private constructor(
        keyString: String,
        ivString: String,
        deviceMac: String,
        keyByteSize: Int = 16
    ) {

    private val cipher: Cipher
    private val secretKeySpec: SecretKeySpec
    private val initializationVector: AlgorithmParameterSpec

    init {
        // Create key for encryption
        val sharedBytes = convertHexStringToByteArray(keyString)
        val macBytes = convertHexStringToByteArray(deviceMac)
        val iv = convertHexStringToByteArray(ivString)
        val keyBytes = ByteArray(keyByteSize)

        // Massage key data into desired format...
        keyBytes[0] = sharedBytes[0]
        keyBytes[1] = sharedBytes[1]
        keyBytes[2] = macBytes[5]
        keyBytes[3] = sharedBytes[2]
        keyBytes[4] = macBytes[4]
        keyBytes[5] = sharedBytes[3]
        keyBytes[6] = macBytes[3]
        keyBytes[7] = sharedBytes[4]
        keyBytes[8] = sharedBytes[5]
        keyBytes[9] = macBytes[2]
        keyBytes[10] = sharedBytes[6]
        keyBytes[11] = macBytes[1]
        keyBytes[12] = sharedBytes[7]
        keyBytes[13] = macBytes[0]
        keyBytes[14] = sharedBytes[8]
        keyBytes[15] = sharedBytes[9]

        cipher = Cipher.getInstance(CIPHER)
        secretKeySpec = SecretKeySpec(keyBytes, ALGORITHM)
        initializationVector = IvParameterSpec(iv)
    }

    @Throws(
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    fun encrypt(plainText: String): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initializationVector)
        return cipher.doFinal(plainText.toByteArray(charset(CHARSET)))
    }

    companion object {
        private const val SHARED_IV = "BE988AEC227543F692CAB1E7EC7B0AC0"
        private const val SHARED_KEY = "2E2C183DBB77493DB08A"

        private const val CIPHER = "AES/CBC/PKCS7Padding"
        private const val ALGORITHM = "AES"
        private const val CHARSET = "UTF-8"

        private val hexDigits = "0123456789ABCDEF".toCharArray()

        @JvmStatic
        internal fun convertHexStringToByteArray(str: String): ByteArray {
            val len = str.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                val firstChar = Character.digit(str[i], 16) shl 4
                val secondChar = Character.digit(str[i + 1], 16)
                data[i / 2] = (firstChar + secondChar).toByte()

                i += 2
            }
            return data
        }

        @JvmStatic
        fun convertToHexStr(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            var j = 0
            var i = 0
            while (j < bytes.size) {
                val value = bytes[j].toInt() and 0xFF
                hexChars[i] = hexDigits[value.ushr(4)]
                hexChars[i + 1] = hexDigits[value and 0x0F]
                j++
                i += 2
            }
            return String(hexChars)
        }

        @JvmStatic
        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class
        )
        fun forArcusBleDevice(deviceMac: String) = AESCrypt(SHARED_KEY, SHARED_IV, deviceMac)
    }
}
