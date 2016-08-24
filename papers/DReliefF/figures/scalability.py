#!/usr/bin/python3

import numpy as np
import matplotlib.pyplot as plt
import csv

# read data
with open('scalability.csv', 'r') as f:
    reader = csv.reader(f)
    data = list(reader)
del data[0] # remove header
numOfCores = [ row[0] for row in data ]
Spark10PercTimes = [ float(row[1]) for row in data ]
Spark20PercTimes = [ float(row[2]) for row in data ]
Spark30PercTimes = [ float(row[3]) for row in data ]
WEKA10PercTimes = [ float(row[4]) for row in data ]
WEKA20PercTimes = [ float(row[5]) for row in data ]
WEKA30PercTimes = [ float(row[6]) for row in data ]

plt.figure(figsize=(6,6), dpi=80)
plt.plot(numOfCores, Spark10PercTimes,linestyle="-",marker="s",label="Spark 10% ECBDL14")
plt.plot(numOfCores, Spark20PercTimes,linestyle="--",marker="s",label="Spark 20% ECBDL14")
plt.plot(numOfCores, Spark30PercTimes,linestyle=".",marker="s",label="Spark 30% ECBDL14")
plt.plot(numOfCores, WEKA10PercTimes,linestyle="-",marker="o", label="WEKA 10% ECBDL14")
plt.plot(numOfCores, WEKA20PercTimes,linestyle="-",marker="o", label="WEKA 20% ECBDL14")
plt.plot(numOfCores, WEKA30PercTimes,linestyle="-",marker="o", label="WEKA 30% ECBDL14")
plt.ylabel("time (minutes)")
plt.xlabel("number of executor cores")
plt.legend(loc="lower right")

plt.show()