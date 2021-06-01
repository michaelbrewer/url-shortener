package com.github.michaelbrewer.shortener.cdk.stack

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbrewer.shortener.cdk.app.buildStacks
import getStackTemplateJson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.StackProps

class ApplicationStackTests {
    @Test
    fun checkCanSynth() {
        val app = App()
        app.node.tryRemoveChild("Tree")
        val baseDomainName = "brew.co"
        val exampleValue = "https://go.$baseDomainName/"
        val env = "testEnv"
        app.node.setContext("env", env)
        app.node.setContext(
            "env_$env",
            mapOf(
                "zone_name" to baseDomainName,
                "hosted_zone_id" to "test",
                "certificate_id" to "certificate_id",
                "watchful_email" to "watchful_email",
            )
        )

        buildStacks(app)

        val jsonNode: JsonNode = ObjectMapper().valueToTree(app.synth().stacks.first().template)

        val outputValue = jsonNode["Outputs"]["DomainNameOutput"]["Value"].asText()
        Assertions.assertEquals(exampleValue, outputValue)

        println(jsonNode.toPrettyString())
    }
}
