import pandas as pd
import re


def read_files(files):
  df = pd.concat(map(pd.read_csv, files))
  return df

def process_link(link):
  # remove orgs with malformed url
  if link.find('.') == -1:
    return ''
  # exclude facebook/guidestar/wikipedia links
  if 'facebook' in link or 'guidestar' in link or 'wikipedia' in link:
    return ''
  index = link.find('›')
  if index > -1:
    link = link[: index - 1]
  return link

# Name: only first letter is capitalized
# Link: remove Facebook/guidestar
# Remove special characters to keep only valid URLs
# About: Change to sentence case
def process_text(df):
  output = []
  for _, org in df.iterrows(): 
    name = org['name'].title()
    link = org['link']
    about_input = org['about']
    # remove orgs without url or about
    if type(link) == str and type(about_input) == str:
      url = process_link(str(link))
    else:
      continue
    if not url:
      continue
    # about = re.sub(r'\d+', '', about_input.capitalize())
    about = about_input.capitalize()
    output.append([name, url, about])
  return output

def save_results(results, start, stop):
  with open('/content/drive/My Drive/Capstone/processed_data/processed_irs990_' + str(start) + '_' + str(stop) + '.csv', 'w') as f:
    writer = csv.writer(f)
    for data in results:
      writer.writerow(data)

if __name__ == '__main__':
  start = 0
  stop = 3000
  step = 50
  files = ['/content/drive/My Drive/Capstone/scraped_data/links_irs990_' + str(i)
      + '_' + str(i + step) + '.csv' for i in range(start, stop, step)]
  df = read_files(files)
  processed = process_text(df)
  save_results(processed, start, stop)
