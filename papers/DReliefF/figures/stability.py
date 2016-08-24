#!/usr/bin/python3

import numpy as np
import matplotlib.pyplot as plt
import csv

BASEPATH = '/home/raul/results/ECBDL14_10perc'
K = 10
MAX_M = 100
STEP_M = 10

def averageDiff(wts1, wts2):
  return sum([ abs(wts1[i] - wts2[i]) for i in range(len(wts1)) ]) / len(wts1)

# Contains the diffs between a weights vector an its predecesor
# First diff is 0 
diffs = [0.0]
previousWeights = []

i = 0
for m in range(STEP_M, MAX_M + STEP_M, STEP_M):
  with open(BASEPATH + "_k{0}m{1}_feats_weights.txt".format(K, m), 'r') as f:
    reader = csv.reader(f)
    currentWeights = [ float(weight) for weight in list(reader)[0] ] # There should be only one line
  if i > 0:
    # print("CURR WEIGHTS:")
    # print(currentWeights)
    # print("PREV WEIGHTS:")
    # print(previousWeights)
    diffs.append(averageDiff(currentWeights, previousWeights))
  previousWeights = currentWeights
  i += 1

# Plot diffs
# print(diffs)
plt.plot(np.arange(STEP_M, MAX_M + STEP_M, STEP_M),  diffs)
plt.show()