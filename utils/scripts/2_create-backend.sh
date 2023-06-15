#!/bin/bash

cd "$WORKING_DIR"/src/city-tasks-api || {
  echo "Error moving to the Tasks Service directory."
  exit 1
}

### VERIFYING CA AND CSR CERTIFICATES FILES
CA_CERTS_DIR="$WORKING_DIR"/utils/certs
TASKS_CERTS_DIR=utils/certs/"$AWS_WORKLOADS_ENV"
if [ ! -f "$CA_CERTS_DIR"/ca-cert.pem ] || [ ! -f "$TASKS_CERTS_DIR"/server-key.pem ] || [ ! -f "$TASKS_CERTS_DIR"/server-cert-"$AWS_WORKLOADS_ENV".pem ]; then
  echo ""
  echo "Error: Not TLS certificates was found for the '$AWS_WORKLOADS_ENV' environment."
  echo "You can create <TLS Certificates> using the 'Helper Menu', option 3."
  exit 1
fi

### REVERTING CONFIGURATION FILES
sh "$WORKING_DIR"/utils/scripts/helper/1_revert-automated-scripts.sh

### READING SERVER DOMAIN NAME AND SERVER FQDN
echo ""
read -r -p 'Enter the <Domain Name> used in your <CSR> certificate: ' server_domain_name
if [ -z "$server_domain_name" ]; then
  echo "Error: The <Domain Name> is required."
  exit 1
fi
server_fqdn="$AWS_WORKLOADS_ENV.$server_domain_name"

### UPDATE API MANIFEST FILE WITH SERVER FQDN
sed -i'.bak' -e "s/server_domain_name/$server_domain_name/g; s/server_fqdn/$server_fqdn/g;"  \
      "$WORKING_DIR"/src/city-tasks-api/copilot/api/manifest.yml
rm -f "$WORKING_DIR"/src/city-tasks-api/copilot/api/manifest.yml.bak

### ASKING TO STORE ALB ACCESS-LOGS
sh "$WORKING_DIR"/utils/scripts/helper/create-alb-logs-s3-bucket.sh

### ASKING TO PRUNE DOCKER SYSTEM
read -r -p "Do you want to prune your docker system? [y/N] " response
case $response in
  [yY])
    sh "$WORKING_DIR"/utils/scripts/helper/2_docker-system-prune.sh
    ;;
esac

echo ""
echo "GETTING INFORMATION FROM AWS. PLEASE WAIT..."

### GETTING CSR CERTIFICATE ARN
acm_arn=$(aws acm list-certificates       \
  --includes  keyTypes=EC_prime256v1      \
  --profile "$AWS_WORKLOADS_PROFILE"      \
  --output text                           \
  --query "CertificateSummaryList[?contains(DomainName, '$server_domain_name')].[CertificateArn]")
if [ -z "$acm_arn" ]; then
  echo ""
  echo "Error: Not ACM Certificate was found for domain: '$server_domain_name'."
  echo "You can import your <CSR> certificates using the 'Helper Menu', option 4."
  sh "$WORKING_DIR"/utils/scripts/helper/1_revert-automated-scripts.sh
  exit 1
fi

### GETTING COGNITO USER POOL ID
cognito_user_pool_id=$(aws cognito-idp list-user-pools --max-results 1 --output text  \
  --query "UserPools[?contains(Name, 'CityUserPool')].[Id]"                           \
  --profile "$AWS_IDP_PROFILE")
if [ -z "$cognito_user_pool_id" ]; then
  echo ""
  echo "Error: Not Cognito User Pool ID was found with name: 'CityUserPool'."
  sh "$WORKING_DIR"/utils/scripts/helper/1_revert-automated-scripts.sh
  exit 0
fi

### UPDATING API MANIFEST FILE WITH COGNITO USER POOL ID
idp_aws_region=$(aws configure get region --profile "$AWS_IDP_PROFILE")
sed -i'.bak' -e "s/idp_aws_region/$idp_aws_region/g; s/cognito_user_pool_id/$cognito_user_pool_id/g"  \
      "$WORKING_DIR"/src/city-tasks-api/copilot/api/manifest.yml
rm -f "$WORKING_DIR"/src/city-tasks-api/copilot/api/manifest.yml.bak

### UPDATING ENV MANIFEST FILE WITH ACM ARN
workloads_aws_region=$(aws configure get region --profile "$AWS_WORKLOADS_PROFILE")
workloads_aws_account_id=$(aws configure get sso_account_id --profile "$AWS_WORKLOADS_PROFILE")
acm_certificate_number=$(echo "$acm_arn" | cut -d'/' -f2)
sed -i'.bak' -e "s/workloads_aws_region/$workloads_aws_region/g; s/workloads_aws_account_id/$workloads_aws_account_id/g; s/acm_certificate_number/$acm_certificate_number/g" \
      "$WORKING_DIR"/src/city-tasks-api/copilot/environments/"$AWS_WORKLOADS_ENV"/manifest.yml
rm -f "$WORKING_DIR"/src/city-tasks-api/copilot/environments/"$AWS_WORKLOADS_ENV"/manifest.yml.bak

### UPDATING ENVOY CONFIGURATION FILE FOR AWS
cat "$WORKING_DIR"/utils/templates/docker/envoy/envoy-aws.yaml > "$WORKING_DIR"/src/city-tasks-api/utils/docker/envoy/envoy.yaml
echo ""
echo "DONE!"

echo ""
echo "INITIALIZING COPILOT STACK ON AWS..."
copilot init                              \
  --app city-tasks                        \
  --name api                              \
  --type 'Load Balanced Web Service'      \
  --dockerfile './Dockerfile'             \
  --port 8080                             \
  --tag '1.6.0'
echo ""
echo "DONE!"

echo ""
echo "INITIALIZING ENVIRONMENT ON AWS..."
copilot env init                          \
  --app city-tasks                        \
  --name "$AWS_WORKLOADS_ENV"             \
  --profile "$AWS_WORKLOADS_PROFILE"      \
  --default-config
echo ""
echo "DONE!"

echo ""
echo "DEPLOYING ENVIRONMENT NETWORKING ON AWS..."
copilot env deploy                        \
  --app city-tasks                        \
  --name "$AWS_WORKLOADS_ENV"
echo ""
echo "DONE!"

echo ""
echo "DEPLOYING CONTAINER APPLICATION ON AWS..."
copilot deploy                            \
  --app city-tasks                        \
  --name api                              \
  --env "$AWS_WORKLOADS_ENV"              \
  --tag '1.6.0'                           \
  --no-rollback                           \
  --resource-tags project=Hiperium,copilot-application-type=api,copilot-application-version=1.6.0
echo ""
echo "DONE!"

echo ""
echo "GETTING ALB DOMAIN NAME..."
alb_domain_name=$(aws cloudformation describe-stacks --stack-name city-tasks-"$AWS_WORKLOADS_ENV" \
  --query "Stacks[0].Outputs[?OutputKey=='PublicLoadBalancerDNSName'].OutputValue" \
  --output text \
  --profile "$AWS_WORKLOADS_PROFILE")
echo "ALB Domain Name: $alb_domain_name"

echo ""
echo "IMPORTANT!!: Create a new CNAME record in your Route53 Hosted Zone for your ALB Domain Name."
echo ""
echo "DONE!"
