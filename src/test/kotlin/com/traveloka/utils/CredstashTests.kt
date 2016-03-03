package com.traveloka.utils

import org.junit.Test

/**
 * Created by michel on 03/03/16.
 */

class CredstashTests {
    @Test fun testGetItem() {
        println(Credstash.getSecret(
                name="ssh.ansible.public_key",
                //version="0000000000000000001",
                region = "ap-southeast-1"))
    }
}