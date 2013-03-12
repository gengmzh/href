'''
Created on 2013-3-12

@author: mzhgeng
'''

class FeedbackService(object):
    '''
    classdocs
    '''


    def __init__(self ):
        '''
        Constructor
        '''
        from models.mongo import getCollection
        self.feedback = getCollection('feedback')

    def save(self, feed, rating=None, email=None):
        doc = {'feed': feed}
        if rating:
            doc['rating'] = rating
        if email:
            doc['email'] = email
        return self.feedback.save(doc)
