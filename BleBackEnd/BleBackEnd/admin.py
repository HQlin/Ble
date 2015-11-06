from django.contrib import admin
from BleBackEnd.models import *

admin.site.register(User)
admin.site.register(Initial_lock)
admin.site.register(Lock_with_owner)
admin.site.register(Keyring)
admin.site.register(Log)