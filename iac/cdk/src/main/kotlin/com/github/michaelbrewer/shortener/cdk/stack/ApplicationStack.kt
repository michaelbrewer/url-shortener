package com.github.michaelbrewer.shortener.cdk.stack

import com.github.eladb.watchful.Watchful
import com.github.eladb.watchful.WatchfulProps
import com.github.michaelbrewer.cdk.stack.BrewStack
import com.github.michaelbrewer.cdk.util.envSettings
import com.github.michaelbrewer.cdk.util.requireNotNull
import software.amazon.awscdk.core.BundlingOptions
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.services.apigateway.LambdaRestApi
import software.amazon.awscdk.services.apigateway.LambdaRestApiProps
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.dynamodb.TableProps
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.FunctionProps
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.lambda.Tracing
import software.amazon.awscdk.services.s3.assets.AssetOptions

class ApplicationStack(
    scope: Construct,
    id: String,
    environment: String,
    branch: String,
    props: StackProps
) : BrewStack(scope, id, environment, branch, props) {
    companion object {
        val DEFAULT_MAX_DURATION = Duration.seconds(5)
        const val DEFAULT_MEMORY_SIZE = 512
    }

    init {
        // Define the table that maps short codes to URLs.
        val table = Table(
            this,
            "Table",
            TableProps
                .builder()
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build()
        )

        // Define the API gateway request handler. All API requests will go to the same function.
        val function = Function(
            this,
            "UrlShortenerFunction",
            FunctionProps
                .builder()
                .code(
                    Code.fromAsset(
                        "../../src/",
                        AssetOptions
                            .builder()
                            .bundling(
                                BundlingOptions
                                    .builder()
                                    .image(Runtime.PYTHON_3_8.bundlingImage)
                                    .command(
                                        mutableListOf(
                                            "bash",
                                            "-c",
                                            "pip install -r requirements.txt -t /asset-output && cp -au . /asset-output"
                                        )
                                    )
                                    .build()
                            )
                            .build()
                    )
                )
                .handler("app.lambda_handler")
                .timeout(DEFAULT_MAX_DURATION)
                .memorySize(DEFAULT_MEMORY_SIZE)
                .runtime(Runtime.PYTHON_3_8)
                .tracing(Tracing.ACTIVE)
                .environment(mutableMapOf(
                    "LOG_LEVEL" to "DEBUG",
                    "POWERTOOLS_SERVICE_NAME" to "virtual-currency-service",
                    "POWERTOOLS_METRICS_NAMESPACE" to "payments",
                    "POWERTOOLS_TRACE_MIDDLEWARES" to "true",
                    "POWERTOOLS_TRACER_CAPTURE_RESPONSE" to "true",
                    "POWERTOOLS_TRACER_CAPTURE_ERROR" to "true",
                    "POWERTOOLS_LOGGER_LOG_EVENT" to "true",
                ))
                .build()
        )

        // Pass the table name to the handler through an environment variable and grant
        // the handler read/write permissions on the table.
        table.grantReadWriteData(function)
        function.addEnvironment("TABLE_NAME", table.tableName)

        // Define the API endpoint and associate the handler
        val api = LambdaRestApi(
            this,
            "UrlShortenerApi",
            LambdaRestApiProps
                .builder()
                .handler(function)
                .build()
        )

        // Map go.<your-configured-domain.com> to this api gateway endpoint
        addSubDomain("go", "UrlShortener", api)

        // Define a Watchful monitoring system and watch the entire scope
        // this will automatically find all watchable resources and add
        // them to our dashboard
        val watchful = Watchful(
            this,
            "watchful",
            WatchfulProps
                .builder()
                .alarmEmail(envSettings(this.node, environment).requireNotNull("watchful_email"))
                .build()
        )
        watchful.watchScope(this)
    }
}
