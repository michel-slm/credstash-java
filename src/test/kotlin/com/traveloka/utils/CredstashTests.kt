package com.traveloka.utils

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import org.junit.Test

/**
 * Created by michel on 03/03/16.
 */

class CredstashTests {
    @Test fun testGetLatestItem() {
        println(Credstash.getSecret(
                name="ssh.ansible.public_key",
                region = Region.getRegion(Regions.AP_SOUTHEAST_1)))
    }
    @Test fun testGetVersionedItem() {
        println(Credstash.getSecret(
                name="ssh.ansible.public_key",
                version="0000000000000000001",
                region = Region.getRegion(Regions.AP_SOUTHEAST_1),
                context = mapOf(Pair("app", "ansible"))))
    }
}