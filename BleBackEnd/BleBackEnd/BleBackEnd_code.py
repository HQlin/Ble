# -*- coding: utf-8 -*-
SUCCESS              = 0
FAILURE              = -1
ACCOUNT_EXISTED      = 4000
ACCOUNT_OR_PSW_WRONG = 4001
SUBMIT_METHOD_WRONG  = 4002
LOCK_SEQUENCE_WRONG  = 4003
LOCK_AUTHORITY_WRONG = 4004
WITHOUT_VIRTUAL_KEY  = 4005
KEY_AUTHORITY_WRONG	 = 4006
FORMAT_ERROR         = 4007
INSTALL_LOCK_FAILURE = 4008
SERVER_INOUT_ERROR   = 5000

class Not_post_method(TypeError):
	"""用于表单提交方法的错误提示"""
	pass

class Check_account_failure(ValueError):
	"""用于检查账号存在与否"""
	pass