#! /bin/bash

BUCKET_NAME=allure-poc-prjct-bucket

JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar
VM_NAME=gcedeploy

# Decrypt the credentials we added to the repo using the key we added with the Travis command line tool
openssl aes-256-cbc
    -K $encrypted_c5904ac432bd_key
    -iv $encrypted_c5904ac432bd_iv
    -in super_secret.txt.enc
    -out super_secret.txt
    -d

# If the SDK is not already cached, download it and unpack it
if [ ! -d ${HOME}/google-cloud-sdk ]; then
   curl https://sdk.cloud.google.com | bash;
fi

gsutil mb gs://${BUCKET_NAME}
gsutil cp ./target/${JAR_NAME} gs://${BUCKET_NAME}/${JAR_NAME}

gcloud compute firewall-rules create ${VM_NAME}-www --allow tcp:80 --target-tags ${VM_NAME}

gcloud compute instances create ${VM_NAME} \
  --tags ${VM_NAME} \
  --zone us-central1-a  --machine-type n1-standard-1 \
  --metadata-from-file startup-script=install.sh \
  --scopes compute-rw,storage-rw