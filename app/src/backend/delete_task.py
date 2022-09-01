import boto3
import json

REGION = "us-east-1"
dynamodb = boto3.resource("dynamodb", region_name=REGION)
users_table = dynamodb.Table("Users")
tasks_table = dynamodb.Table("Tasks")

def lambda_handler(event, context):
    delete_tasks = event["body-json"]["tasks"]
    update_attributes={}
    
    for delete_task in delete_tasks:
        email = delete_task["email"]
        user_info = users_table.get_item(Key={"username": email})["Item"]
        user_tasks = user_info["user_tasks"]
        user_tasks.remove(delete_task["task_id"])
        
        update_attributes["user_tasks"] = {
            "Value": user_tasks, 
        }
        try:
            users_table.update_item(
                Key={"username": email}, AttributeUpdates=update_attributes,
            )
            tasks_table.delete_item(
                Key={
                    "username": email,
                    "task_id": delete_task["task_id"]
                }
            )
        except Exception as e:
            return {"statusCode": 500, "body": str(e)}
        
    return {"statusCode": 200, "body": "Delete successful"}
    