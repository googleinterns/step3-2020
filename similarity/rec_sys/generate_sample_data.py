import random
import pandas as pd


# Reads and process input data with Pandas Dataframe
def read_name_data(filename):
  fields = ['Organization']
  data = pd.read_csv(filename, skipinitialspace=True, usecols=fields)
  return data

def random_gen(df):
  users = ['Tony', 'Elijah', 'Iris', 'Ali', 'Jess']
  for person in users:
    # ratings = np.random.randint(-1, 2, 50)
    ratings = random.choices(population=[-1, 0, 1], weights=[0.1, 0.8, 0.1], k=50)
    df[person] = ratings

def main(path):
  org_names = read_name_data(path + 'sample_data.csv')
  random_gen(org_names)
  org_names.to_csv(path + 'random_ratings.csv')


if __name__ == '__main__':
  main('../data/')