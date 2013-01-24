'''
Created on 2013-1-24

@author: mzhgeng
'''
from pymongo import MongoClient

def getMongo():
    from settings import DATABASES
    uri = DATABASES['mongo']['uri']
    return MongoClient(uri)

def getDatabase():
    mongo = getMongo()
    return mongo['href']

def getCollection(name):
    return getDatabase()[name]

