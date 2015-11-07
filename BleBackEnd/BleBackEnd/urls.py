"""BleBackEnd URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.8/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Add an import:  from blog import urls as blog_urls
    2. Add a URL to urlpatterns:  url(r'^blog/', include(blog_urls))
"""
from django.conf.urls import patterns, include, url
from django.conf import settings
from django.conf.urls.static import static
from django.contrib import admin
from BleBackEnd import api, account, dashboard

# urlpatterns = [
#     url(r'^admin/', include(admin.site.urls)),
# ]

urlpatterns = patterns('',
	url(r'^admin/', include(admin.site.urls)),
	url(r'^login', account.login),
    url(r'^signin', account.signin),

    url(r'api/check_user/$', api.check_user),
    url(r'api/create_user/$', api.create_user),
    url(r'api/distribute_key/$', api.distribute_key), 
    url(r'api/destroy_key/$', api.destroy_key), 
    url(r'api/set_user_info/$', api.set_user_info), 
    url(r'api/apply_key/$', api.apply_key),
    url(r'api/install_lock/$', api.install_lock),

    url(r'dashboard/index/$', dashboard.index),
    url(r'dashboard/recent_record/$', dashboard.recent_record),
    url(r'dashboard/distribute_key/$', dashboard.distribute_key),
    url(r'dashboard/account_setting/$', dashboard.account_setting),

    url(r'test/$', api.test),

    # psw reset_password of email
    url(r'^account/reset_password/$', account.reset_password, name="reset_password"),
    # psw reset_password
    url(r'^account/reset_confirm/(?P<seq1>[0-9A-Za-z]+)-(?P<seq2>.+)/$', account.reset_confirm, name='reset_password_confirm'),

)

if settings.DEBUG and settings.STATIC_ROOT:
  urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)
