import knn_pb2
import numpy as np
import pandas as pd
from scipy.sparse import csc_matrix
from scipy.sparse.linalg import svds


# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  # fill NANs with 0s
  return data.fillna(0)

def svd(df, k):
  data = df.drop(columns=['Name']).to_numpy()
  sparse_matrix = csc_matrix(data, dtype=float)
  # take k most significant features
  u, s, v = svds(sparse_matrix, k)
  s_mat = np.diag(s)
  # reconstruct to make prediction
  prediction = u.dot(s_mat).dot(v)
  print('prediction:\n', prediction)
  return prediction

def edit_data(input, prediction):
  output = input.copy()
  for row_i, row in input.iterrows():
    for col_i, col in enumerate(row):
      # row (feature), col (user), num == 0
      if not col:
        output.at[row_i, input.columns.values[col_i]] = prediction[row_i, col_i - 1]
  return output

"""
Rounds floats [-1.0f, 1.0f] to int {-1, 0, 1} away from 0
takes one float as input
returns one int as output
"""
def round_away_from_zero(input):
  output = 0
  if input < 0:
    output = -1
  elif input > 0:
    output = 1
  return output

def collaborative_filtering(df):
  prediction = svd(df, 2)
  rounding_func = np.frompyfunc(round_away_from_zero, 1, 1)
  int_result = rounding_func(prediction).astype(np.int8)
  processed = edit_data(df, int_result)
  return processed

def read_proto(filename):
  with open(filename, 'rb') as input:
    orgs = knn_pb2.Organizations()
    orgs.ParseFromString(input.read())
  return orgs

  for org in orgs.orgs:
    print(org.id, ': ')
    for n in org.neighbors:
      print(n.id)
    print()

def fill_with_neighbors(df, neighbors, k=1):
  # for row_i, row in df.iterrows():
  #   for col_i, col in enumerate(row):
  #     # row (feature), col (user), num == 0
  #     if not col:
  pass

def main(filename):
  df = read_data(filename)
  print('input:\n', df)

  # TODO: Fill sparse matrix with text similarity from kNN
  # neighbors = read_proto('../data/neighbors.txt')
  # fill_with_neighbors(df, neighbors)

  # matrix decomposition with SVD
  result = collaborative_filtering(df)
  print('processed:\n', result)


if __name__ == '__main__':
  main('../data/random_ratings.csv')
