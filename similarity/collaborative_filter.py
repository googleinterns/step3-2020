import numpy as np
import pandas as pd

# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename)
  # fill NAN with 0
  return data.fillna(0)

# calculate the Pearson's correlation coefficient among everyone
def calc_pearson(df):
  corr = df.corr(method='pearson')
  return corr

def sort_index(corr):
  sorted = np.argsort(corr, axis=1)
  return sorted

# fill unknowns with ratings from the k most similar users
def fill_knn(df, sorted):
  result = df.copy()
  for row_i, row in df.iterrows():
    for col_i, col in enumerate(row):
      # row (feature), col (user), num == 0
      if not col:
        # find the closest person
        nn_index = sorted.iloc[col_i - 1][-2] + 1
        closest_user = df.columns.values[nn_index]
        preference = df[closest_user][row_i]
        result.at[row_i, df.columns.values[col_i]] = preference
  return result

def main():
  df = read_data('theoretical_data.csv')
  print(df)
  # calculate pearson coefficient
  corr = calc_pearson(df)
  # find k most similar users
  sorted = sort_index(corr)
  # (fill in NANs with most similar orgs)
  # k = 1
  result = fill_knn(df, sorted)
  print(result)
  # or matrix decomposition
  # But how do I handle NANs?

if __name__ == '__main__':
  main()
