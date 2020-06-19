from google.protobuf.json_format import MessageToJson
import json
import knn_pb2
import nltk
from nltk.corpus import stopwords
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.neighbors import NearestNeighbors
import string

# Reads csv as input and returns a Pandas DataFrame
def read_data(filename):
  data = pd.read_csv(filename, skipinitialspace=True)
  # TODO: does org name improve or hurt performance
  # appends organization name to mission statement
  for i in range(data.shape[0]):
    data['Mission'][i] += ' ' + data['Organization'][i]
  return data

# remove stopwords and punctuation, convert to lowercase
def process_text(input):
  # TODO: should numbers be removed too?
  # TODO: some '-' that are by itself are not removed
  nltk.download('stopwords')
  stop_words = set(stopwords.words('english'))
  sentences = [word.lower() for word in input['Mission']]
  punct_removed = []
  for sentence in sentences:
    punct_removed.append(''.join([s for s in sentence if s not in string.punctuation]))
  output = []
  for sentence in punct_removed:
    words = []
    for word in sentence.split():
      if word not in stop_words:
        words.append(word)
    output.append(' '.join(words))
  return output

def extract_words(text):
  # TODO: is ngram useful for this model?
  vectorizer = CountVectorizer(ngram_range=(1, 2))
  features = vectorizer.fit_transform(text).toarray()
  return features

def tf_idf(word_dict):
  tfidf_transformer = TfidfTransformer(sublinear_tf=True)
  tf_idf = tfidf_transformer.fit_transform(word_dict)
  return tf_idf.toarray()

def knn(features):
  # TODO: check the radius
  classifier = NearestNeighbors(n_neighbors=4, radius=0.4)
  classifier.fit(features)
  return classifier

def main():
  filename = 'sample_data.csv'
  df = read_data(filename)
  text = process_text(df)
  # make a bag of words
  bag_of_words = extract_words(text)
  # TF/IDF adds weights to each word
  vectorized = tf_idf(bag_of_words)
  # K-Nearest Neighbors (k = 4)
  model = knn(vectorized)
  prediction = model.kneighbors(vectorized, 4, return_distance=False)
  orgs = knn_pb2.Organizations()
  for i in range(df.shape[0]):
    print(df['Organization'][i], ': ', df['Organization'][prediction[i][1]], ', ',
        df['Organization'][prediction[i][2]], ', ', df['Organization'][prediction[i][3]])
    org = orgs.orgs.add()
    org.name = df['Organization'][i]
    neighbor1 = org.neighbors.add()
    neighbor1.name = df['Organization'][prediction[i][1]]
    neighbor2 = org.neighbors.add()
    neighbor2.name = df['Organization'][prediction[i][2]]
    neighbor3 = org.neighbors.add()
    neighbor3.name = df['Organization'][prediction[i][3]]
  with open('neighbors.txt', 'wb') as out_file:
    out_file.write(orgs.SerializeToString())
  with open("neighbors.json", 'w') as json_file:
    json_file.write(MessageToJson(orgs))
  

if __name__ == '__main__':
  main()
