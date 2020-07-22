import pandas as pd


# Reads and process input data with Pandas Dataframe
def read_data(filename):
  fields = ['name', 'link']
  data = pd.read_csv(filename, skipinitialspace=True, usecols=fields)
  return data

def evaluate(g4np, scraped):
  scraped_i = 0
  g4np_i = 0
  correct = 0
  while g4np_i < 9997:
    g4np_row = g4np.iloc[g4np_i]
    scraped_row = scraped.iloc[scraped_i]
    while scraped_row['name'].lower() != g4np_row['name'].lower():
      g4np_i += 1
      g4np_row = g4np.iloc[g4np_i]
    scraped_link = str(scraped_row['link'])
    if scraped_link.find('www') != -1:
      scraped_link = scraped_link.replace('www.', '')
    if scraped_link in str(g4np_row['home_page']):
      correct += 1
    else:
      print('name:', scraped_row['name'], 'scraped:', scraped_row['link'], 'g4np:', g4np_row['home_page'])
    g4np_i += 1   
    scraped_i += 1
  return correct

def main():
  g4np_filename = '/content/drive/My Drive/Capstone/G4NP/raw_data/G4NP_links_0_10000.csv'
  scraped_filename = '/content/drive/My Drive/processed_g4np_0_10000.csv'
  g4np_data = pd.read_csv(g4np_filename, skipinitialspace=True)
  scraped_data = read_data(scraped_filename)
  count = evaluate(g4np_data, scraped_data)
  print(count)
  