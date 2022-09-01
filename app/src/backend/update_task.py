import boto3
import json

REGION = "us-east-1"
dynamodb = boto3.resource("dynamodb", region_name=REGION)
tasks_table = dynamodb.Table("Tasks")

def lambda_handler(event, context):
    update_tasks = event["body-json"]["tasks"]
    for update_task in update_tasks:
        email = update_task["email"]
        task_id = update_task["task_id"]
        update_attributes = {}
        try:
            user_info = tasks_table.get_item(Key={"username": email, "task_id": task_id})["Item"]
        except Exception as e:
            return {"statusCode": 500, "body": str(e)}
        if "subject" in update_task:
            update_attributes["subject"] = {
                "Value": update_task["subject"],
            }
        if "description" in update_task:
            update_attributes["description"] = {
                "Value": update_task["description"],
            }
        if "hours" in update_task:
            update_attributes["hours"] = {
                "Value": update_task["hours"],
            }
        if "description" in update_task:
            update_attributes["description"] = {
                "Value": update_task["description"],
            }
        if "minutes" in update_task:
            update_attributes["minutes"] = {
                "Value": update_task["minutes"],
            }
        if "due_date" in update_task:
            update_attributes["due_date"] = {
                "Value": update_task["due_date"],
            }
        if "importance" in update_task:
            update_attributes["importance"] = {
                "Value": update_task["importance"],
            }
        if "stared" in update_task:
            update_attributes["stared"] = {
                "Value": update_task["stared"],
            }
        try:
            tasks_table.update_item(
                Key={"username": email, "task_id": task_id}, AttributeUpdates=update_attributes,
            )
        except Exception as e:
            return {"statusCode": 500, "body": str(e)}

    return {"statusCode": 200, "body": "Update successful"}
