import crawler
import scraper

if __name__ == '__main__':
    query = 'BLM'
    query_result = crawler.search_google(query)
    scraper.scrape(query_result)
