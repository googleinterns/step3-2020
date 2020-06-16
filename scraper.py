from bs4 import BeautifulSoup
import codecs
import json
from selenium import webdriver
from webdriver_manager.chrome import ChromeDriverManager


def fetch_webpage(url, browser):
    browser.get(url)
    html = browser.page_source
    return html


# find and returns all links that the donate buttons point to in a webpage
def find_donate_links(webpage):
    # testing with local site
    # content = codecs.open(webpage, 'r').read()
    soup = BeautifulSoup(webpage, 'html.parser')
    titles = soup.find_all('a', title='Donate')
    donate = soup.find_all('a', title='donate')
    if donate:
        titles.append(donate)
    Donate = soup.find_all('a', text='Donate')
    if Donate:
        titles.append(Donate)
    links = [entry.get('href') for entry in titles]
    return links


def find_about_pages(webpage):
    soup = BeautifulSoup(webpage, 'html.parser')
    titles = soup.find_all('a', title='About')
    about = soup.find_all('a', text='about')
    if about:
        titles.append(about)
    ABOUT = soup.find_all('a', text='ABOUT')
    if ABOUT:
        titles.append(ABOUT)
    links = [entry.get('href') for entry in titles]
    return links


def scrape(urls):
    browser = webdriver.Chrome(ChromeDriverManager().install())
    info_json = {'result': []}
    for link in urls:
        webpage = fetch_webpage(link, browser)
        about_pages = find_about_pages(webpage)
        donate_links = find_donate_links(webpage)
        info_json['result'].append(save_to_json(link, about_pages, donate_links))
    with open('output.json', 'w') as outfile:
        json.dump(info_json, outfile)


def save_to_json(link, about, donate):
    data_json = {
      "url": link,
      "about": about,
      "donate": donate
    }
    return data_json
