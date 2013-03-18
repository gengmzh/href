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
        time = __fromTimestamp(time)
    item = request.REQUEST.get('item', None)
    order = request.REQUEST.get('order', 0)
    limit = request.REQUEST.get('limit', '20')
    limit = int(limit)
    # query
    from models.PostService import PostService
    service = PostService()
    posts = service.findList(time, item, order, limit)
    return __json(posts)

def __fromTimestamp(millis):
    if not millis:
        return None
    try:
        millis = long(millis)
    except Exception, ex:
        log.error('convert timestamp %s failed' % millis)
        log.error(ex)
        return None
    d = datetime.fromtimestamp(millis/1000)
    return d.strftime("%Y%m%d %H:%M:%S")

def __json(value):
    return HttpResponse(json.dumps(value, ensure_ascii=False), content_type="application/json; charset=UTF-8")

def findPostDetail(request, post_id):
    from models.PostService import PostService
    service = PostService()
    post = service.findDetail(post_id)
    return __json(post)

def markPost(request, post_id=None):
    if not post_id:
        post_id = request.REQUEST.get('id', None)
    value = {}
    if post_id:
        mark = request.REQUEST.get('mark', None)
        mark = False if mark and mark=='false' else True
        from models.PostService import PostService
        service = PostService()
        try:
            service.mark(post_id, mark)
            value['code'] = 0
            value['message'] = 'ok'
        except Exception, ex:
            log.error(ex)
            value['code'] = 1
            value['message'] = 'href is busy'
    else:
        value['code'] = 1
        value['message'] = 'post id can not be empty!'
        log.warn('post id can not be empty!')
    return __json(value)

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


