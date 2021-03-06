#! /bin/bash
echo "Launched START.sh script...."
BUCKET_NAME=allure-poc-prjct-bucket

JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar
VM_NAME=gcedeploy

echo "Google SDK install...."
gcloud version || true
if [ ! -d "$HOME/google-cloud-sdk/bin" ]; then rm -rf $HOME/google-cloud-sdk; export CLOUDSDK_CORE_DISABLE_PROMPTS=1; curl https://sdk.cloud.google.com | bash; fi
# Add gcloud to $PATH
source $HOME/google-cloud-sdk/completion.bash.inc
source $HOME/google-cloud-sdk/path.bash.inc
gcloud version

# travis has an old version of base64 util in some Ubuntu distr, so don't change line below!!!! (it possible doesn't work on MAC)
#echo $GCE_MASTER_SVC_KEY_IN_BASE64 | base64 -di > ./gcloud-api-key.json
echo $GCE_MASTER_SVC_KEY_IN_BASE64 | base64 --decode > ./gcloud-api-key.json
#   or echo ${$GCE_MASTER_SVC_KEY_IN_BASE64//\\n/} | base64 --decode --ignore-garbage > ./gcloud-api-key.json
#   or echo "Base64 encode this text." | openssl enc -base64

# NB: email from service account key json file
gcloud auth activate-service-account \
    gce-master-acc@test-gce-prjct.iam.gserviceaccount.com \
    --key-file ./gcloud-api-key.json
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
  --metadata GCE_MASTER_SVC_KEY_IN_BASE64=$GCE_MASTER_SVC_KEY_IN_BASE64 \
  --service-account gce-master-acc@test-gce-prjct.iam.gserviceaccount.com \
  --scopes compute-rw,storage-rw
echo "done"