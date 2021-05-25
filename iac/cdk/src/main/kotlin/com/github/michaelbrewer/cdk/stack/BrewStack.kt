package com.github.michaelbrewer.cdk.stack

import com.github.michaelbrewer.cdk.util.envSettings
import com.github.michaelbrewer.cdk.util.nameSpace
import com.github.michaelbrewer.cdk.util.requireNotNull
import software.amazon.awscdk.core.ArnComponents
import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.CfnOutputProps
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.services.apigateway.DomainNameOptions
import software.amazon.awscdk.services.apigateway.RestApi
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.route53.ARecord
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneAttributes
import software.amazon.awscdk.services.route53.RecordTarget
import software.amazon.awscdk.services.route53.targets.ApiGateway

/**
 * BrewStack common code.
 *
 * Common code that can be shared with stacks at the company.
 */
open class BrewStack(scope: Construct,
        id: String,
        environment: String,
        branch: String,
        props: StackProps
) : Stack(scope, id, props) {
    private val namespace = nameSpace(environment, branch)
    private val settings = envSettings(this.node, environment)

    /**
     * Add subdomain record to our configured root domain
     *
     * @param subDomain The sub domain name to add
     * @param label Label of the subdomain
     * @param api Api to add
     */
    fun addSubDomain(subDomain: String, label: String, api: RestApi) {
        val baseDomainName = settings.requireNotNull("zone_name")
        val domainName = "$subDomain.$baseDomainName"

        // Add subdomain mapping
        val certificateId = settings.requireNotNull("certificate_id")
        val certificateArn = of(this).formatArn(
            ArnComponents
                .builder()
                .service("acm")
                .resource("certificate")
                .resourceName(certificateId)
                .build()
        )
        val certificate = Certificate.fromCertificateArn(this, "CertFromArn", certificateArn)
        api.addDomainName(
            "Domain",
            DomainNameOptions
                .builder()
                .certificate(certificate)
                .domainName(domainName)
                .build()
        )
        CfnOutput(
            this,
            "DomainNameOutput",
            CfnOutputProps
                .builder()
                .exportName("$label$namespace")
                .value("https://$domainName/")
                .description("URL for the $label")
                .build()
        )

        // Create DNS records
        val hostedZoneId = settings.requireNotNull("hosted_zone_id")
        val zone = HostedZone.fromHostedZoneAttributes(
            this,
            baseDomainName,
            HostedZoneAttributes.builder()
                .zoneName(baseDomainName)
                .hostedZoneId(hostedZoneId)
                .build()
        )
        ARecord.Builder
            .create(this, "ARecord")
            .zone(zone)
            .recordName(subDomain)
            .comment("ARecord for $label")
            .target(RecordTarget.fromAlias(ApiGateway(api)))
            .ttl(Duration.seconds(60))
            .build()
    }
}