'''
Created on 2013-3-12

@author: mzhgeng
'''
import os
import unittest


class FeedbackServiceTest(unittest.TestCase):


    def setUp(self):
        os.environ["DJANGO_SETTINGS_MODULE"] = "settings"
        from models.FeedbackService import FeedbackService
        self.service = FeedbackService()

    def test_save(self):
        feedId = self.service.save('fighting', 1.0, 'seddat@foxmail.com')
        self.assertIsNotNone(feedId)
        print feedId
        self.service.feedback.remove(feedId)
        

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'FeedbackServiceTest.testName']
    unittest.main()