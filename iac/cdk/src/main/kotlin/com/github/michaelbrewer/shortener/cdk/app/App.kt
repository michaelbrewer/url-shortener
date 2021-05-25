package com.github.michaelbrewer.shortener.cdk.app

import com.github.michaelbrewer.cdk.util.branchName
import com.github.michaelbrewer.cdk.util.envName
import com.github.michaelbrewer.cdk.util.nameSpace
import com.github.michaelbrewer.shortener.cdk.stack.ApplicationStack
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.core.Tags


/**
 * Build out our stacks.
 *
 * Call directly from the unit tests.
 */
fun buildStacks(app: App) {
    val environment = envName(app)
    val branch = branchName(app)
    val namespace = nameSpace(environment, branch)

    Tags.of(app).add("project", "url-shortener")
    Tags.of(app).add("environment", environment)
    Tags.of(app).add("branch", branch)

    ApplicationStack(
        app,
        "url-shortener-$namespace",
        environment,
        branch,
        StackProps
                .builder()
                .description("url-shortener application stack. environment: ($environment) branch: ($branch)")
                .build()
    )

    app.synth()
}

fun main() = buildStacks(App())
