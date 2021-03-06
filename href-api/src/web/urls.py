from django.conf import settings
from django.conf.urls import patterns, include, url
from django.conf.urls.static import static
# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'pushwebsite.views.home', name='home'),
    # url(r'^pushwebsite/', include('pushwebsite.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    # url(r'^admin/', include(admin.site.urls)),
    
    # web views
    url(r'^$', 'web.views.index', name='index'),
    url(r'^index$', 'web.views.index', name='index'),
    url(r'^index.html$', 'web.views.index', name='index'),
    # href api
    url(r'^href/post/?$', 'web.views.findPostList', name='api-post-list'),
    url(r'^href/post/(?P<post_id>[A-Za-z\d]+)/?$', 'web.views.findPostDetail', name='api-post-content'),
    url(r'^href/track/?$', 'web.views.track', name='api-post-track'),
    url(r'^href/feedback/?$', 'web.views.feedback', name='api-feedback'),
    
)

# urlpatterns += static(settings.MEDIA_URL , document_root = settings.MEDIA_ROOT )
urlpatterns += static(settings.STATIC_URL, document_root = settings.STATIC_ROOT )
