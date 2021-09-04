spark-submit \
 --master yarn \
 --deploy-mode cluster \
 --driver-memory 1g \
 --num-executors 2 \
 --executor-cores 1 \
 --executor-memory 1g \
 --conf spark.dynamicAllocation.enabled=true \
 --conf spark.executor.extraJavaOptions=-Duser.timezone=\"UTC\" \
 --conf spark.driver.extraJavaOptions=-Duser.timezone=\"UTC\" \
 --class "com.ravindra.batch.BatchApp" /Users/ravindrayadav/Documents/assignment/sparkProject/lib/sparkProject-1.0-SNAPSHOT.jar \
 --configFile /Users/ravindrayadav/Documents/assignment/sparkProject/conf/batch.properties