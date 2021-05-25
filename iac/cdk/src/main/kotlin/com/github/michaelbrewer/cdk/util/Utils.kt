package com.github.michaelbrewer.cdk.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.ConstructNode
import software.amazon.awscdk.core.Stack

fun envName(app: App): String {
    return app.node.tryGetContext("env") as String? ?: "dev"
}

fun branchName(app: App): String {
    return app.node.tryGetContext("branch") as String? ?: "develop"
}

fun nameSpace(environment: String, branch: String): String {
    return "$environment${if (branch == "master") "" else "-$branch"}"
}

fun envSettings(node: ConstructNode, environment: String): Map<String, String> {
    @Suppress("UNCHECKED_CAST")
    return requireNotNull(node.tryGetContext("env_$environment") as Map<String, String>?) {
        "settings needs to be configured for $environment"
    }
}

fun Map<String, String>.requireNotNull(name: String) = requireNotNull(this[name]) {
    "'$name' needs to be configured in cdk.json"
}
