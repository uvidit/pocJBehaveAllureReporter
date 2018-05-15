#! /bin/bash
echo "Launched START.sh script...."
BUCKET_NAME=allure-poc-prjct-bucket

JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar
VM_NAME=gcedeploy

# Decrypt the credentials we added to the repo using the key we added with the Travis command line tool
gsutil mb gs://${BUCKET_NAME}
gsutil cp ./target/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

echo "Creating firewall and starting VM in cloud...."
#gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:80 --target-tags ${VM_NAME}
#
#gcloud compute instances create ${VM_NAME} \
#  --tags ${VM_NAME} \
#  --zone us-central1-a  --machine-type n1-standard-1 \
#  --metadata-from-file startup-script=install.sh \
#  --scopes compute-rw,storage-rw
echo "done"