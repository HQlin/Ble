# -*- coding: utf-8 -*-
from django.db import models

class User(models.Model):

	class Meta:
		verbose_name = "User"
		verbose_name_plural = "Users"

	def __str__(self):
		return self.nick_name + ': ' + self.account

	def __unicode__(self):
		return self.nick_name + ': ' + self.account
	
	account 	= models.CharField(max_length = 20, unique = True)
	nick_name 	= models.CharField(max_length = 15, default= '用户')
	psw 		= models.CharField(max_length = 32)
	phone 		= models.CharField(max_length = 20, blank = True)
	ID_card 	= models.CharField(max_length = 50, blank = True)
	email 		= models.EmailField(max_length = 50, blank = True)
	sex 		= models.CharField(max_length = 10, default = '未知')
	regdate 	= models.DateTimeField(auto_now_add = True)
	is_delete 	= models.BooleanField(default = False)

class Initial_lock(models.Model):

	class Meta:
		verbose_name = "Initial_Lock"
		verbose_name_plural = "Initial_Locks"

	def __str__(self):
		return self.lock_seq
	
	def __unicode__(self):
		return self.lock_seq

	lock_seq 	= models.CharField(max_length = 128, unique = True)
	virtual_key = models.CharField(max_length = 128, unique = True)
	regdate 	= models.DateTimeField(auto_now_add = True)
	is_used 	= models.BooleanField(default = False)
	
class Lock_with_owner(models.Model):

	class Meta:
		verbose_name = "Lock_with_owner"
		verbose_name_plural = "Lock_with_owner"

	def __str__(self):
		return self.owner.nick_name + '-' + self.owner.account + ': ' + self.lock_name
	
	def __unicode__(self):
		return self.lock_name

	initial_lock= models.OneToOneField(Initial_lock)
	owner 		= models.ForeignKey(User)
	lock_name 	= models.CharField(max_length = 10, default = '锁')
	regdate 	= models.DateTimeField(auto_now_add = True)
	is_delete 	= models.BooleanField(default = False)

class Keyring(models.Model):

	class Meta:
		verbose_name = "Keyring"
		verbose_name_plural = "Keyrings"

	def __str__(self):
		return self.owner.nick_name + ': ' + self.owner.account
	
	def __unicode__(self):
		return self.owner.account

	owner 			= models.ForeignKey(User)
	initial_lock 	= models.ForeignKey(Initial_lock)
	validity		= models.DateTimeField()
	regdate 		= models.DateTimeField(auto_now_add = True)
	is_delete 		= models.BooleanField(default = False)

class Log(models.Model):

	class Meta:
		verbose_name = "Log"
		verbose_name_plural = "Logs"

	def __str__(self):
		return self.owner.nick_name + ': ' + self.owner.account
	
	def __unicode__(self):
		return self.owner.nick_name + ': ' + self.lock_with_owner.lock_name + ', ' + str(self.use_time)

	owner 			= models.ForeignKey(User)
	lock_with_owner = models.ForeignKey(Lock_with_owner)
	use_time 		= models.DateTimeField(auto_now_add = True)