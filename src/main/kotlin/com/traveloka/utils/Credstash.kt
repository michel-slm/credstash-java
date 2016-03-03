package com.traveloka.utils

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.EnableKeyRequest
import java.nio.ByteBuffer
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * Created by michel on 24/02/16.
 */
object Credstash {

    val DEFAULT_REGION = Region.getRegion(Regions.US_EAST_1)
    val PAD_LEN = 19
    val WILDCARD_CHAR = '*'

    fun getSecret(
            name: String,
            version: String = "",
            region: Region = DEFAULT_REGION,
            tableName: String = "credential-store",
            context: Map<String, String> = mapOf(),
            profileName: String? = null) {
        val client = AmazonDynamoDBClient(ProfileCredentialsProvider())

        val dynamoDB = DynamoDB(client)

        val secrets = dynamoDB.getTable(tableName)
        val material: Item
        if (version.isBlank()) {
            val querySpec = QuerySpec()
                    .withMaxResultSize(1)
                    .withScanIndexForward(false)
                    .withConsistentRead(true)
                    .withKeyConditionExpression("#n = :n")
                    .withNameMap(mapOf(Pair("#n", "name")))
                    .withValueMap(mapOf(Pair(":n", name)))
            val response = secrets.query(querySpec)

            if (response.count() == 0) {
                throw NoSuchElementException()
            }
            material = response.first()
            println(material.get("version"))

        } else {
            println("Got here - VERSIONED")
            val response = secrets.getItem(
                    GetItemSpec()
                            .withPrimaryKey(
                                    KeyAttribute("name", name),
                                    KeyAttribute("version", version)))

            println(response.get("version"))
            println(decryptSecret(
                    secretItem = response,
                    context = context,
                    region = region))
        }


    }

    fun decryptSecret(secretItem: Item,
                      context: Map<String, String> = mapOf(),
                      region: Region): String {
        val client = AWSKMSClient(ProfileCredentialsProvider())
        client.setRegion(region)

        // or getMimeDecoder ? the MimeDecoder output matches that of Python's
        val keyEncodedString = secretItem.getString("key")
        val keyBlob = ByteBuffer.wrap(Base64.getDecoder().decode(keyEncodedString))
        println("Done")
        val kms_response = client.decrypt(DecryptRequest()
                .addEncryptionContextEntry("app", "ansible")
                .withCiphertextBlob(keyBlob))
                .plaintext.array()
        println(kms_response.count())
        val decryptor = Cipher.getInstance("AES")
        val key = SecretKeySpec(kms_response.slice(0..31).toByteArray(), "AES")
        decryptor.init(Cipher.DECRYPT_MODE, key)
        val plaintext = decryptor.doFinal(secretItem.getByteBuffer("contents").array())
        println(String(plaintext))
        return ""
    }
}
