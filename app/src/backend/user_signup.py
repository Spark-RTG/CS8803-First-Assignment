import json
import boto3
from botocore.exceptions import ClientError
from datetime import datetime

REGION = "us-east-1"
USER_POOL_ID = "us-east-1_WQiX9nY0r"
CLIENT_ID = "14fnhahoft0nm54n1417dvmn08"

cognitoclient = boto3.client("cognito-idp", region_name=REGION)
dynamodb = boto3.resource("dynamodb", region_name=REGION)
users_table = dynamodb.Table("Users")


def lambda_handler(event, context):
    password = event["body-json"]["password"]
    email = event["body-json"]["email"]
    user_tasks = []
    result = False
    message = ""
    response = {}
    returndata = {}
    userdata = {}

    try:
        response = cognitoclient.sign_up(
            ClientId=CLIENT_ID,
            Username=email,
            Password=password,
            UserAttributes=[
                {"Name": "email", "Value": email},
            ],
        )
        result = True
        message = "Signup successful"
        timestamp = str(datetime.now())
        users_table.put_item(
            Item={
                "username": email, 
                "creation_time": timestamp,
                "user_tasks": user_tasks
            }
        )

    except ClientError as e:
        if e.response["Error"]["Code"] == "UsernameExistsException":
            result = False
            message = "User already exists"
        elif e.response["Error"]["Code"] == "ParamValidationError":
            result = False
            message = "Param Validation Error"
        elif e.response["Error"]["Code"] == "InvalidParameterException":
            result = False
            message = "Please enter a valid personal email"
        elif e.response["Error"]["Code"] == "InvalidPasswordException":
            result = False
            message = (
                "Please enter a password with at least 8 characters including UPPERCASE, lowercase, special characters(e.g. !@#,.?), and number(1-9).",
            )
        else:
            result = False
            message = e.response["Error"]["Code"]

    userdata["email"] = email
    returndata["result"] = result
    returndata["message"] = message
    returndata["userdata"] = userdata

    return {"statusCode": 200, "body": returndata}

