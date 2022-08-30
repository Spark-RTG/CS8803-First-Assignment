import json
import boto3    
from botocore.exceptions import ClientError
 
REGION="us-east-1"
USER_POOL_ID="us-east-1_WQiX9nY0r"
CLIENT_ID="14fnhahoft0nm54n1417dvmn08"

cognitoclient = boto3.client('cognito-idp', region_name=REGION)
                            
def lambda_handler(event, context):
    email=event['body-json']['email']
    password=event['body-json']['password']
    result=False
    message=""
    response={}
    returndata={} 
    userdata={}
    
    try:
        response = cognitoclient.admin_initiate_auth(
            UserPoolId=USER_POOL_ID,
            ClientId=CLIENT_ID,
            AuthFlow='ADMIN_USER_PASSWORD_AUTH',
            AuthParameters={
                'USERNAME': email,
                'PASSWORD': password
            }
        )
    
    except ClientError as e:
        message="Error in logging in"
        if e.response['Error']['Code'] == 'UserNotFoundException':
            result=False
            message=="Can't Find user by Email"
        elif e.response['Error']['Code'] == 'NotAuthorizedException':
            result=False
            message="Incorrect username or password"
        elif e.response['Error']['Code'] == 'UserNotConfirmedException':
            result=False
            message="User is not confirmed"
        else:
            result=False
            message=e.response['Error']['Code']

    if 'ResponseMetadata' in response:
        if response['ResponseMetadata']['HTTPStatusCode']==200:        
            result=True
            message="Login successful"
            response = cognitoclient.admin_get_user(
                UserPoolId=USER_POOL_ID,
                Username=email
            )
            for item in response['UserAttributes']:
                if item['Name']=='name':
                    userdata['name']=item['Value']
                elif item['Name']=='email':
                    userdata['email']=item['Value']
                elif item['Name']=='email_verified':
                    userdata['email_verified']=item['Value']
        else:
            return False
            message="Something went wrong"
            
    userdata['email']=email
    returndata['result']=result
    returndata['message']=message
    returndata['userdata']=userdata
    
    return {
        "statusCode": 200,
        "body": returndata
    }