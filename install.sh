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

WWW=ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAidGVzdC1nY2UtcHJqY3QiLAogICJwcml2YXRlX2tleV9pZCI6ICJkYmMwMDkxOWE3ZWFlZmE4YjIyM2M5ZDdiNjdkMzkyZWRjM2E5Yzc1IiwKICAicHJpdmF0ZV9rZXkiOiAiLS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tXG5NSUlFdlFJQkFEQU5CZ2txaGtpRzl3MEJBUUVGQUFTQ0JLY3dnZ1NqQWdFQUFvSUJBUUNoU2RpRm5JUTNVNmZOXG5xR3R0b3c3aGhTSWc0c3FpZS9wUWprTG9wVGU0ZXY3YTlmS0FRZVlKRi91OHY0dlZidmhINU5ka2w2eGk2VTg5XG5SSG5ydTZKWk9qaDNaVHFKV2ROUEJaMWUrbU9tWjdudU5aTzVIbE9LRXcyZU16MFVJQ0pJcll1MWhsei9zNktrXG5EVTZBZHAxWkM2QU01RXZJM2pSYVg1Z3lCeld6UklGY2grK3NVV2RFcmV4WDdFdGRsYStPc0MrRmpobFhXRGVXXG43bEsxQk4yN2lRYS9ScXhpWmUrQndzSGtVeGhHZ1F2NituNmJLS09LSXhIbThuUUVkT0JnZnh0Q0ZNcEdrUEg0XG4reE0yeFE3OGZxVUtLUEtmZUxoZFBlcTA0dU5WcGs2TDZHRzVmTEJDdlNUTjR3VHltY2R4SStaN0ZlYysvL1BNXG54djRoVk1TdkFnTUJBQUVDZ2dFQUE1ZlZqd1J4blpucmZ4VmxiMGZXcVFlQ1B5Rk9TbGhndHpLMEl1S3JYeHkrXG5XTHBwOEptSE9tU2lZdzRSVzJ2aGRCaVJBOWl0S25id0lQbEpqZ3JjRHdqUkFUZnNHZXM2WGVST0lUT0t6TVk2XG55aWUvWnJ4YU9CVThsRzJueXE3OGxJbnBOdDRMYkZiZ0pLOTNORDF3TysrYVhTOG8wUXpDTmRFelhzbnlWRkFGXG5VZWtCYjFDVTc5N2J0bzFYd1dwTXdxbnZJL2NEaFdUc05yeWEvVUl3WFE4TDg2bzJLTFBqS0QzM1pMR0lJMjlqXG5vWVl4WFRvRk9wcDRlRUtDajRFWFc1RVFhUXVFbzIzTWtXdXY2UWY5eTNHeVBwbytWYURMM09HYkJhV0VaRUpyXG4xTDlObUZ5cHY0aXRJNnR2b1plMy8xS2Ztb3EvRVVmbFFaZGZGWTRqUVFLQmdRRFVCZ043b296NGJLUkdxOVZiXG5Ddi9ad3l5aFJpZkpsRDFHOCs4QVUxZ1dWVHdXMFE1bld5dmhwKzRBRlRmYzhaa1owVHZtNThKbVlJdEczM1cwXG5QdXJXKzA3Y05IYkZtZ1JNM081UWNpNkZvRE14aDMwejNCa3Rwb2xnMVcwWVBXbmt6TFFMb1VhNkJDQVMvdXh3XG5iTE5DZ0VTdUZJSGJscnJpVmdYNjdCSXZXUUtCZ1FEQ3ZlbVF2UGJqTVNYR1k2blNZYWV3ZEJKUm1Nd1pyMk9aXG51Zis0ZEdmQ256Y0Vmd1luRDBqT3RIWWkxVUdwUnZZakdVc1B2S0FGcFJZendZZDUrNXNSc2tQamdWc1h5NjhEXG5PdDJKSVMra0hJOHdJR1MrRlpYOHZpbXZMVGUzSlJHZEY3cVdyZWh3OHlSUTN2NXE5anZPd0xnaHdLU1lvTHlPXG52NVljUzMxYlJ3S0JnRGJwRzh2UVRHd21UdUZTclYzMzJrMCtnL2Q4b045TytoTy9KejNSVi9Yb3V6ZzVpbDV3XG5PVXdtM3JlOHh3djhzaWNDamwzOTFkWUgyT3BSQXozY01admQyQTJsdE1EMlkzaTRteXZRZG9YNitHY3liTldhXG5naGJWazZ5MWVaeVdneTlUYUttK2FmL1JBN0dIdmhwYnZJMVRoUFlFcjZEOFI0aEl1U0hFTSsxNUFvR0FOT3FGXG4zTjU3OTZBVE02VkJML2w1SjJsaTBPbEVESGFzQ2NtUXd2NWVjM1B2N3pqVTR5ZzBFSGJZNHY0dnFWK3U0anF0XG5Pc2wyY2hJNXhqYUFLWlZFdmJwQzJENytFYWszRlJtWnVQbGJpS3g1L0FuUlZoVm5mQlh3SGJtYnVoaElnUGRCXG5yRS8xS1R2b1VsRkZzS3JYOVlaSWdtTm1WbExXNmJNRmh3cThOV0VDZ1lFQXNFNVFLMm8yVGFUNWtNT3AyZnJzXG41ZHdPMkFndzJKYm9rOTR4WlliRlEvMjJ5blZsK3JhNUNsdDNQUGxWcUdsY2hER2ZJd0N1YzY2aWtWV2hQM2psXG5SZE5RSnFUYW1wWHk0MTRNb3lGUmNtTEtNZEF4UGUyTHhvYWNweHhqWUg5clo0QmdBVGJmVXhENWNMcHkramlsXG4wb1g4eUY2NTIzS0VpZGx6eUdkKzFERT1cbi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS1cbiIsCiAgImNsaWVudF9lbWFpbCI6ICJnYy1zdmMtYWNjLWFsbHVyZS1yZXBvcnRlci1wcmpAdGVzdC1nY2UtcHJqY3QuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJjbGllbnRfaWQiOiAiMTAzODg1Nzc0MDI1NDI3MTQxMDk4IiwKICAiYXV0aF91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL2F1dGgiLAogICJ0b2tlbl91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL3Rva2VuIiwKICAiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsCiAgImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvZ2Mtc3ZjLWFjYy1hbGx1cmUtcmVwb3J0ZXItcHJqJTQwdGVzdC1nY2UtcHJqY3QuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iCn0K
echo $WWW | base64 --decode --output ./gcloud-api-key.json
cat ./gcloud-api-key.json

gcloud auth activate-service-account \
    gc-svc-acc-allure-reporter-prj@test-gce-prjct.iam.gserviceaccount.com \
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
                -H "Authorization: token 8UtpEXn3KwVdS-07AbYlmA" \
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