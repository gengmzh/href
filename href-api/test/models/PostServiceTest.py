'''
Created on 2013-2-25

@author: mzhgeng
'''
import os
import unittest


class PostServiceTest(unittest.TestCase):

    def setUp(self):
        os.environ["DJANGO_SETTINGS_MODULE"] = "settings"
        from models.PostService import PostService
        self.postService = PostService()

    def test_findList(self):
        pl = self.postService.findList(0, limit=1);
        self.assertIsNotNone(pl)
        for p in pl:
            print p

    def test_findContent(self):
        post = self.postService.findContent('bbefab1f');
        self.assertIsNotNone(post)
        print post;

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'PostServiceTest.test_find']
    unittest.main()