import json
import os
import uuid
from typing import Any, Dict

import boto3  # type: ignore
from aws_lambda_powertools import Logger, Tracer
from aws_lambda_powertools.logging.correlation_paths import API_GATEWAY_HTTP

logger = Logger()
tracer = Tracer()

# Pull out the DynamoDB table name from the environment
table_name = os.environ["TABLE_NAME"]
ddb = boto3.resource("dynamodb")
table = ddb.Table(table_name)


def text_response(message: str, code: int = 200) -> Dict[str, Any]:
    """Build text response

    Parameters
    ----------
    message : str
        Message body
    code :
        Http status code

    Returns
    -------
    Dict
        API gateway response
    """
    return {
        "statusCode": code,
        "headers": {"Content-Type": "text/plain"},
        "body": message,
    }


@tracer.capture_method
def create_short_url(event: Dict[str, Any]) -> Dict[str, Any]:
    """Create a new shortened url

    Parameters
    ----------
    event : Dict
        API Gateway Event

    Returns
    -------
    Dict
        API gateway response
    """
    # Parse targetUrl
    target_url = event["queryStringParameters"]["targetUrl"]

    # Create a unique id (take first 8 chars)
    slug_id = str(uuid.uuid4())[0:8]

    # Create item in DynamoDB
    table.put_item(Item={"id": slug_id, "target_url": target_url})

    # Create the redirect URL
    url = (
        "https://"
        + event["requestContext"]["domainName"]
        + event["requestContext"]["path"]
        + slug_id
    )

    return text_response("Created URL: %s" % url)


@tracer.capture_method
def read_short_url(event: Dict[str, Any]) -> Dict[str, Any]:
    """Redirect to the destination url

    Parameters
    ----------
    event : Dict
        API Gateway Event

    Returns
    -------
    Dict
        API gateway response
    """
    # Parse redirect ID from path
    slug_id = event["pathParameters"]["proxy"]

    # Load redirect target from DynamoDB
    response = table.get_item(Key={"id": slug_id})
    logger.debug("RESPONSE: " + json.dumps(response))

    item = response.get("Item", None)
    if item is None:
        return text_response("No redirect found for " + slug_id, 400)

    # Respond with a redirect
    return {"statusCode": 301, "headers": {"Location": item.get("target_url")}}


@tracer.capture_lambda_handler
@logger.inject_lambda_context(correlation_id_path=API_GATEWAY_HTTP)
def lambda_handler(event: Dict[str, Any], _):
    """Lambda event handler

    Parameters
    ----------
    event : Dict
        API Gateway Response
    _ : Any
        Lambda Context

    Returns
    -------
    Dict
        API Gateway Response
    """
    logger.info("EVENT: " + json.dumps(event))

    query_string_params = event.get("queryStringParameters")
    if query_string_params is not None:
        target_url = query_string_params["targetUrl"]
        if target_url is not None:
            return create_short_url(event)

    path_parameters = event.get("pathParameters")
    if path_parameters is not None and path_parameters["proxy"] is not None:
        return read_short_url(event)

    return text_response("usage: ?targetUrl=URL")
