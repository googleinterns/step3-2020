from bs4 import BeautifulSoup
import csv
from selenium import webdriver
from urllib.parse import quote_plus
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.options import Options


def read_csv(filename):
  file = open(filename, 'r')
  reader = csv.reader(file)
  # skip the header
  next(reader, None)
  data = list(reader)
  file.close()
  return list(data)

def fetch_webpage(url, browser):
    browser.get(url)
    html = browser.page_source
    return html

def get_google_url(query, num=1, start=0, lang='en'):
  query = quote_plus(str(query))
  url = 'https://www.google.com/search?q={}&num={}&start={}&nl={}'.format(query, num, start, lang)
  return url

def save_as_dict(name, link, about):
  data_dict = {
    "name": name,
    "link": link,
    "about": about
  }
  return data_dict

def find_link(webpage):
  soup = BeautifulSoup(webpage, 'html.parser')
  # find a list of all span elements
  span = soup.find('span', {'class' : 'ellip'})
  if not span:
    link = soup.find('cite')
    if link:
      url = soup.find('cite').get_text()
    else:
      return '', ''
  else:
    url = span.get_text()
  print(url)

  # Find a brief description
  data = soup.find('div', {"class": 'g'})
  if not data:
    return '', ''
  about = data.find('span',{'class':'st'})
  about_text = about.text.strip()
  print(about_text)
  return url, about_text

def search_google(data, start, length):
  names = data[start: start + length]
  # Chrome fails to start on Google Cloud Shell
  # browser = webdriver.Chrome(ChromeDriverManager().install())
  chrome_options = Options()
  chrome_options.add_argument('--headless')
  chrome_options.add_argument('--no-sandbox')
  chrome_options.add_argument('--disable-dev-shm-usage')
  browser = webdriver.Chrome('./chromedriver', options=chrome_options)
  info_dict = []
  for name in names:
    link = get_google_url(name[0])
    print(link)
    webpage = fetch_webpage(link, browser)
    url, about = find_link(webpage)
    info_dict.append(save_as_dict(name[0], url, about))
  return info_dict

def save_results(results, start, length):
  csv_columns = ['name','link','about']
  with open('links_irs990_' + str(start) + '_' + str(start + length) + '.csv', 'w') as f:
    writer = csv.DictWriter(f, fieldnames=csv_columns)
    writer.writeheader()
    for data in results:
        writer.writerow(data)


if __name__ == '__main__':
  # TODO: set the start and num_entries each run
  start = 0
  num_entries = 10
  data = read_csv('irs990_names.csv')
  results = search_google(data, start, num_entries)
  save_results(results, start, num_entries)
  