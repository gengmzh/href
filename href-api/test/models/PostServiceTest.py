'''
Created on 2013-2-25

@author: mzhgeng
'''
import unittest


class PostServiceTest(unittest.TestCase):

    def setUp(self):
        from models.PostService import PostService
        self.postService = PostService()

    def test_find(self):
        pl = self.postService.findList(0, limit=1);
        self.assertIsNotNone(pl)
        for p in pl:
            print p


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'PostServiceTest.test_find']
    unittest.main()