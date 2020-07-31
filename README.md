# Activism Platform - Nonprofit Finder

## Objective
The Activism Platform organizes and promotes activism by connecting users to nonprofit organizations:
  - Promote accessibility with searching for organizations feature
  - Promote impact and reach with recommending similar organizations feature
  - Promote relevancy with crowdsourcing from users feature

![Alt Text](readme/search.gif)


## STEP Internship
The web app is our capstone project for our [Google STEP Internship](https://buildyourfuture.withgoogle.com/programs/step/). This internship offers first and second-year undergraduate students the opportunity to work on a software project alongside other STEP interns and full-time Googlers, and provides the chance to bridge the gap between academic understanding and practical professional experience.


## Table of Contents
- [Objective](#Objective)
- [STEP Internship](#STEP-Internship)
- [Features](#Features)
  - [Searching and Ranking](#Searching-and-Ranking)
    - [Search by Keyword](#Search-by-Keyword)
    - [Search by Category](#Search-by-Category)
    - [Ranking](#Ranking)
    - [Pagination](#Pagination)
  - [Machine Learning](#Machine-Learning)
    - [Text Similarity](#Text-Similarity)
    - [Collaborative Filter](#Collaborative-Filter)
  - [Crowdsourcing](#Crowdsourcing)
    - [Uploading](#Uploading)
    - [Moderating](#Moderating)
- [Technology Stack](#Technology-Stack)
- [Authors](#Authors)
- [Acknowledgements](#Acknowledgements)


## Features
Here are some of the main features of our web app
### Searching and Ranking
Our searching functionality helps make organizations more accessible to users. You can search by keyword or by category.

#### Search by Keyword
Simply type in a keyword or key phrase of your interest in the search box and hit enter or click the search icon. It will then take you to the results page. You can search from any page of our website by clicking on the search icon on the top navigation bar.

![Alt Text](readme/search.gif)


#### Search by Category
Interested in a category of organizations? We got you! Powered by the [Google Cloud Natural Language](https://cloud.google.com/natural-language) Content classification, we have grouped organizations by their names and mission statements for your convenience. Click on the arrow on the side navigation bar to explore the categories, and click on a category's text to see organizations in that category.

![Alt Text](readme/category.gif)


#### Ranking
We value your opinion! If you like an organization, give it a thumbs up. If you dislike one, you can also give it a thumbs down to help other users. We rank the search results for either keyword or category by the net number of thumbs ups. Net thumbs ups = number of likes - number of dislikes, so go ahead and give your favorite nonprofit a thumbs up!

![Alt Text](readme/rating.gif)


#### Pagination
Believe it or not, we have collected information on thousands of organizations for you! We only show 10 results per page. You can navigate through the other results with our intuitive pagination UI!

![Alt Text](readme/pagination.gif)


### Machine Learning
We use Machine Learning (ML) to recommend organizations that a user may like to promote impact and reach of nonprofits.
#### Text Similarity
We assigned each organization four other nonprofits that are "Like this." We achieved so with text similarity analysis on the names and mission statements of all of the organizations in our database. Here is our process for text similarity:
1. Remove stopwords (a, the, is etc.), number, and punctuations from sentences, and convert all text to lowercase
2. Lemmatize the sentences by grouping inflected forms of words together (i.e. "likes" is the same as "like")
3. Embeds sentences into weighted vectors with the [TensorFlow Universal Sentence Encoder](https://tfhub.dev/google/universal-sentence-encoder/4)
4. Train a k-Nearest Neighbor (k-NN) classifier on the weighted word vectors
5. Feed our model an input (name + mission statement of an organization) and let our ML model find most similar organizations to the input

![Alt Text](readme/similarity.gif)


#### Collaborative Filter
We can also make very personalized recommendations to users based on their ratings of organizations and other users' ratings. The idea is that people of similar interests are probably going to like the same organizations. Our collaborative filter not only captures the similarity among users, but also utilizes the [text similarity](#Text-Similarity) results from the previous section. Here is our workflow:
1. Fetch the user ratings: thumbs up is stored as 1.0 and thumbs down is stored as -1.0
2. Generate matrix with (number of organizations) rows and (number of users) columns
3. Fill unrated organizations in the matrix with ratings of similar (text similarity) organizations from the same user
4. Run Singular Value Decomposition (SVD) on the filled matrix, and for dimensionality reduction, only keep k most significant singular values
5. Recompose a matrix as our prediction
6. Find three new organizations with the highest predited ratings to recommend to each user
 
![Alt Text](readme/recommend.png)


### Crowdsourcing
Our web app promotes relevancy with a crowdsourcing feature along with admin tools to moderate user uploaded content.
#### Uploading
Know an organization that is not yet in our database? Please contribute! You can enter the organization's name, website link, and mission statement in our upload page.

![Alt Text](readme/crowdsource.png)


#### Moderating
To ensure the high quality of the content in our database, we built an admin console to review all uploaded submissions. The admin console displays existing organizations of the same name along with the submitted information for verification. An admin can either approve (send new organization to our database) or not approve (discard this uploaded entry).

![Alt Text](readme/admin.png)


### Disclaimers on Data Acquisition
The nonprofit organizations data is acquired through scraping publicly available information from the Internet. We obtained a list of names of nonprofits from Google for Nonprofits (G4NP). We then searched the names of organizations on Google and fetched the first result. The link and description are processed based on the Google search results.

## Technology Stack
Frontend:
- [Figma](https://www.figma.com/): UI design sketches
- [Material Design](https://material.io/) - Design system for the frontend
- [UIKit](https://getuikit.com/docs/introduction) - CSS and Javascript framework

Backend:
- [Apache Maven](https://maven.apache.org/): Build and deploy web app
- [Google Cloud Platform (GCP)](https://cloud.google.com/)
  - [App Engine (Java 8)](https://cloud.google.com/appengine/docs/standard/java): Hosts web servlet
  - [Cloud Natural Language](https://cloud.google.com/natural-language): Classify nonprofit category from text
  - [Cloud SQL for MySQL](https://cloud.google.com/sql/docs/mysql): Database storing organization and user info
- [Gson](https://github.com/google/gson): Java library for sending and reading information in JSON format
- [MySQL](https://www.mysql.com/): Relational database to store data and make queries
- [Protobuf](https://developers.google.com/protocol-buffers): language independent communication
 
Machine Learning and Scraping:
- [Beautiful Soup](https://www.crummy.com/software/BeautifulSoup/bs4/doc/): Scraping
- [Nltk](https://www.nltk.org/): natural language (stopwords, word stems)
- [Numpy](https://numpy.org/doc/stable/reference/): Python array operations
- [Pandas](https://pandas.pydata.org/docs/): Dataframe in Python
- [Scikit-learn](https://scikit-learn.org/stable/): k-Means, k-NN machine learning implementations
- [Selenium](https://www.selenium.dev/documentation/en/webdriver/): Web driver for scraping
- [TensorFlow](https://www.tensorflow.org/): Universal sentence encoder
 

## Authors
- **Iris Beckham** - [GitHub](https://github.com/iris-beckham) - [LinkedIn](https://www.linkedin.com/in/iris-beckham-91282518b/) - *Frontend*
- **Tony Pan** - [GitHub](https://github.com/tonypan2000) - [LinkedIn](https://www.linkedin.com/in/tony-pan/) - *Data Acquisition and Machine Learning*
- **Elijah Rodriguez** - [GitHub](https://github.com/hajiler) - [LinkedIn](https://www.linkedin.com/in/elijahrrb/) - *Backend*

![Alt Text](readme/about.png)


## Acknowledgements
A big thank you to the following people, groups, and organizations that have helped us with this project:
- Gold Intelligence, Attribution, Google Ads for hosting us and improving Machine Learning performance.
- Google for Nonprofits (G4NP) for providing data, legal advice, and suggestions on how to use and present data to users.
