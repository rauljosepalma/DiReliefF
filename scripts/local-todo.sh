i=10
while [ $i -lt 20 ]
do
  spark-submit --master local --class sparkfs.Main --total-executor-cores 3 /home/raul/Desktop/Spark-FS/project-files/target/scala-2.10/sparkfs_2.10-0.1.0.jar "/home/raul/Datasets/Large/EPSILON/EPSILON.parquet" "/home/raul/Desktop/Spark-FS/project-files/papers/DReliefF/results/EPSILON" 10 $i false
  
  i=$(( $i + 10 ))
done