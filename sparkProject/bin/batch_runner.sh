spark-submit \
--master k8s://https://127.0.0.1:32788 \
--deploy-mode cluster \
--conf spark.executor.cores=3 \
--conf spark.executor.memory=1g \
--conf spark.executor.instances=2 \
--conf spark.driver.memory=1g \
--conf spark.kubernetes.container.image=gcr.io/spark-operator/spark:v3.1.1 \
--conf spark.executor.extraJavaOptions=-Duser.timezone=\"UTC\" \
--conf spark.driver.extraJavaOptions=-Duser.timezone=\"UTC\" \
--conf spark.kubernetes.namespace=default \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.mount.path=/files \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.path=/files \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.readOnly=true \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.subPath=/files/Documents/assignment/sparkProject/lib/sparkProject-1.0-SNAPSHOT.jar \
--conf spark.kubernetes.driver.volumes.hostPath.localvol.options.type=DirectoryOrCreate \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.mount.path=/files \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.path=/files \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.readOnly=true \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.subPath=/files/Documents/assignment/sparkProject/lib/sparkProject-1.0-SNAPSHOT.jar \
--conf spark.kubernetes.executor.volumes.hostPath.localvol.options.type=DirectoryOrCreate \
--conf spark.kubernetes.file.upload.path=files/Documents/assignment/sparkProject \
--class "com.ravindra.batch.BatchApp" local:///files/Documents/assignment/sparkProject/lib/sparkProject-1.0-SNAPSHOT.jar \
--configFile /files/Documents/assignment/sparkProject/conf/batch.properties