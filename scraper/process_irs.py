import csv
from googlesearch import search

def read_csv(filename):
  with open(filename, 'r') as f:
    reader = list(csv.reader(f))
  return reader

if __name__ == '__main__':
  data = read_csv('irs990_names.csv')
  print(len(data))
  start = 0
  num_entries = 10
  # search(start, num_entries)
