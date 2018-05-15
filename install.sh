#! /bin/bash

## This startup script runs ON the compute vm

BUCKET_NAME=allure-poc-prjct-bucket
JAR_NAME=travisMvnGce-1.0-SNAPSHOT-jar-with-dependencies.jar

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
echo " installing all needed dependencies...."

sudo apt-get install dirmngr
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list

echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list

apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
apt-get update

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get install oracle-java8-installer -y

echo "--------------------------------------------------------"
echo " deploying test artifact to env and launching it ...."
mkdir /opt/gcedeploy

gsutil cp gs://${BUCKET_NAME}/${JAR_NAME} /opt/gcedeploy/${JAR_NAME}
java -jar /opt/gcedeploy/${JAR_NAME}

echo "--------------------------------------------------------"
echo " saving test results to GC bucket ...."

gsutil -m cp -r /allure-results gs://${BUCKET_NAME}
gsutil -m cp -r /allure-results/* gs://${BUCKET_NAME}/allure-results_$(date +%F)/


echo "--------------------------------------------------------"
echo "  triggering TravisCI build to finalize run ...."

curl -s -X POST -H "Content-Type: application/json" \
                -H "Accept: application/json" \
                -H "Travis-API-Version: 3" \
                -H "Authorization: token 8UtpEXn3KwVdS-07AbYlmA" \
                https://api.travis-ci.org/repo/uvidit%2FpocProjectDocPages/requests

echo "--------------------------------------------------------"
echo "  stopping GCE instance ...."
sudo shutdown now

echo "  CUT! Thank you all!!! Buy! %)"
echo "========================================================"
exit