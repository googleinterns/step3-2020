import numpy as np
import pandas as pd


# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  # fill NANs with 0s
  return data.fillna(0)

def svd(df):
  data = df.drop(columns=['Name'])
  u, s, v = np.linalg.svd(data, full_matrices=False)
  prediction = np.dot(u * s, v)
  return np.rint(prediction)

def main():
  df = read_data('theoretical_data.csv')
  print(df)
  # matrix decomposition with SVD
  prediction = svd(df)
  print(prediction)


if __name__ == '__main__':
  main()
