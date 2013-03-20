# Create your views here.
import logging
log = logging.getLogger('href-api')

from django.http import HttpRequest, HttpResponse
from django.shortcuts import render_to_response
from datetime import datetime

import json


def index(request):    
    return render_to_response('index.html', { })

def findPostList(request):
    # args
    time = request.REQUEST.get('time', None)
    if time:
        try:
            time = datetime.fromtimestamp(long(time)/1000)
            time = time.strftime("%Y%m%d %H:%M:%S")
        except Exception, ex:
            time = None
            log.error('convert time %s failed' % time)
            log.error(ex)
    item = request.REQUEST.get('item', None)
    order = request.REQUEST.get('order', 0)
    limit = request.REQUEST.get('limit', '20')
    limit = int(limit)
    # query
    from models.PostService import PostService
    service = PostService()
    posts = service.findList(time, item, order, limit)
    # track
    __track(request, 'query', ','.join([ p['_id'] for p in posts ]))
    return __json(posts)

def __json(value):
    return HttpResponse(json.dumps(value, ensure_ascii=False), content_type="application/json; charset=UTF-8")

def findPostDetail(request, post_id):
    __track(request, 'click', post_id)
    # query
    from models.PostService import PostService
    service = PostService()
    post = service.findDetail(post_id)
    return __json(post)

def feedback(request):
    feed = request.REQUEST.get('feed', None)
    value = {}
    if feed:
        rating = request.REQUEST.get('rating', None)
        if rating:
            rating = float(rating)
        email = request.REQUEST.get('email', None)
        from models.FeedbackService import FeedbackService
        service = FeedbackService()
        service.save(feed, rating, email)
        value['code'] = 0
        value['message'] = 'ok'
    else:
        value['code'] = 1
        value['message'] = 'feed can not be empty!'
        log.warn('feed can not be empty!')
    return __json(value)

def __track(request, action=None, value=None):
    did = request.REQUEST.get('did', None)
    mdl = request.REQUEST.get('mdl', None)
    loc = request.REQUEST.get('loc', None)
    if not action:
        action = request.REQUEST.get('act', None)
    if not value:
        value = request.REQUEST.get('val', None)
    if did or mdl or loc or action or value:
        log.info('did=%s mdl=%s loc=%s act=%s val=%s' % (did, mdl, loc, action, value))
        return True
    else:
        return False

def track(request):
    value = {}
    if __track(request):
        value['code'] = 0
        value['message'] = 'ok'
    else:
        value['code'] = 1
        value['message'] = 'track can not be empty'
        log.warn('track can not be empty!')
    return __json(value)


