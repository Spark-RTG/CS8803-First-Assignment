import json
import boto3  
import time
import datetime

REGION="us-east-1"
dynamodb = boto3.resource('dynamodb',region_name=REGION)
users_table = dynamodb.Table('Users')
tasks_table = dynamodb.Table('Tasks')

def lambda_handler(event, context):
    add_tasks=event['body-json']["tasks"]
    for add_task in add_tasks:
        email=add_task['email']
        task_id=add_task['task_id']
        subject=add_task['subject']
        description=add_task['description']
        hours=add_task['hours']
        minutes=add_task['minutes']
        due_date=add_task['due_date']
        importance=add_task['importance']
        stared=add_task['stared']
        update_attributes = {}
        try:
            user_info = users_table.get_item(Key={"username": email})["Item"]
        except Exception as e:
            return {"statusCode": 500, "body": "Users not found"}
        user_tasks = user_info["user_tasks"]
        user_tasks.append(task_id)
        tasks_table.put_item(
            Item={                        
                "username": email,
                "task_id": task_id,
                "subject": subject,
                "description": description,
                "hours": hours,
                "minutes": minutes,
                "due_date": due_date,
                "importance": importance,
                "stared": stared
            }
        )
        update_attributes["user_tasks"] = {
            "Value": user_tasks, 
        }
        users_table.update_item(
            Key={"username": email}, AttributeUpdates=update_attributes,
        )
        
    return {
        "statusCode": 200,
        "body": "Add successful"
    }
