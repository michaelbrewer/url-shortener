import os

from botocore.stub import Stubber  # type: ignore

from tests.utils import load_event


def test_usage(lambda_context):
    mock_event = load_event("event-usage.json")
    from src import app

    result = app.lambda_handler(mock_event, lambda_context)

    assert result["body"] == "usage: ?targetUrl=URL"


def test_create_short_url(lambda_context, mocker):
    mock_event = load_event("event-create.json")

    from src import app

    slug_id = "foo"
    mock_uuid = mocker.patch("uuid.uuid4")
    mock_uuid.return_value = slug_id
    stubber = Stubber(app.table.meta.client)
    stubber.deactivate()
    stubber.add_response(
        "put_item",
        {},
        {
            "Item": {"id": slug_id, "target_url": "https://www.fiserv.com"},
            "TableName": os.environ["TABLE_NAME"],
        },
    )
    stubber.activate()

    result = app.lambda_handler(mock_event, lambda_context)

    assert "Created URL" in result["body"]


def test_read_short_url(lambda_context):
    mock_event = load_event("event-read.json")

    from src import app

    saved_url = "https://www.fiserv.com"
    stubber = Stubber(app.table.meta.client)
    stubber.deactivate()
    stubber.add_response(
        "get_item",
        {
            "Item": {
                "id": {"S": "18cc6cd6"},
                "target_url": {"S": saved_url},
            },
        },
        {"Key": {"id": "18cc6cd6"}, "TableName": "table"},
    )
    stubber.activate()

    result = app.lambda_handler(mock_event, lambda_context)

    assert result["headers"]["Location"] == saved_url


def test_read_short_url_none(lambda_context):
    mock_event = load_event("event-read.json")

    from src import app

    stubber = Stubber(app.table.meta.client)
    stubber.deactivate()
    stubber.add_response(
        "get_item",
        {},
        {"Key": {"id": "18cc6cd6"}, "TableName": "table"},
    )
    stubber.activate()

    result = app.lambda_handler(mock_event, lambda_context)

    assert result["body"] == "No redirect found for 18cc6cd6"
