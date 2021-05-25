package com.github.michaelbrewer.shortener.cdk.stack

import getStackTemplateJson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.StackProps

class ApplicationStackTest {
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
        val stack = ApplicationStack(app, "updates-stack", env, "develop", StackProps.builder().build())
        val jsonNode = getStackTemplateJson(stack)

        val outputValue = jsonNode["Outputs"]["DomainNameOutput"]["Value"].asText()
        Assertions.assertEquals(exampleValue, outputValue)

        println(jsonNode.toPrettyString())
    }
}
