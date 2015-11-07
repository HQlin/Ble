# -*- coding: utf-8 -*-
from hashlib import md5
from django.shortcuts import render_to_response
from django.utils import timezone
from django.http import HttpResponseRedirect
from models import User
from smartlock_code import *
from api import _check_method, _generate_psw


def login(request):
	error = []

	try:
		del request.session['user_info']
		_check_method(request)
	except Not_post_method:
		error.append('提交方式错误')
		return render_to_response('account/login.html', {'loginwarning': error})
	except KeyError:
		pass
	finally:
		account = request.POST.get('account')
		psw 	= request.POST.get('psw')
		if not account or not psw:
			error.append('输入正确的用户名以及密码')
			return render_to_response('account/login.html', {'loginwarning': error})
		try:
			user_info = User.objects.get(account=account, psw=_generate_psw(psw))
			request.session['user_info'] = {
				'account'	: user_info.account,
				'nick_name' : user_info.nick_name,
				'ID' 		: user_info.id,
				'ID_card' 	: user_info.ID_card,
				'phone'		: user_info.phone,
				'regdate' 	: str(timezone.make_naive(user_info.regdate)),
				'email' 	: user_info.email,
				'sex'		: user_info.sex
			}
			return HttpResponseRedirect('/dashboard/index/')
		except User.DoesNotExist:
			error.append('用户名或密码错误')
		except Exception, e:
			print(e)
			error.append('无法登陆')

		return render_to_response('account/login.html', {'loginwarning': error})

def signin(request):
	error = []
	try:
		_check_method(request)
	except Not_post_method:
		error.append('请使用POST方式提交')
		return render_to_response('account/login.html', {'signwarning': error})

	account = request.POST.get('account')
	psw 	= request.POST.get('psw')
	if not account or not psw:
		error.append('用户名或密码没有填写')
		return render_to_response('account/login.html', {'signwarning': error})
	try:
		user_info = User.objects.create(account=account, psw=_generate_psw(psw))
		request.session['user_info'] = {
			'account'	: user_info.account,
			'nick_name' : user_info.nick_name,
			'ID' 		: user_info.id,
			'ID_card' 	: user_info.ID_card,
			'phone'		: user_info.phone,
			'regdate' 	: str(timezone.make_naive(user_info.regdate)),
			'email' 	: user_info.email,
		}
		# return HttpResponseRedirect('/dashboard/index/')
		error.append('注册成功！请登陆')
		return render_to_response('account/login.html', {'loginwarning': error})
	except Exception, e:
		print(e)
		error.append('此用户无法创建')

	return render_to_response('account/login.html', {'signwarning': error})

def reset_password(request):
	error = []
	email = request.POST.get('email')
	if not email:
		error.append('请输入正确的邮箱')
		return render_to_response('account/reset_password.html', {'resetwarning': error})
	user = User.objects.get(email = email)
	if not user:
		error.append('邮箱不存在')
	return render_to_response('account/reset_password.html', {'resetwarning': error})

def reset_confirm(request):
	pass
