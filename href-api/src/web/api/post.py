# Create your views here.
import logging
log = logging.getLogger('href-api')

from django.http import HttpRequest, HttpResponse
from django.shortcuts import render_to_response
from datetime import datetime
from models.PostService import PostService
import json

def index(request):
    # args
    time = request.REQUEST.get('time', None)
    if time:
        time = fromTimestamp(time)
    item = request.REQUEST.get('item', None)
    order = request.REQUEST.get('order', 0)
    limit = request.REQUEST.get('limit', 20)
    # query
    service = PostService()
    posts = service.findList(time, item, order, limit)
    return HttpResponse(json.dumps(posts, ensure_ascii=False), content_type="application/json; charset=UTF-8")

def fromTimestamp(millis):
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

def detail(request, post_id):
    service = PostService()
    post = service.getById(post_id)
    return HttpResponse(json.dumps(post, ensure_ascii=False), content_type="application/json; charset=UTF-8")

