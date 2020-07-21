import json
import knn_pb2

def read_proto(filename):
  with open(filename, 'rb') as input:
    orgs = knn_pb2.Organizations()
    orgs.ParseFromString(input.read())

  for org in orgs.orgs:
    print(org.id, ': ')
    for n in org.neighbors:
      print(n.id)
    print()


def read_json(filename):
  with open(filename, 'r') as input:
    data = json.loads(input.read()) 

  for org in data['orgs']:
    print(org['id'], ': ')
    for n in org['neighbors']:
      print(n['id'])
    print()

# read_proto('../data/neighbors.txt')
read_json('../data/neighbors.json')
