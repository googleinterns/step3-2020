import csv
from googlesearch import search

def read_csv(filename):
  file = open(filename, 'r')
  reader = csv.reader(file)
  # skip the header
  next(reader, None)
  data = list(reader)
  file.close()
  return list(data)

def search_google(data, start, length):
  names = data[start: start + length]
  url = []
  for org in names:
    link = [query for query in search(org[0],
        tld='com',  # The top level domain
        lang='en',  # The language
        num=1,  # Number of results per page
        start=0,  # First result to retrieve
        pause=2.0,  # Lapse between HTTP requests
        )]
    print(link[0])
    url.append(link[0])
  return url

def save_results(results, start, length):
  with open('links_irs990_' + str(start) + '_' + str(start + length) + '.csv', 'w') as f:
    writer = csv.writer(f, quoting=csv.QUOTE_ALL)
    writer.writerow(results)


if __name__ == '__main__':
  # TODO: set the start and num_entries each run
  start = 0
  num_entries = 1
  data = read_csv('irs990_names.csv')
  results = search_google(data, start, num_entries)
  save_results(results, start, num_entries)
  