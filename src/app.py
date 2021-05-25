import json
import os
from typing import Any, Dict
import uuid
import logging

import boto3  # type: ignore

LOG = logging.getLogger()
LOG.setLevel(logging.INFO)

# Pull out the DynamoDB table name from the environment
table_name = os.environ.get("TABLE_NAME")
ddb = boto3.resource("dynamodb")
table = ddb.Table(table_name)


def text_response(message: str, code: int = 200) -> Dict[str, Any]:
    return {
        "statusCode": code,
        "headers": {"Content-Type": "text/plain"},
        "body": message,
    }


def create_short_url(event: Dict[str, Any]) -> Dict[str, Any]:
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


def read_short_url(event: Dict[str, Any]) -> Dict[str, Any]:
    # Parse redirect ID from path
    slug_id = event["pathParameters"]["proxy"]

    # Load redirect target from DynamoDB
    response = table.get_item(Key={"id": slug_id})
    LOG.debug("RESPONSE: " + json.dumps(response))

    item = response.get("Item", None)
    if item is None:
        return text_response("No redirect found for " + slug_id, 400)

    # Respond with a redirect
    return {"statusCode": 301, "headers": {"Location": item.get("target_url")}}


def lambda_handler(event: Dict[str, Any], _):
    LOG.info("EVENT: " + json.dumps(event))

    query_string_params = event.get("queryStringParameters")
    if query_string_params is not None:
        target_url = query_string_params["targetUrl"]
        if target_url is not None:
            return create_short_url(event)

    path_parameters = event.get("pathParameters")
    if path_parameters is not None and path_parameters["proxy"] is not None:
        return read_short_url(event)

    return text_response("usage: ?targetUrl=URL")
