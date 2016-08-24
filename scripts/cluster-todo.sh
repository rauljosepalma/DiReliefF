# WEKA

export CLASSPATH=/home/raul/Software/weka-3-8-0/weka.jar
time java -Xmx120g weka.attributeSelection.ReliefFAttributeEval -M 10 -D 1 -K 10 -i ECBDL14_20perc.arff
time java -Xmx120g weka.attributeSelection.ReliefFAttributeEval -M 10 -D 1 -K 10 -i ECBDL14_30perc.arff
time java -Xmx120g weka.attributeSelection.ReliefFAttributeEval -M 10 -D 1 -K 10 -i ECBDL14_40perc.arff



/opt/spark/bin/spark-submit --name=HIGGS --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/HIGGS_train.parquet" 10 20

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/HIGGS_train.parquet" "/root/HIGGS_feats_knn.txt" "NaiveBayes" > /root/HIGGS_naivebayes.txt

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/HIGGS_train.parquet" "full" "NaiveBayes"

// $1 = Dataset name
// $2 = Classifier: NaiveBayes, RandomForest

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-saver.jar "hdfs://master:8020/datasets/ECBDL14.parquet" 0.15
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_15perc.parquet" 10 10


/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/KDDCUP99_train.parquet" 7 10


i=0
while [ $i -lt 3 ]
do
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/$1_train.parquet" 10 10
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "/root/$1_feats_positive.txt" "$2"
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "/root/$1_feats_10perc.txt" "$2"
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "/root/$1_feats_25perc.txt" "$2"
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "/root/$1_feats_knn.txt" "$2"
  rm $1_feats_10perc.txt
  rm $1_feats_25perc.txt
  rm $1_feats_positive.txt
  rm $1_feats_knn.txt

  i=$(( $i + 1 ))
done
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "full" "$2"


/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0.jar "hdfs://master:8020/datasets/ECBDL14_train.parquet" 10 10
  /opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-classifier.jar "hdfs://master:8020/datasets/$1_train.parquet" "/root/$1_feats_10perc.txt" "$2"

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0.jar "hdfs://master:8020/datasets/HIGGS_train.parquet" 10 10
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0.jar "hdfs://master:8020/datasets/_train.parquet" 10 10



# Importante!!!
{ sleep 3h ; ./cluster-todo-next.sh HIGGS RandomForest > HIGGS_RandomForest_US ; } &
{ sleep 2h ; ./cluster-todo-next.sh ; } &

./cluster-todo.sh EPSILON RandomForest > EPSILON_RandomForest.txt

/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 1 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 2 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 3 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 4 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 5 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 6 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 7 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 8 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 9 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 10 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 11 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 12 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 13 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_10perc.parquet" "/root/results2" 10 10 "false"


/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 1 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 2 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 3 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 4 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 5 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 6 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 7 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 8 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 9 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 10 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 11 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 12 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 13 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_20perc.parquet" "/root/results2" 10 10 "false"

/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 1 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 2 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 3 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 4 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 5 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 6 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 7 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 8 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 9 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 10 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 11 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 12 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"
/opt/spark/bin/spark-submit --class sparkfs.Main --total-executor-cores 13 /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results2" 10 10 "false"

echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_30perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_40perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_50perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_60perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_70perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_80perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_90perc.parquet" "/root/results" 10 10 "false"
echo "\n" >> /root/results/ECBDL14_k10m10_feats_10perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_knn.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_25perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_positive.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_50perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_weights.txt
echo "\n" >> /root/results/ECBDL14_k10m10_feats_75perc.txt
echo "\n" >> /root/results/ECBDL14_k10m10_hits_contrib.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14.parquet" "/root/results" 10 10 "false"




/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/HIGGS_train.parquet" 10 50

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_train.parquet" 10 50
mv ECBDL14_feats_10perc.txt ECBDL14_feats_10perc-1.txt 
mv ECBDL14_feats_25perc.txt ECBDL14_feats_25perc-1.txt 
mv ECBDL14_feats_knn.txt ECBDL14_feats_knn-1.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/ECBDL14_train.parquet" 10 50

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/EPSILON_train.parquet" 10 50
mv EPSILON_feats_10perc.txt EPSILON_feats_10perc-1.txt 
mv EPSILON_feats_25perc.txt EPSILON_feats_25perc-1.txt 
mv EPSILON_feats_knn.txt EPSILON_feats_knn-1.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/EPSILON_train.parquet" 10 50

/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/KDDCUP99_train.parquet" 10 50
mv KDDCUP99_feats_10perc.txt KDDCUP99_feats_10perc-1.txt 
mv KDDCUP99_feats_25perc.txt KDDCUP99_feats_25perc-1.txt 
mv KDDCUP99_feats_knn.txt KDDCUP99_feats_knn-1.txt
/opt/spark/bin/spark-submit --class sparkfs.Main /root/spark-fs-0.1.0-relieff.jar "hdfs://master:8020/datasets/KDDCUP99_train.parquet" 10 50
