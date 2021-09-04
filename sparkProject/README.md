#### Project Overview

[![Build Status](https://jenkins.datasparkanalytics.com/buildStatus/icon?job=DS_algo)](https://jenkins.datasparkanalytics.com/job/DS_algo)

#### Dependencies

* Scala 2.12
* Spark 3.1.1

## Building with Maven

    $ mvn clean install

### Skipping tests

    $ mvn clean install -DskipTests
    
    
Minikube Setup
Install and run Minikube:

#### Install Minikube
```
https://minikube.sigs.k8s.io/docs/start/
```

#### Install and Set Up kubectl to deploy and manage apps on Kubernetes
```
https://kubernetes.io/docs/tasks/tools/
```

#### Start the Kubernetes cluster using Minikube
Check versions
```
$ minikube version
$ kubectl version
```
Start a kubernetes cluster using minikube. Obviously, you will need to set values on your resources that your machine can support.
```
$ minikube start \
-p <PROFILE-NAME> \
--mount --mount-string \
</LOCAL/FILESYSTEM/>:</CONTAINER/MOUNT/POINT/> \
--disk-size <DISK-SIZE> \
--memory <MEMORY-SIZE> \
--cpus <NUM-OF-CPU> \
--driver=docker \
--kubernetes-version=<K8S-VERSION>

where:
<PROFILE-NAME> = name of the cluster
</LOCAL/FILESYSTEM/> = The local machine’s filesystem path you want to mount to minikube and on the containers
</CONTAINER/MOUNT/POINT/> = The mount point on containers and minikube
<DISK-SIZE> = disk size in GB, e.g. 100g
<MEMORY-SIZE> = memory size in MB, e.g. 16384
<NUM-OF-CPU> = number of CPU, e.g. 6
<K8S-VERSION> = kubernetes version, e.g. 1.15.5

Example:
minikube start \
-p minikube-local \
--mount --mount-string \
/Users/marvinlandicho/:/files/ \
--disk-size 100g \
--memory 16384 \
--cpus 6 \
--driver=docker \
--kubernetes-version=1.19.8
```

Validate that you are on the right contexts. The asterisk (*) on the CURRENT column show which context you are in
```
$ kubectl config get-contexts
$ kubectl config use-context <PROFILE-NAME>
```
There may be time that you will need to ssh to your minikube container running the kubernetes cluster. List the minikube profiles by running this command
```
$ minikube ssh -p <PROFILE-NAME>
```
Launch the Kubernetes dashboard to access those logs and see the status of your cluste
```
$ minikube dashboard -p <PROFILE-NAME>
```
To delete the kubernetes cluster and all the resources that were created
```
$ minikube delete --all --purge
```

#### Running Spark Job on Kubernetes

View the cluster information by running this command. It is important to collect this as the API endpoint will always be different from each installations. You will use it to point your spark-submit job
```
$ kubectl cluster-info
```
Create namespace, service account, cluster role bindings. This will allow the service account to be able to create and delete pods, services and resources for running a spark job.
```
$ kubectl create -f ./kubernetes/spark-rbac.yaml
```

Use any publically available docker Spark image 
```
e.g. gcr.io/spark-operator/spark:v3.1.1
```

Install spark-submit binary on your machine. You can download the binary for a specific version
```
https://archive.apache.org/dist/spark/
```

Below is a sample spark-submit job that allows reading and writing to your local machine. More details about running spark on kubernetes are described
https://spark.apache.org/docs/latest/running-on-kubernetes.html
```
$ ./spark-submit \
--master k8s://https://127.0.0.1:54974 \
--deploy-mode cluster \
--name spark-pi \
--class org.apache.spark.examples.SparkPi \
--conf spark.kubernetes.namespace=default \
--conf spark.executor.instances=2 \
--conf spark.kubernetes.container.image=dataspark-docker-snapshots.artifactory.datasparkanalytics.com/kubernetes/spark/spark:v3.0.0-v-1.1 \
--conf spark.kubernetes.container.image.pullSecrets=artifactory-creds \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.mount.path=/files \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.path=/files \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.readOnly=true \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.subPath=/files/Build/DataSpark/minikube-cluster/jars/spark-examples_2.12-3.0.0.jar \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.type=DirectoryOrCreate \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.mount.path=/files \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.path=/files \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.readOnly=true \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.subPath=/files/Build/DataSpark/minikube-cluster/jars/spark-examples_2.12-3.0.0.jar \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.type=DirectoryOrCreate \
local:///files/Build/DataSpark/minikube-cluster/jars/spark-examples_2.12-3.0.0.jar \
100000
```

#### [Optional] Installing Spark-operator
The Kubernetes Operator for Apache Spark aims to make specifying and running Spark applications as easy and idiomatic as running other workloads on Kubernetes. It uses Kubernetes custom resources for specifying, running, and surfacing status of Spark applications. For a complete reference of the custom resource definitions, please refer to the API Definition. For details on its design, please refer to the design doc. It requires Spark 2.3 and above that supports Kubernetes as a native scheduler backend.
```
https://github.com/GoogleCloudPlatform/spark-on-k8s-operator
```
Install the helm chart
```
helm repo add \
spark-operator \
https://googlecloudplatform.github.io/spark-on-k8s-operator 
```

Install the spark operator
```
helm install spark-operator \
spark-operator/spark-operator \
--namespace au-daas-spark \
--set sparkJobNamespace="au-daas-spark",nodeSelector.application="spark",webhook.enable=true,webhook.namespaceSelector="name=au-daas-spark",image.tag="v1beta2-1.2.3-3.1.1"
```

#### Configurations
config `Contains the configuration for batch and streaming apps`

Spark Streaming App
```
spark_app_name = <SPARK APPLICATION NAME>
input_stream_dir = <LOCATION OF STREAM DIRECTORY>
input_file_format = <>
input_delimiter = <CSV FILE DELIMITER>
input_header = <FLAG TO INDICATE HEADER AVAILABILITY>
max_files_per_trigger = <MAXIMUM NUMBER OF FILES FOR PROCESSING>
checkpoint_dir = <LOCATION OF CHECKPOINT DIRECTORY>
source_archive_dir = <LOCATION OF ARCHIVE DIRECTORY>
clean_source = <CLEAN UP PROCESSED FILES>
output_path = <LOCATION OF OUTPUT DIRECTORY>
output_delimiter = <CSV FILE DELIMITER>
output_header = <FLAG TO INDICATE HEADER AVAILABILITY>
block_size = <STORAGE BLOCK SIZE>
```

Spark Batch App
```
spark_app_name = <SPARK APPLICATION NAME>
input_path = <LOCATION OF SOURCE DIRECTORY OR FILE>
input_delimiter = <CSV FILE DELIMITER>
input_header = <FLAG TO INDICATE HEADER AVAILABILITY>
output_path = <LOCATION OF OUTPUT DIRECTORY>
output_delimiter = <CSV FILE DELIMITER>
output_header = <FLAG TO INDICATE HEADER AVAILABILITY>
block_size = <STORAGE BLOCK SIZE>
sql_query = <SQL QUERY>
```

#### Spark Apps Runners
bin `Contains the runner for batch and streaming apps`

Spark Streaming App
```
sh ./bin/streaming_runner.sh
```
Spark Batch App
```
sh ./bin/batch_runner.sh
```

#### Spark Apps Libraries/packages
lib `Contains the binary for batch and streaming apps`

#### Data Directories
data `Contains Input/Output directories for batch and streaming apps`

Spark Streaming App
```
stream_data  -> STREAMING DIRECTORY
output/stream -> STREAMING OUTPUT DIRECTORY
```
Spark Batch App
```
raw_data -> SOURCE DIRECTORY
output/batch -> BATCH OUTPUT DIRECTORY
```

#### Usage
1. Clone the project
2. cd spark-project

3. Spark Batch App
   1. Update the configs
   2. run batch sh ./bin/batch_runner.sh
   3. Output will be available at output directory

4. Spark Streaming App
   1. Update the configs
   2. run streaming sh ./bin/streaming_runner.sh
   3. Output will be available at output directory


#### Tradeoffs compared to alternative solutions
Apache Spark Vs Apache Flink
1. `Throughput` Spark relies on micro-batching and Flink relies on Event-based Streaming
2. `Windowing` Spark supports Time based and Flink supports Time and count based
3. `Auto-scaling` Spark support auto scaling and Flink does not
4. `State Management` Flink only support distributed snapshots 
5. `Community Support` Spark has about 1000 and Flink has about 400

#### Potential Improvements
1. Moving the Input streaming from files to Kafka
2. Moving to Apache Flink
