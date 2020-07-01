from absl import logging
import nltk
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
import numpy as np
import pandas as pd
import re
from sklearn.neighbors import NearestNeighbors
import string
import tensorflow as tf
import tensorflow_hub as hub


def load_module():
  module_url = "https://tfhub.dev/google/universal-sentence-encoder/4" 
  model = hub.load(module_url)
  # Reduce logging output.
  logging.set_verbosity(logging.ERROR)
  print ("module %s loaded" % module_url)
  return model

def embed(input, model):
  return model(input)

# Reads tsv as input and returns a Pandas DataFrame
def read_data(filename):
  df = pd.read_csv(filename, skipinitialspace=True, sep='\t')
  # appends organization name to about information
  df.about += ' ' + df.name
  return df

# remove stopwords and punctuation, convert to lowercase
def process_text(input):
  # to lower case
  sentences = [word.lower() for word in input['about']]
  # remove punctuation
  punct_removed = []
  for sentence in sentences:
    punct_removed.append(''.join([s for s in sentence if s not in string.punctuation]))
  # Remove numbers
  num_removed = []
  for sentence in punct_removed:
    num_removed.append(re.sub(r'\d+', '', sentence))
  # remove stopwords and lemmatize
  nltk.download('stopwords')
  nltk.download('wordnet')
  stop_words = set(stopwords.words('english'))
  lemmatizer = WordNetLemmatizer()
  output = []
  for sentence in num_removed:
    words = []
    for word in sentence.split():
      if word not in stop_words:
        words.append(lemmatizer.lemmatize(word))
    output.append(' '.join(words))
  return output

def knn(features):
  # TODO: check the radius
  classifier = NearestNeighbors(n_neighbors=5, radius=0.4)
  classifier.fit(features)
  return classifier

def main():
  filename = 'processed_irs990_0_100.csv'
  df = read_data(filename)
  text = process_text(df)  
  module = load_module()
  embeddings = embed(text, module)

  # K-Nearest Neighbors (k = 4)
  model = knn(embeddings)
  prediction = model.kneighbors(embeddings, 5, return_distance=False)
  orgs = Organizations()
  for i in range(df.shape[0]):
    print(df['name'][i], ': ', df['name'][prediction[i][1]], ', ',
        df['name'][prediction[i][2]], ', ', df['name'][prediction[i][3]], 
        ', ', df['name'][prediction[i][4]])
    org = orgs.orgs.add()
    org.id = df['id'][i]
    neighbor1 = org.neighbors.add()
    neighbor1.id = df['id'][prediction[i][1]]
    neighbor2 = org.neighbors.add()
    neighbor2.id = df['id'][prediction[i][2]]
    neighbor3 = org.neighbors.add()
    neighbor3.id = df['id'][prediction[i][3]]
    neighbor4 = org.neighbors.add()
    neighbor4.id = df['id'][prediction[i][4]]
    with open(path + '0_100_neighbors.txt', 'wb') as out_file:
      out_file.write(orgs.SerializeToString())

if __name__ == '__main__':
  main()
  