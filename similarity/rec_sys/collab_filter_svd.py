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
  return prediction, processed

def read_proto(filename):
  with open(filename, 'rb') as input:
    orgs = knn_pb2.Organizations()
    orgs.ParseFromString(input.read())
  return orgs.orgs

def fill_with_neighbors(df, neighbors, user):
  filled = df.copy()
  for i, rated in enumerate(df[user]):
    if not rated:
      # find k-NN when the user matrix is too sparse
      n1_index = neighbors[i].neighbors[0].id
      n2_index = neighbors[i].neighbors[1].id
      n3_index = neighbors[i].neighbors[2].id
      if df[user][n1_index]:
        filled.at[n1_index, user] = df[user][n1_index]
      elif df[user][n1_index]:
        filled.at[n2_index, user] = df[user][n2_index]
      elif df[user][n1_index]:
        filled.at[n3_index, user] = df[user][n3_index]
      
  return filled

def fill_sparsity(df, user):
  neighbors = read_proto('../data/neighbors.txt')
  data = fill_with_neighbors(df, neighbors, user)
  return data

def make_recommendations(input, prediction, user):
  # print previous likes
  print('Because', user, 'liked:', end=' ')
  count = 0
  for i, rated in enumerate(input[user]):
    if rated > 0:
      print(input['Name'][i], end=', ')
      count += 1
  print('\n', user + ' will also like:', end=' ')
  for i, rated in enumerate(input[user]):
    if not rated:
      new_rating = prediction[user][i]
      if new_rating > 0:
        print(prediction['Name'][i], end=', ')
  print()

def main(filename, user):
  df = read_data(filename)
  print('input:\n', df)

  # Fill sparse matrix with text similarity from kNN
  data = fill_sparsity(df, user) 
  print('filled:\n', data)

  # matrix decomposition with SVD
  prediction, processed_prediction = collaborative_filtering(data)
  print('processed:\n', prediction)

  # make recommendation
  make_recommendations(df, processed_prediction, user)


if __name__ == '__main__':
  main('../data/random_ratings.csv', 'Tony')
