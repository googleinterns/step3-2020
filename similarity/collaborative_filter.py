import numpy as np
import pandas as pd

# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  return data

# calculate the Pearson's correlation coefficient among everyone
def calc_pearson(df):
  # for i, row in df.iterrows():
  corr = df.corr(method='pearson')
  return corr

def sort_index(corr):
  sorted = np.argsort(corr, axis=1)
  return sorted

def main():
  df = read_data('theoretical_data.csv')
  # calculate pearson coefficient
  corr = calc_pearson(df)
  # find k most similar users
  sorted = sort_index(corr)
  print(sorted)
  # (fill in NANs with most similar orgs)

  # or matrix decomposition
  # But how do I handle NANs?

if __name__ == '__main__':
  main()
