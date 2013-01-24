from django.conf.urls import patterns, url

# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

# post
urlpatterns = patterns('web.api.post',
    # post
    url(r'^post', 'index', name='api-post'),
    
)
