#!/bin/bash

echo ""
echo "CREATING DEVICES TABLE..."
echo ""
awslocal dynamodb create-table                                \
  --table-name Devices                                        \
  --attribute-definitions AttributeName=id,AttributeType=S    \
  --key-schema AttributeName=id,KeyType=HASH                  \
  --billing-mode PAY_PER_REQUEST

echo ""
echo "WRITING DEVICE ITEMS..."
echo ""
awslocal dynamodb batch-write-item                            \
    --request-items file:///var/lib/localstack/devices.json

echo ""
echo "CREATING LAMBDA FUNCTION..."
echo ""
awslocal lambda create-function                 \
  --function-name 'city-tasks-events'           \
  --runtime 'java17'                            \
  --architectures 'arm64'                       \
  --role 'arn:aws:iam::000000000000:role/test'  \
  --handler 'com.hiperium.city.tasks.events.ApplicationHandler::handleRequest' \
  --zip-file fileb:///var/lib/localstack/city-tasks-events-1.6.0.jar

echo ""
echo "CREATING EVENTBRIDGE RULE..."
echo ""
awslocal events put-rule        \
  --name city-tasks-event-rule  \
  --event-pattern "{\"source\":[\"com.hiperium.city.tasks\"],\"detail-type\":[\"TaskExecution\"]}"

echo ""
echo "GETTING LAMBDA ARN..."
echo ""
lambda_arn=$(awslocal lambda get-function   \
  --function-name city-tasks-events         \
  --query 'Configuration.FunctionArn'       \
  --output text)
echo "ARN: $lambda_arn"

echo ""
echo "CREATING EVENTBRIDGE TARGET..."
echo ""
awslocal events put-targets       \
  --rule city-tasks-event-rule    \
  --targets "Id"="1","Arn"="$lambda_arn"

echo ""
echo "DONE!"
