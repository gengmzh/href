# Create your views here.
from django.http import HttpRequest, HttpResponse
from django.shortcuts import render_to_response
from datetime import datetime
import logging
from mongo import getCollection
import json

log = logging.getLogger('href-api')

def index(request):
    # args
    st = request.REQUEST.get('time', None)
    item = request.REQUEST.get('item', None)
    order = request.REQUEST.get('order', 0)
    limit = request.REQUEST.get('limit', 20)
    # query
    q = {}
    if st:
        st = fromTimestamp(st)
        order = '$gt' if order==0 else '$lt'
        if item:
            q['$or'] = [{'ct':{order: st}}, {'ct':st, '_id':{order: item}}]
        else:
            q['ct'] = {order: st}
    posts = []
    coll = getCollection('post')
    cursor = coll.find(q, fields={'ctt':0, 'sl':0, 'pt':0}).sort([('ct', -1), ('_id', -1)]).limit(limit)
    for post in cursor:
        posts.append(post)
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
    coll = getCollection('post')
    post = coll.find_one(post_id)
    return HttpResponse(json.dumps(post, ensure_ascii=False), content_type="application/json; charset=UTF-8")
    

