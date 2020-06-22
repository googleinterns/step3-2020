import nltk
from nltk.corpus import stopwords
import numpy as np
import pandas as pd
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfTransformer
import string


# Reads csv as input with Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename, skipinitialspace=True)
  # appends organization name to mission statement
  for i in range(data.shape[0]):
    data['Mission'][i] += ' ' + data['Organization'][i]
  return data

def process_text(input):
  remove_puct = input
  for char in input:
    # remove all punctuations
    if char in string.punctuation:
      remove_puct = remove_puct.replace(char, ' ')
  # convert to all lowercase
  output = remove_puct.lower()
  return output

# map each word to an index
def extract_words(data):
  bag_of_words = {}
  nltk.download('stopwords')
  stop_words = set(stopwords.words('english'))
  index = 0
  for about in data['Mission']:
    about_text = process_text(about)
    # map each word to its index
    for word in about_text.split():
      if word not in bag_of_words and word not in stop_words:
        bag_of_words[word] = index
        index += 1
  return bag_of_words

# turns every mission statement into an array of word count
def generate_features(df, word_dict):
  num_orgs = df.shape[0]
  num_words = len(word_dict)
  feature_matrix = np.zeros((num_orgs, num_words))
  missions = df['Mission']
  for (j, m) in enumerate(missions):
    mission = process_text(m)
    for word in mission.split():
      if word in word_dict:
        index = word_dict[word]
        # ith column of word dict is the word in the jth mission statement
        feature_matrix[j][index] += 1
  return feature_matrix

def tf_idf(features):
  tfidf_transformer = TfidfTransformer(sublinear_tf=True)
  tf_idf = tfidf_transformer.fit_transform(features)
  return tf_idf

def main():
  filename = 'sample_data.csv'
  data = read_data(filename)
  # make a bag of words
  bag_of_words = extract_words(data)
  features = generate_features(data, bag_of_words)
  # TF/IDF adds weights to each word
  vectorized = tf_idf(features)
  # k-means
  num_clusters = 3
  clusters = KMeans(n_clusters=num_clusters).fit(vectorized)
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
