#!/usr/bin/python3

import numpy as np
import matplotlib.pyplot as plt
import csv

# read data
with open('memory.csv', 'r') as f:
    reader = csv.reader(f)
    data = list(reader)
del data[0] # remove header

dsSizes = [ row[0] for row in data ]
SparkMemory = [ float(row[1]) for row in data ]
WEKAMemory = [ float(row[2]) for row in data ]

plt.plot(dsSizes, SparkMemory, linestyle="-", marker="s", label="Spark")
plt.plot(dsSizes[:4], WEKAMemory[:4], linestyle=":", marker="o", label="WEKA")
plt.ylabel("Memory Consumption (GB)")
plt.xlabel("Percentage of instances of ECBDL14")
plt.legend(loc="lower right")

plt.show()