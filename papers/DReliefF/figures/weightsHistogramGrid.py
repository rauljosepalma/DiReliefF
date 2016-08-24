#!/usr/bin/python3

import numpy as np
import matplotlib.pyplot as plt
import csv

NUM_ROWS = 2
NUM_COLS = 5
NUM_BINS = 50

# read data
with open('/home/raul/head1000_feats_weights.csv', 'r') as f:
  reader = csv.reader(f)
  data = list(reader)

# Plot each weight array on a subplot
(i, j) = (1, 1)
allWeights = []
plt.figure(1)
for pltNum, row in enumerate(data) :
  weights = [ float(e) for e in row ]
  allWeights.append(weights)
  plt.subplot(NUM_ROWS,NUM_COLS, pltNum + 1)
  n, bins, patches = plt.hist(weights, NUM_BINS)
  
# Plot the whole dataset
plt.figure(2)
plt.hist(allWeights, NUM_BINS)

plt.show()


# dsSizes = [ row[0] for row in data ]
# SparkMemory = [ float(row[1]) for row in data ]
# WEKAMemory = [ float(row[2]) for row in data ]

# plt.plot(dsSizes, SparkMemory, linestyle="-", marker="s", label="Spark")
# plt.plot(dsSizes[:4], WEKAMemory[:4], linestyle=":", marker="o", label="WEKA")
# plt.ylabel("Memory Consumption (GB)")
# plt.xlabel("Percentage of instances of ECBDL14")
# plt.legend(loc="lower right")

# plt.show()