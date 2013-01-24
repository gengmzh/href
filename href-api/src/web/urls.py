from django.conf.urls import patterns, include, url

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
    url(r'^href/', include('web.api.urls')),
)
