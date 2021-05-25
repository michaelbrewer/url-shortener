# url-shortener infrastructure code using CDK

Install the CDK tools

```shell script
npm install -g aws-cdk
```

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Gradle](https://gradle.org/) based project, so you can open this project with any Gradle compatible Java IDE to build and run tests.

Useful commands
----

 * `./gradlew cdk:build`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
