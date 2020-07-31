# Activism Platform - Nonprofit Finder

## Objective
The Activism Platform organizes and promotes activism by connecting users to nonprofit organizations:
  - Promote accessibility with searching for organizations feature
  - Promote impact and reach with recommending similar organizations feature
  - Promote relevancy with crowdsourcing from users feature
![Alt Text](readme/search.gif)

## Table of Contents
- [Objective](#Objective)
- [Features](#Features)
  - [Searching and Ranking](#Searching-and-Ranking)
    - [Search by Keyword](#Search-by-Keyword)
    - [Search by Category](#Search-by-Category)
    - [Ranking](#Ranking)
    - [Pagination](#Pagination)
  - [Machine Learning](#Machine-Learning)
    - [Text Similarity](#Text-Similarity)
    - [Collaborative Filter](#Collaborative-Filter)
- [Technology Stack](#Technology-Stack)

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

#### Text Similarity
![Alt Text](readme/similarity.gif)

#### Collaborative Filter
![Alt Text](readme/recommend.png)

## Technology Stack
