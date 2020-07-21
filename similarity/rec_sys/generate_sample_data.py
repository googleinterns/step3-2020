import json
import numpy as np
import pandas as pd
import random


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

def demo(path):
  org_names = read_name_data(path + 'sample_data.csv')
  random_gen(org_names)
  org_names.to_csv(path + 'random_ratings.csv')


def read_json(path):
  filename = path + 'ratings.json'
  with open(filename, 'r') as input:
    data = json.loads(input.read()) 
  return data

def gen_from_sql(path):
  # TODO: get the actaul number of organizations
  size = 8000
  data = read_json(path)
  df = pd.DataFrame(index=range(size))
  print(df)
  for entry in data:
    email = entry.email
    if email in df:
      df.at[email]

if __name__ == '__main__':
  path = '../data/'
  # demo(path)
  gen_from_sql(path)
