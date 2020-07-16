import numpy as np
import pandas as pd


# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  # fill NANs with 0s
  return data.fillna(0)

def svd(df, k):
  data = df.drop(columns=['Name'])
  u, s, v = np.linalg.svd(data, full_matrices=False)
  s_mat = np.diag(s)
  # prediction = np.dot(u, np.dot(s_mat, v))
  
  # take k most significant features
  u = u[:, 0:k]
  s_mat = s_mat[0:k, 0:k]
  v = v[0:k, :]
  s_root = np.sqrt(s_mat)
  # u root_s * root_sv
  prediction = (u.dot(s_root)).dot(s_root.dot(v))

  return prediction

def main():
  df = read_data('theoretical_data.csv')
  print(df)
  # matrix decomposition with SVD
  prediction = svd(df, 2)
  int_result = np.rint(prediction)
  print(int_result)
  # processed = edit_data(df, int_result)
  # print(processed)


if __name__ == '__main__':
  main()
