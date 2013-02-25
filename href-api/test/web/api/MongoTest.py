'''
Created on 2013-1-24

@author: mzhgeng
'''
import unittest


class PostServiceTest(unittest.TestCase):

    def setUp(self):
        from web.api.mongo import getMongo
        self.mongo = getMongo()
        self.db = self.mongo['href']
    
    def testMongo(self):
        coll = self.db['post']
        post = coll.find_one()
        self.assertIsNotNone(post)
        print post
        print post['ttl'].encode('utf8')
        print post['ctt'].encode('utf8')

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'PostServiceTest.testMongo']
    unittest.main()