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

def generate_dataframe(data, size):
  df = pd.DataFrame(index=range(size))
  for entry in data:
    email = entry['email']
    if email not in df:
      df[email] = np.zeros(size)
    df.at[int(entry['id']), email] = entry['rating']
  return df

def gen_from_sql(path):
  # TODO: get the actaul number of organizations
  size = 8000
  data = read_json(path)
  df = generate_dataframe(data, size)
  df.to_csv(path + 'user_ratings.csv')

if __name__ == '__main__':
  path = '../data/'
  # demo(path)
  gen_from_sql(path)
