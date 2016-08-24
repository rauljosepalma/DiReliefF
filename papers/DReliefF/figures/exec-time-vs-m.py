#!/usr/bin/python3

import numpy as np
import matplotlib.pyplot as plt
import csv

# read data
with open('exec-time-vs-m.csv', 'r') as f:
    reader = csv.reader(f)
    data = list(reader)
del data[0] # remove header

numOfSamples = [ row[0] for row in data ]
SparkTimes = [ float(row[1]) for row in data ]
WEKATimes = [ float(row[2]) for row in data ]

plt.plot(numOfSamples, SparkTimes, linestyle="-", marker="s", label="Spark")
plt.plot(numOfSamples, WEKATimes, linestyle=":", marker="o", label="WEKA")
plt.ylabel("Execution Time (minutes)")
plt.xlabel("Number of Samples m for a 10% size ECBDL14")
plt.legend(loc="lower right")

plt.show()