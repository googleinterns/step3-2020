import numpy as np
import pandas as pd


# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  # fill NANs with 0s
  return data.fillna(0)

def svd(df):
  data = df.drop(columns=['Name'])
  print(data)
  u, s, v = np.linalg.svd(data, full_matrices=False)
  print(u.shape, s.shape, v.shape)
  prediction = u.dot(s)
  print(prediction)

def main():
  df = read_data('theoretical_data.csv')
  svd(df)
  # matrix decomposition with SVD


if __name__ == '__main__':
  main()
