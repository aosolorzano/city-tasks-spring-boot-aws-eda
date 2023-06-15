#!/bin/bash

cd "$WORKING_DIR" || {
  echo "Error moving to the application's root directory."
  exit 1
}

### READ CSR DOMAIN NAME AND CSR SERVER FQDN
echo ""
read -r -p 'Enter the <Domain Name> used in your <CSR> certificate: ' server_domain_name
if [ -z "$server_domain_name" ]; then
  echo "Error: The <Domain Name> is required."
  exit 1
fi

echo ""
echo "DELETING COPILOT APP FROM AWS..."
copilot app delete --yes

### DELETE ACM CERTIFICATE
acm_arn=$(aws acm list-certificates     \
  --includes  keyTypes=EC_prime256v1    \
  --profile "$AWS_WORKLOADS_PROFILE"    \
  --output text                         \
  --query "CertificateSummaryList[?contains(DomainName, '$server_domain_name')].[CertificateArn]")
if [ "$acm_arn" ]; then
  echo ""
  echo "DELETING ACM CERTIFICATE FROM AWS..."
  aws acm delete-certificate            \
    --certificate-arn "$acm_arn"
else
  echo ""
  echo "WARNING: Not ACM Certificate was found for domain: '$server_domain_name'."
  echo "You must delete the certificate manually."
fi

### REVERT CONFIGURATION FILES
sh "$WORKING_DIR"/utils/scripts/helper/1_revert-automated-scripts.sh
echo ""
echo "DONE!"
