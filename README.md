# url-shortener
[![Url Shortener Build](https://github.com/michaelbrewer/url-shortener/actions/workflows/lambda.python.yml/badge.svg)](https://github.com/michaelbrewer/url-shortener/actions/workflows/lambda.python.yml)
[![CDK Build](https://github.com/michaelbrewer/url-shortener/actions/workflows/cdk.java.yml/badge.svg)](https://github.com/michaelbrewer/url-shortener/actions/workflows/cdk.java.yml)
[![Code Coverage](https://codecov.io/gh/michaelbrewer/url-shortener/branch/develop/graph/badge.svg?token=SMPW0VWHZ1)](https://codecov.io/gh/michaelbrewer/url-shortener)
[![Code style: black](https://img.shields.io/badge/code%20style-black-000000.svg)](https://github.com/psf/black)
[![Code style: ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg)](https://conventionalcommits.org)

URL Shortener AWS CDK example.

# High level infrastructure architecture

![URL Shortener Service Architecture](./media/architecture.png)


# Building the Python Lambda

```shell
# Install the dev dependencies
make dev
# Build and run the unit tests
make pr
```

See [IAC ReadMe](iac/README.md) for more details on how to deploy via AWS CDK

# Tools used in this project

GitHub actions:
- [CodeQL](https://securitylab.github.com/tools/codeql/) - Discover vulnerabilities across a codebase with CodeQL
- [Dependabot](https://docs.github.com/en/code-security/supply-chain-security/managing-vulnerabilities-in-your-projects-dependencies/configuring-dependabot-security-updates) - Dependabot security updates
- [Python build](https://docs.github.com/en/actions/guides/building-and-testing-python) - Building and testing Python
- [Kotlin build](https://docs.github.com/en/actions/guides/building-and-testing-java-with-gradle) - Build and testing Kotlin
- [CodeCoverage](https://about.codecov.io) - Code coverage reporting

Python AWS Lambda tools:
- [PipEnv](https://pipenv.pypa.io/en/latest/) - Python dependency management and manages virtualenv
- [Flake8](https://flake8.pycqa.org/en/latest/) - Python code style linting
- [Black](https://github.com/psf/black) - The uncompromising Python code formatter
- [iSort](https://pycqa.github.io/isort/) - Python utility to sort imports alphabetically, and automatically separated into sections and by type
- [PyTest](https://docs.pytest.org/en/6.2.x/index.html) - Testing framework for Python
- [Moto](https://github.com/spulec/moto) - A library that allows you to easily mock out tests based on AWS infrastructure.
- [Lambda Powertools](https://github.com/awslabs/aws-lambda-powertools-python) - A suite of utilities for AWS Lambda Functions that makes tracing with AWS X-Ray, structured logging and creating custom metrics asynchronously easier
- [PreCommit](https://pre-commit.com) - A framework for managing and maintaining multi-language pre-commit hooks.
- [Radon](https://github.com/rubik/radon/) & [Xenon](https://github.com/rubik/xenon) - Various code metrics for Python code

Infrastructure code tools:
- [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin) - plugin provides a task to determine which dependencies have updates
- [AWS CDK](https://aws.amazon.com/cdk/) - Define cloud infrastructure using familiar programming languages
- [Junit 5](https://junit.org/junit5/docs/current/user-guide/) - JUnit 5
- [JaCoCo](https://www.eclemma.org/jacoco/) - JaCoCo Java Code Coverage Library
- [KtLint](https://github.com/pinterest/ktlint) - An anti-bikeshedding Kotlin linter with built-in formatter
- [Detekt](https://github.com/detekt/detekt) - A static code analysis tool for the Kotlin programming language
- [CFNLint](https://github.com/aws-cloudformation/cfn-lint) - Validate AWS CloudFormation yaml/json templates against the AWS CloudFormation Resource Specification
- [CFN-Diagram](https://github.com/mhlabs/cfn-diagram) - CLI tool to visualise CloudFormation/SAM/CDK templates as diagrams
- [CDK Watchful](https://github.com/cdklabs/cdk-watchful) - Construct library that makes it easy to monitor CDK apps
