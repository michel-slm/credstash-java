package com.traveloka.utils

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import java.util.*

/**
 * Created by michel on 24/02/16.
 */
object Credstash {

    val DEFAULT_REGION = "us-east-1"
    val PAD_LEN = 19
    val WILDCARD_CHAR = '*'

    fun getSecret(
            name: String,
            version: String = "",
            region: String? = null,
            tableName: String = "credential-store",
            context: Map<String, String> = mapOf(),
            profileName: String? = null) {
        val dynamoDB = DynamoDB(AmazonDynamoDBClient(ProfileCredentialsProvider()))
        val secrets = dynamoDB.getTable(tableName)
        val material: Item
        if (version.isEmpty()) {
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

        } else {
            val response = secrets.getItem(
                    GetItemSpec()
                            .withPrimaryKey(
                                    KeyAttribute("name", name),
                                    KeyAttribute("version", version)))


        }


    }
}
