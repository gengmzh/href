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
    st = request.REQUEST.get('st', None)
    if st:
        st = fromTimestamp(st)
    et = request.REQUEST.get('et', None)
    if et:
        et = fromTimestamp(et)
    # query
    q = {}
    if st:
        q['ct'] = {'$gt': st}
    if et:
        q['ct'] = {'$lt': et} 
    coll = getCollection('post')
    posts = []
    cursor = coll.find(q, fields={'ctt':0}).sort([('ct', -1)]).limit(20)
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
    

