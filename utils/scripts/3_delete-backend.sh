#!/bin/bash

cd "$WORKING_DIR"/src/city-tasks-api || {
  echo "Error moving to the Tasks Service directory."
  exit 1
}

### READ CSR DOMAIN NAME AND CSR SERVER FQDN
echo ""
read -r -p 'Enter the <Domain Name> used in your <CSR> certificate: ' server_domain_name
if [ -z "$server_domain_name" ]; then
  echo "Error: The <Domain Name> is required."
  exit 1
fi
server_fqdn="$AWS_WORKLOADS_ENV.$server_domain_name"

### ASKING TO DELETE ALB DOMAIN NAME ON ROUTE53
read -r -p "Do you want to <delete> the ALB domain from Route53? [Y/n] " delete_alb_domain
if [ -z "$delete_alb_domain" ]; then
  read -r -p 'Enter the <AWS Profile> to deletion the <Record Set>: [default] ' aws_profile
  if [ -z "$aws_workloads_profile" ]; then
    AWS_ROUTE53_PROFILE='default'
  else
    AWS_ROUTE53_PROFILE=$aws_profile
  fi
fi

echo ""
echo "GETTING INFORMATION FROM AWS. PLEASE WAIT..."

### GETTING ALB DOMAIN NAME
alb_domain_name=$(aws cloudformation describe-stacks --stack-name city-tasks-"$AWS_WORKLOADS_ENV" \
  --query "Stacks[0].Outputs[?OutputKey=='PublicLoadBalancerDNSName'].OutputValue" \
  --output text \
  --profile "$AWS_WORKLOADS_PROFILE")

echo ""
echo "DELETING COPILOT APP FROM AWS..."
copilot app delete --yes
echo ""
echo "DONE!"

### DELETE ACM CERTIFICATE
echo ""
echo "DELETING ACM CERTIFICATE FROM AWS..."
acm_arn=$(aws acm list-certificates   \
  --includes keyTypes=EC_prime256v1   \
  --profile "$AWS_WORKLOADS_PROFILE"  \
  --output text \
  --query "CertificateSummaryList[?contains(DomainName, '$server_domain_name')].[CertificateArn]")
if [ -z "$acm_arn" ]; then
  echo ""
  echo "WARNING: Not ACM Certificate found to delete. You must delete it manually."
else
  aws acm delete-certificate      \
    --certificate-arn "$acm_arn"  \
    --profile "$AWS_WORKLOADS_PROFILE"
  echo "DONE!"
fi

### DELETE RECORD SET FROM ROUTE53
echo ""
echo "DELETING RECORD SET FROM ROUTE53..."
hosted_zone_id=$(aws route53 list-hosted-zones-by-name \
  --dns-name "$server_domain_name" \
  --profile "$AWS_ROUTE53_PROFILE" \
  --output text \
  --query "HostedZones[?contains(Name, '$server_domain_name')].[Id]")
if [ -z "$hosted_zone_id" ]; then
  echo ""
  echo "WARNING: Not Hosted Zone found on Route53. You must delete it manually."
else
  cat "$WORKING_DIR"/utils/templates/route53/tasks-api-delete-alb-record-set.json > "$WORKING_DIR"/src/city-tasks-api/utils/aws/route53/tasks-api-delete-alb-record-set.json
  sed -i'.bak' -e "s/server-name-fqdn/$server_fqdn/g; s/alb-domain-name/$alb_domain_name/g" \
        "$WORKING_DIR"/src/city-tasks-api/utils/aws/route53/tasks-api-delete-alb-record-set.json
  rm -f "$WORKING_DIR"/src/city-tasks-api/utils/aws/route53/tasks-api-delete-alb-record-set.json.bak
  hosted_zone_id=$(echo "$hosted_zone_id" | cut -d'/' -f3)
  aws route53 change-resource-record-sets \
    --hosted-zone-id "$hosted_zone_id" \
    --change-batch file://"$WORKING_DIR"/src/city-tasks-api/utils/aws/route53/tasks-api-delete-alb-record-set.json \
    --profile "$AWS_ROUTE53_PROFILE"
  echo "DONE!"
fi

### REVERT CONFIGURATION FILES
sh "$WORKING_DIR"/utils/scripts/helper/1_revert-automated-scripts.sh
