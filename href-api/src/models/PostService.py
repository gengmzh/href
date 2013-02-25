'''
Created on 2013-2-25

@author: mzhgeng
'''

class PostService(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        from models.mongo import getCollection
        self.postColl = getCollection('post')
        self.userColl = getCollection('user')
        from django.conf import settings
        self.icon_path = settings.STATIC_URL+'icon/'

    def findList(self, time, item=None, order=0, limit=20):
        q = {}
        if time:
            order = '$gt' if order==0 else '$lt'
            if item:
                q['$or'] = [{'ct':{order: time}}, {'ct':time, '_id':{order: item}}]
            else:
                q['ct'] = {order: time}
        posts = []
        cursor = self.postColl.find(q, fields={'ctt':0, 'sl':0, 'pt':0}).sort([('ct', -1), ('_id', -1)]).limit(limit)
        uids = []
        for post in cursor:
            posts.append(post)
            if 'au' in post and not post['au'] in uids:
                uids.append(post['au'])
        users = self.__findUser(uids)
        for p in posts:
            if 'au' in p and p['au'] in users:
                u = users[p['au']]
                p['uid'] = p['au']
                p['au'] = u['un']
                p['icon'] = u['icon']
        return posts

    def __findUser(self, userIds=[]):
        if not userIds:
            return {}
        users = {}
        cursor = self.userColl.find({'_id':{'$in': userIds}}, fields={'_id':1, 'uid':1, 'icon':1})
        for user in cursor:
            users[user['_id']] = {'un':user['uid'], 'icon':self.icon_path+user['icon']}
        return users

    def getById(self, post_id):
        post = self.postColl.find_one(post_id)
        if 'au' in post:
            users = self.__findUser([post['au']])
            if post['au'] in users:
                u = users[post['au']]
                post['uid'] = post['au']
                post['au'] = u['un']
                post['icon'] = u['icon']
        return post

