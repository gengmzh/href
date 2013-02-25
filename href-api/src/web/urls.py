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
    url(r'^href/', include('web.api.urls')),
)

# urlpatterns += static(settings.MEDIA_URL , document_root = settings.MEDIA_ROOT )
urlpatterns += static(settings.STATIC_URL, document_root = settings.STATIC_ROOT )
