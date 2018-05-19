#! /bin/bash
echo "Launched START.sh script...."
BUCKET_NAME=allure-poc-prjct-bucket

JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar
VM_NAME=gcedeploy
GCLOUD_DIR=google-cloud-sdk

gcloud info --format='value(installation.sdk_root)' | xargs rm -rf
rm -rf ~/${GCLOUD_DIR}
rm -rf ~/.config/gcloud

curl https://sdk.cloud.google.com | bash
chmod +x ~/${GCLOUD_DIR}/install.sh
CLOUDSDK_CORE_DISABLE_PROMPTS=1 ~/${GCLOUD_DIR}/install.sh

source /Users/ozinchenko/google-cloud-sdk/completion.bash.inc
source /Users/ozinchenko/google-cloud-sdk/path.bash.inc

gcloud auth activate-service-account \
    gc-svc-acc-allure-reporter-prj@test-gce-prjct.iam.gserviceaccount.com \
    --key-file ./test-gce-prjct-dbc00919a7ea.json
gcloud config set project test-gce-prjct
echo "Google SDK info:"
gcloud info

echo "Google SDK downloaded."
echo "Creating Bucket on Google Storage....."

gsutil mb gs://${BUCKET_NAME}
gsutil cp ./target/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}
gsutil -m cp -r ./.allure gs://${BUCKET_NAME}/

gcloud compute firewall-rules delete --quiet ${VM_NAME}-www
gcloud compute instances delete --quiet --zone=us-central1-a ${VM_NAME}

echo "Creating firewall in cloud...."
gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:80 --target-tags ${VM_NAME}
echo "Starting VM in cloud...."
gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --zone us-central1-a  --machine-type n1-standard-1 \
  --metadata-from-file startup-script=install.sh \
  --scopes compute-rw,storage-rw
echo "done"