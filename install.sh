#! /bin/bash

## This startup script runs ON the compute vm inside GCE

BUCKET_NAME=allure-poc-prjct-bucket
JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar

GCE_DEPLOY_DIR=opt/gcedeploy
ALLURE_RESULTS=allure-results

VM_NAME=gcedeploy

sudo su -

echo "========================================================"
echo "==> @ CAMERA! ACTION! %) "
printf " * USER: %s\n" $USER
printf " * CURRENT DIRECTORY: %s\n" $PWD
echo "--------------------------------------------------------"
echo " ENVIRONMENT :"
env
echo "--------------------------------------------------------"
echo " installing all needed dependencies......"

sudo apt-get install dirmngr
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
apt-get update

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get install oracle-java8-installer -y

echo "Google SDK install...."
gcloud version || true
if [ ! -d "./google-cloud-sdk/bin" ]; then rm -rf ./google-cloud-sdk; export CLOUDSDK_CORE_DISABLE_PROMPTS=1; curl https://sdk.cloud.google.com | bash; fi
# Add gcloud to $PATH
source ./google-cloud-sdk/completion.bash.inc
source ./google-cloud-sdk/path.bash.inc
gcloud version

echo $GCE_MASTER_SVC_KEY_IN_BASE64 | base64 --decode --output ./gcloud-api-key.json
cat ./gcloud-api-key.json

gcloud auth activate-service-account \
    gce-master-acc@test-gce-prjct.iam.gserviceaccount.com \
    --key-file ./gcloud-api-key.json

gcloud config set project test-gce-prjct
echo "Google SDK info:"
gcloud info

echo "Google SDK downloaded."
echo "--------------------------------------------------------"
echo " deploying test artifact to env and launching it ...."
mkdir -p /${ALLURE_RESULTS}
rm -rf /${ALLURE_RESULTS}/*

mkdir /${GCE_DEPLOY_DIR}
gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /${GCE_DEPLOY_DIR}/${JAR_NAME}
java -jar /${GCE_DEPLOY_DIR}/${JAR_NAME}


echo " to get trend data - needed last report history dir with all data in it"
mkdir -p /${ALLURE_RESULTS}/history
rm -rf /${ALLURE_RESULTS}/history/*
gsutil cp gs://${BUCKET_NAME}/allure-report/history/* /${ALLURE_RESULTS}/history

gsutil -m cp -r gs://${BUCKET_NAME}/.allure /
chmod +x .allure/allure-2.6.0/bin/allure
.allure/allure-2.6.0/bin/allure -v generate --clean

echo "--------------------------------------------------------"
echo " saving test results to GC bucket ...."

gsutil -m rm -rf gs://${BUCKET_NAME}/allure-report/*
gsutil -m cp -r /allure-report gs://${BUCKET_NAME}/

#gsutil -m cp -r /allure-results gs://${BUCKET_NAME}
gsutil -m cp -r /allure-results/* gs://${BUCKET_NAME}/allure-results_$(date +%Y.%m.%d_%H-%M)/


echo "--------------------------------------------------------"
echo "  triggering TravisCI build to finalize run ...."

curl -s -X POST -H "Content-Type: application/json" \
                -H "Accept: application/json" \
                -H "Travis-API-Version: 3" \
                -H "Authorization: token 8ioPeQGS8fxsFd7mqW7pWQ" \
                https://api.travis-ci.org/repo/uvidit%2FpocProjectDocPages/requests

echo "--------------------------------------------------------"
echo " @ Saving gce log..."
gcloud compute --project=test-gce-prjct instances get-serial-port-output gcedeploy --zone=us-central1-a > ./gce_$(date +%Y.%m.%d_%H-%M).log
gsutil -m cp ./gce_*.log gs://${BUCKET_NAME}
echo "--------------------------------------------------------"


echo "  stopping GCE instance ...."
sudo shutdown now -h now

echo "  CUT! Thank you all!!! Buy! %)"
echo "========================================================"
exit