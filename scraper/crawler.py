try:
    from googlesearch import search
except ImportError:
    print("No module named 'google' found, please run 'pip install google'")


# https://www.geeksforgeeks.org/performing-google-search-using-python-code/
# Search keyword
def search_google(keyword):
    return [query for query in search(keyword,
                                      tld='com',  # The top level domain
                                      lang='en',  # The language
                                      num=10,  # Number of results per page
                                      start=0,  # First result to retrieve
                                      pause=2.0,  # Lapse between HTTP requests
                                      )]
