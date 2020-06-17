import nltk
from nltk.corpus import stopwords
import numpy as np
import pandas as pd
from sklearn.cluster import KMeans
import string


# Reads csv as input with Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename, skipinitialspace=True)
  return data

# map each word to an index
def extract_words(data):
  bag_of_words = {}
  nltk.download('stopwords')
  stop_words = set(stopwords.words('english'))
  index = 0
  for about in data['Mission']:
    # remove all punctuations
    for char in about:
      if char in string.punctuation:
        about = about.replace(char, ' ')
    # convert to all lowercase
    about = about.lower()
    # map each word to its index
    for word in about.split():
      if word not in bag_of_words.keys() and word not in stop_words:
        bag_of_words[word] = index
        index += 1
  return bag_of_words

# turns every mission statement into an array of word count
def generate_features(df, word_dict):
  num_orgs = df.shape[0]
  num_words = len(word_dict)
  feature_matrix = np.zeros((num_orgs, num_words))
  missions = df['Mission']
  j = 0
  for m in missions:
    # remove all punctuations
    for char in m:
      if char in string.punctuation:
        m = m.replace(char, ' ')
    # convert to all lowercase
    m = m.lower()
    for word in m.split():
      if word in word_dict.keys():
        index = word_dict[word]
        # ith column of word dict is the word in the jth review
        feature_matrix[j][index] = 1
    j += 1
  return feature_matrix

def main():
  filename = 'sample_data.csv'
  data = read_data(filename)
  # make a bag of words
  bag_of_words = extract_words(data)
  features = generate_features(data, bag_of_words)
  # TODO: add TF/IDF to weight each word
  # k-means
  num_clusters = 3
  clusters = KMeans(n_clusters=num_clusters).fit(features)
  prediction = clusters.predict(features)
  result = []
  for i in range(num_clusters):
    group = []
    for j in range(data.shape[0]):
      if prediction[j] == i:
        group.append(data['Organization'][j])
    result.append(group)
  print(result)


if __name__ == '__main__':
  main()
