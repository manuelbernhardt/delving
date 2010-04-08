"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)



"""

import datetime
import os
import subprocess

from django.db import models, connection
from django.conf import settings

from utils.gen_utils import dict_2_django_choice



AUTOGEN_NAME = 'Autogenerated by Sip Manager'



class Aggregator(models.Model):
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200, blank=True)

    def __unicode__(self):
        return '[%s] %s' % (self.name_code, self.name)





# PROVT_ = Provider types
PROVT_MUSEUM = 1
PROVT_ARCHIVE = 2
PROVT_LIBRARY = 3
PROVT_AUDIO_VIS_ARCH = 4
PROVT_AGGREGATOR = 5


PROV_TYPES = {
    PROVT_MUSEUM : 'Museum',
    PROVT_ARCHIVE : 'Archive',
    PROVT_LIBRARY : 'Library',
    PROVT_AUDIO_VIS_ARCH : 'Audio Visual Archive',
    PROVT_AGGREGATOR : 'Aggregator',
    }


class ProviderManager(models.Manager):

    PROVIDER_TYPE_LOOKUP = {'L':  PROVT_LIBRARY,
                            'A':  PROVT_ARCHIVE,
                            'M':  PROVT_MUSEUM,
                            'AA': PROVT_AUDIO_VIS_ARCH,
                            'AG': PROVT_AGGREGATOR,
                            }


    def get_or_create(self, name_code, file_name, aggregator):
        cursor = connection.cursor()
        cursor.execute('SELECT id FROM %s_provider WHERE name_code="%s"' % (__name__.split('.')[-2], name_code))
        if cursor.rowcount:
            # this can so not fail - i just refuse to do errorhandling for this call
            item = self.model.objects.filter(name_code=name_code)[0]
            was_created = False
        else:
            try:
                sanitized_file_name = os.path.splitext(file_name)[0].replace('__','_')
                s = sanitized_file_name.split('_')[1].upper()
                item_type = self.PROVIDER_TYPE_LOOKUP[s]
                country_idx = 2
            except:
                item_type = 1
                country_idx= 1
            try:
                country = sanitized_file_name.split('_')[country_idx]
                if len(country) > 2:
                    raise
            except:
                country = '??'
            try:
                name = ' '.join(sanitized_file_name.split('_')[country_idx+1:])
            except:
                name = AUTOGEN_NAME
            item = self.model(aggregator=aggregator, name_code=name_code,
                              name=name, item_type=item_type, country=country)
            item.save()
            was_created = True
        return item, was_created


class Provider(models.Model):
    aggregator = models.ForeignKey(Aggregator)
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200,blank=True)
    country = models.CharField(max_length=5)
    item_type = models.IntegerField(choices=dict_2_django_choice(PROV_TYPES),
                                    default = PROVT_MUSEUM)

    objects = ProviderManager()


    def __unicode__(self):
        return '%s - [%s] %s' % (self.aggregator, self.name_code, self.name)






# DAST_ = DataSet types
DAST_ESE = 1
DAST_TYPES = {
    DAST_ESE: 'ESE',
    }


class DataSetManager(models.Manager):

    def get_or_create(self, file_name):
        cursor = connection.cursor()
        cursor.execute('SELECT id FROM %s_dataset WHERE name_code="%s"' % (
            __name__.split('.')[-2], file_name))
        if cursor.rowcount:
            # this can so not fail - i just refuse to do errorhandling for this call
            item = self.model.objects.filter(name_code=file_name)[0]
            was_created = False
        else:

            try:
                int(file_name[:5]) # we use it as string but check that it is an int...
                aggregator_nc = file_name[:2]
                provider_nc = file_name[2:5]
            except:
                aggregator_nc = 'XX'
                provider_nc = 'XXX'

            aggregator, was_created = Aggregator.objects.get_or_create(
                name_code=aggregator_nc, name=AUTOGEN_NAME)

            provider, was_created = Provider.objects.get_or_create(
                provider_nc, file_name, aggregator)

            item = self.model(provider=provider, name_code=file_name,
                              name=AUTOGEN_NAME, language='??')
            item.save()
            was_created = True
        return item, was_created


class DataSet(models.Model):
    provider = models.ForeignKey(Provider)
    name_code = models.CharField(max_length=200)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200, blank=True)
    language = models.CharField(max_length=4)
    item_type = models.IntegerField(choices=dict_2_django_choice(DAST_TYPES),
                                    default = DAST_ESE)

    objects = DataSetManager()

    def __unicode__(self):
        return '[%s] %s' % (self.name_code, self.name)





# REQS_ = Request status
REQS_PRE = 0 # unoficial state, only in dummy_ingestion
REQS_INIT = 1
REQS_IMPORTED = 2
REQS_ABORTED = 3
REQS_SIP_PROCESSING = 4
REQS_PENDING_VALIDATION_SIGNOFF = 5
REQS_PENDING_AIP_SIGNOFF = 6
REQS_CREATING_AIP = 7
REQS_AIP_COMPLETED = 8

REQS_STATES = {
    REQS_PRE: 'unprocessed',
    REQS_INIT: 'under construction',
    REQS_IMPORTED: 'import completed',
    REQS_ABORTED: 'aborted',
    REQS_SIP_PROCESSING: 'sip processing',
    REQS_PENDING_VALIDATION_SIGNOFF: 'pending validation sign off',
    REQS_PENDING_AIP_SIGNOFF: 'pending AIP sign off',
    REQS_CREATING_AIP: 'creating AIP',
    REQS_AIP_COMPLETED: 'AIP completed',

    }

class RequestManager(models.Manager):

    def get_or_create_from_file(self, full_path):
        file_name = os.path.split(full_path)[1]
        data_set, ds_created = DataSet.objects.get_or_create(file_name)
        mtime = os.path.getmtime(full_path)
        time_created = datetime.datetime.fromtimestamp(mtime)

        lst = ['SELECT id FROM %s_request' % __name__.split('.')[-2] ]
        lst.append('WHERE data_set_id=%i' % data_set.pk)
        lst.append('AND file_name="%s"' % file_name)
        lst.append('AND time_created="%s"' % time_created)
        sql = ' '.join(lst)

        cursor = connection.cursor()
        cursor.execute(sql)
        if cursor.rowcount:
            # this can so not fail - i just refuse to do errorhandling for this
            pk = cursor.fetchone()[0]
            item = self.model.objects.filter(pk=pk)[0]
            was_created = False
        else:
            cmd = 'awk -F "<record>" \'{s+=(NF-1)} END {print s}\' %s' % full_path
            try:
                rec_count = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip() or 0
            except:
                rec_count = 0
            kwargs = {'data_set': data_set,
                      'file_name': file_name,
                      'record_count': rec_count,
                      'time_created': time_created
                      }
            if not rec_count:
                kwargs['err_msg'] = 'Failed to count records'
                kwargs['status'] = REQS_ABORTED
            item = self.model(**kwargs)
            item.save()
            was_created = True
        return item, was_created



class Request(models.Model):
    data_set = models.ForeignKey(DataSet)
    status = models.IntegerField(choices=dict_2_django_choice(REQS_STATES),
                                 default = REQS_PRE)
    record_count = models.IntegerField() # no of records in request
    # we dont store path, find it by os.walk and check time_stamp
    file_name = models.CharField(max_length=200,
                                 help_text='relative filename, dont store path, system will find it with os.walk() and timestamp...')
    time_created = models.DateTimeField(editable=False)
    pid = models.IntegerField(default=0) # what process 'owns' this item
    err_msg = models.CharField(max_length=200, blank=True)

    objects = RequestManager()

    def __unicode__(self):
        return '%s - %s' % (self.data_set, self.time_created)

