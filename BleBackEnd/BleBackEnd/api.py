# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response
from django.http import HttpResponse
from django.utils import timezone
from django.core import serializers
from django.db import IntegrityError
from hashlib import md5
from smartlock_code import *
import time
import re

from models import User, Initial_lock, Lock_with_owner, Keyring, Log

def _generate_psw(raw_psw):
	_SALT = 'test'
	return md5(str(raw_psw) + _SALT).hexdigest()

def _check_method(request):
	if request.method != 'POST':
		raise Not_post_method('wrong submit type')

# 验证用户
def check_user(request):
	try:
		_check_method(request)

		account = request.POST.get('account')
		psw 	= request.POST.get('psw')
	
		user = User.objects.get(account = account, psw = _generate_psw(psw))
		return HttpResponse(SUCCESS)

	except Not_post_method:
		return HttpResponse(SUBMIT_METHOD_WRONG)
	except User.DoesNotExist:
		return HttpResponse(ACCOUNT_OR_PSW_WRONG)

def create_user(request):
	try:
		_check_method(request)
		account 	= request.POST.get('account')
		psw 		= request.POST.get('psw')

		account_reg = r'^(?!_)(?!.*?_$)[a-zA-Z0-9_]{5,20}$'
		psw_reg 	= r'[a-zA-Z0-9_!@#$%^&*()-=+?]{5,32}$'

		if not re.match(account_reg, account) or not re.match(psw_reg, psw):
			return HttpResponse(FORMAT_ERROR)

		User.objects.create(account = account, psw = _generate_psw(psw))
		return HttpResponse(SUCCESS)
	except Not_post_method:
		return HttpResponse(SUBMIT_METHOD_WRONG)
	except IntegrityError:
		return HttpResponse(ACCOUNT_EXISTED)
	except Exception, e:
		print(e)
		return HttpResponse(FAILURE)

def install_lock(request):
	try:
		_check_method(request)
		account 	= request.POST.get('account')
		psw 		= request.POST.get('psw')
		lock_seq 	= request.POST.get('lock_seq')
		lock_name 	= request.POST.get('lock_name')

		lock_owner = User.objects.get(account = account, psw = _generate_psw(psw))
		initial_lock = Initial_lock.objects.get(lock_seq = lock_seq, is_used = False)
		Lock_with_owner.objects.create(
			initial_lock = initial_lock,
			owner = lock_owner,
			lock_name = lock_name
		)
		initial_lock.is_used = True
		initial_lock.save()
		return HttpResponse(SUCCESS)
	except Not_post_method:
		return HttpResponse(SUBMIT_METHOD_WRONG)
	except User.DoesNotExist:
		return HttpResponse(ACCOUNT_OR_PSW_WRONG)
	except Initial_lock.DoesNotExist:
		return HttpResponse(LOCK_SEQUENCE_WRONG)
	except IntegrityError:
		return HttpResponse(INSTALL_LOCK_FAILURE)


def distribute_key(request):
	try:
		_check_method(request)
		account 	= request.POST.get('account')
		owner 		= request.POST.get('owner')
		lock_seq 	= request.POST.get('lock_seq')
		validity 	= request.POST.get('validity') or '1990-01-01 00:00:00'
		session 	= request.session
		dir(session)
		if (session.get('user_info')):
			lock_owner = User.objects.get(account = account, id = session['user_info'].get('ID'))
		else:
			psw 		= request.POST.get('psw')
			lock_owner   = User.objects.get(account = account, psw = _generate_psw(psw))
		key_owner    = User.objects.get(account = owner)
		
		initial_lock = Initial_lock.objects.get(lock_seq = lock_seq)
		Lock_with_owner.objects.get(
			initial_lock = initial_lock,
			owner = lock_owner 
		)
		validity = timezone.make_aware(
			timezone.datetime.strptime(validity, '%Y-%m-%d %H:%M:%S')
		)

		if not lock_owner == key_owner:
			try:
				keyring = Keyring.objects.get(
					owner = key_owner,
					initial_lock = initial_lock
				)
				keyring.validity = validity
				keyring.regdate = timezone.now()
				keyring.save()
			except Keyring.DoesNotExist:
				keyring = Keyring.objects.create(
					owner = key_owner,
					initial_lock = initial_lock,
					validity = validity
				)
		return HttpResponse(SUCCESS)
	except Not_post_method:
		return HttpResponse(SUBMIT_METHOD_WRONG)
	except User.DoesNotExist:
		return HttpResponse(ACCOUNT_OR_PSW_WRONG)
	except Initial_lock.DoesNotExist:
		return HttpResponse(LOCK_SEQUENCE_WRONG)
	except Lock_with_owner.DoesNotExist:
		return HttpResponse(LOCK_AUTHORITY_WRONG)
	except ValueError:
		return HttpResponse(FORMAT_ERROR)
	except Exception, e:
		print(e)
		return HttpResponse(SERVER_INOUT_ERROR)

def destroy_key(request):
	return distribute_key(request)
	
def apply_key(request):
	try:
		_check_method(request)
		account 	= request.POST.get('account')
		psw 		= request.POST.get('psw')
		lock_seq 	= request.POST.get('lock_seq')

		owner 			= User.objects.get(account = account, psw = _generate_psw(psw))
		initial_lock 	= Initial_lock.objects.get(lock_seq = lock_seq)

		try:
			Lock_with_owner.objects.get(owner = owner, initial_lock = initial_lock)
			return HttpResponse(
				'infinite time' + ',' + str(initial_lock.virtual_key)
			)
		except Exception, e:
			pass

		keyring	= Keyring.objects.get(
			owner = owner,
			initial_lock = initial_lock
		)
		if not keyring.is_delete and keyring.validity > timezone.now():
			Log.objects.create(
				owner = owner,
				lock_with_owner = Lock_with_owner.objects.get(initial_lock = initial_lock)
			)
			return HttpResponse(
				str(timezone.make_naive(keyring.validity)) +
				',' + str(initial_lock.virtual_key)
			)
		else:
			return HttpResponse(KEY_AUTHORITY_WRONG)
	except Not_post_method:
		return HttpResponse(SUBMIT_METHOD_WRONG)
	except User.DoesNotExist:
		return HttpResponse(ACCOUNT_OR_PSW_WRONG)
	except Initial_lock.DoesNotExist:
		return HttpResponse(LOCK_SEQUENCE_WRONG)
	except Keyring.DoesNotExist:
		return HttpResponse(WITHOUT_VIRTUAL_KEY)
	except Exception, e:
		print(e)
		return HttpResponse(SERVER_INOUT_ERROR)

def set_user_info(request):
	try:
		_check_method(request)
		user_info 	= request.session['user_info']
		nick_name 	= request.POST.get('nick_name')
		phone		= request.POST.get('phone')
		email		= request.POST.get('email')
		ID_card 	= request.POST.get('ID_card')
		sex 		= request.POST.get('sex')

		if (user_info):
			user = User.objects.get(id = user_info.get('ID'))
		else:
			account 	= request.POST.get('account')
			psw = request.POST.get('psw')
			user= User.objects.get(account = account, psw = _generate_psw(psw))
		if nick_name:
			user.nick_name = nick_name
		if phone:
			user.phone = phone
		if email:
			user.email = email
		if ID_card:
			user.ID_card = ID_card
		if sex:
			user.sex = sex
		user.save()
	except Exception, e:
		print(e)
		return HttpResponse(FAILURE)
	return HttpResponse(SUCCESS)

def test(request):
    return render_to_response('test.html')