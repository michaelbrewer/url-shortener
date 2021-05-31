package com.github.michaelbrewer.cdk.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.awscdk.core.App

class TestUtils {
    @Test
    fun testEnvName() {
        val app = App()
        app.node.tryRemoveChild("Tree")
        Assertions.assertEquals("dev", envName(app))
    }

    @Test
    fun testBranchName() {
        val app = App()
        app.node.tryRemoveChild("Tree")
        app.node.setContext("branch", "foo")
        Assertions.assertEquals("foo", branchName(app))
    }

    @Test
    fun testNameSpace() {
        Assertions.assertEquals("env", nameSpace("env", "master"))
    }

    @Test
    fun testRequiredNotNull() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            mapOf("1" to "2").requireNotNull("foo")
        }
        Assertions.assertEquals("'foo' needs to be configured in cdk.json", exception.message)
    }

    @Test
    fun testEnvSettings() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            val app = App()
            app.node.tryRemoveChild("Tree")
            envSettings(app.node, "foo")
        }
        Assertions.assertEquals("settings needs to be configured for foo", exception.message)
    }
}
