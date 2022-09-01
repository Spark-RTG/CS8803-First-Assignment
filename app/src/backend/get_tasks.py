import json
import boto3
from boto3.dynamodb.conditions import Attr
from decimal import Decimal

class DecimalEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Decimal):
            return str(obj)
        return json.JSONEncoder.default(self, obj)

REGION="us-east-1"
dynamodb = boto3.resource('dynamodb',region_name=REGION)
tasks_table = dynamodb.Table('Tasks')

def customSort(k):
    return k['due_date']
    
def lambda_handler(event, context):
    email=event['body-json']['email']
    update_attributes = {}
    
    filter_expression = Attr("username").eq(email)
    response = tasks_table.scan(FilterExpression=filter_expression)
    if "Items" not in response.keys():
        return {"statusCode": 204, "body": json.dumps("No available tasks")}
    tasks = response["Items"]
    tasks.sort(key=customSort)
    for task in tasks:
        task["email"] = task["username"]
    
    return {"statusCode": 200, "body": tasks }
