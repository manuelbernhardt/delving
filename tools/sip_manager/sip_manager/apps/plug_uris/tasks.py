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


 Two alternative image generation methods are available
   generate_images_pil() - 20% faster but more limited in image formats
   generate_images_magic() - richer support for image formats, slightly better image quality

 Selectable with the global boolean USE_IMAGE_MAGIC



 Url structure for generated thumbnails, can be one of

 1 = - old style (pre 0.6)

        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.FULL_DOC.jpg
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.BRIEF_DOC.jpg

 2 = from now on
        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as:
        original/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51
        FULL_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg
        BRIEF_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg

        only FULL_DOC & BRIEF_DOC is sent to production

Old img generation
FULL_DOC
mogrify -path /dest_dir
    -format jpg
    -define jpeg:size=260x200
    -thumbnail 200x [one orginals sub dir]/*.original


BRIEF_DOC
mogrify -path BRIEF_DOC/subdir1/subdir2
    -format jpg
    -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg
"""

import datetime
import hashlib
import subprocess
import os
import sys
import time
import urllib2
import urlparse
#from xml.dom.minidom import parseString

try:
    from PIL import Image
except ImportError:
    try:
        import Image
    except:
        print '*** No module named PIL - needed by %s' % __file__
        print '\tsuggested solution: sudo easy_install pil'
        sys.exit(1)


# sudo ln -s /opt/local/lib/libMagickWand.dylib /opt/local/lib/libWand.dylib
#from pythonmagickwand.image import Image

from django.db import connection, transaction
from django.conf import settings

from apps.base_item import models as base_item
from apps.process_monitor.sipproc import SipProcess

import models

FULLDOC_SIZE = (200, 10000)
BRIEFDOC_SIZE = (10000, 110)


SIP_OBJ_FILES = settings.SIP_OBJ_FILES

USE_IMAGE_MAGIC = False



class UriPepareStorageDirs(SipProcess):
    SINGLE_RUN = True
    INIT_PLUGIN = True
    PLUGIN_TAXES_DISK_IO = True

    def run_it(self):
        for s in ('orginal','FULL_DOC', 'BRIEF_DOC'):
            test_dir = os.path.join(SIP_OBJ_FILES, s,'12','32')
            if not os.path.exists(test_dir):
                self.task_starting('Creating dirs for %s' % s, 256)
                self.pre_generate_uri_trees(s)
        return True

    def pre_generate_uri_trees(self, prefix):
        hex_str = '0123456789ABCDEF'
        base_dir = os.path.join(SIP_OBJ_FILES, prefix)
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)
        for f1 in hex_str:
            for f2 in hex_str:
                first_dir = os.path.join(base_dir, '%s%s' % (f1, f2))
                os.mkdir(first_dir)
                for s1 in hex_str:
                    for s2 in hex_str:
                        second_dir = os.path.join(first_dir, '%s%s' % (s1, s2))
                        os.mkdir(second_dir)



class UriCreateNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'Create new uri records'

    def run_it(self):
        cursor = connection.cursor()

        # SQL logic:
        #   Finding MdRecords with no matching Uri items, since all MdRecords
        #   contains uri this indicates that this item is not processed
        cursor.execute('SELECT DISTINCT m.id FROM base_item_mdrecord m LEFT JOIN plug_uris_uri u ON m.id = u.mdr_id WHERE u.mdr_id IS NULL')
        self.task_starting('Creating new uri records', cursor.rowcount)
        t0 = time.time()
        record_count = 0
        while True:
            result = cursor.fetchone()
            if not result:
                break
            record_count += 1
            mdr_id = result[0]
            mdrs = base_item.MdRecord.objects.filter(pk=mdr_id,pid=0)
            if not mdrs:
                # we know it exists, so must be busy, leave it for later processing
                continue
            mdr = mdrs[0]
            if mdr.status not in (base_item.MDRS_CREATED,
                                  base_item.MDRS_IDLE,
                                  base_item.MDRS_PROCESSING):
                continue # never touch bad / completed items
            self.handle_md_record(mdr)
            if t0 + self.TASK_PROGRESS_TIME < time.time():
                self.task_progress(record_count)
                t0 = time.time()
        return True

    def handle_md_record(self, mdr):
        """For the moment cheat a bit and just get the europeana:object item
        we need to build the new cache asap!
        """
        #dom = parseString(mdr.source_data)
        parts = mdr.source_data.split('<europeana:object>')
        if len(parts) == 1:
            return False # no obj found - shouldnt happen
        img_url = parts[1].split('<')[0]
        srvr_name = urlparse.urlsplit(img_url).netloc.lower()
        uri_sources = models.UriSource.objects.filter(name_or_ip=srvr_name)
        if uri_sources:
            uri_source = uri_sources[0]
        else:
            uri_source = models.UriSource(name_or_ip=srvr_name)
            uri_source.save()
        uri = models.Uri(mdr=mdr,item_type=models.URIT_OBJECT,
                         url=img_url,uri_source=uri_source)
        uri.save()
        return True


class UriProcessNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'process new uri records'
    PLUGIN_TAXES_NET_IO = True

    def run_it(self):
        uris =  models.Uri.objects.filter(status=models.URIS_CREATED,
                                          pid=0).order_by('-uri_source')
        self.task_starting('Process new Uri records', len(uris))
        t0 = time.time()
        record_count = 0
        for uri in uris:
            record_count += 1

            self.uri = self.grab_item(models.Uri, uri.pk, 'Checking url')
            if not self.uri:
                continue # Failed to take control of it

            self.handle_uri()

            self.release_item(models.Uri, self.uri.pk)

            if t0 + self.TASK_PROGRESS_TIME < time.time():
                self.task_progress(record_count)
                t0 = time.time()
        return True


    def handle_uri(self):
        url_itm = self.verify()
        if not url_itm:
            return False

        if self.uri.item_type == models.URIT_OBJECT:
            # we only download objects
            if not self.save_object(url_itm):
                return False

        self.uri_state(models.URIS_COMPLETED)
        return True


    def verify(self):
        "Check if url is responding and giving a 200 result."
        self.uri.time_lastcheck = datetime.datetime.now()
        try:
            itm = urllib2.urlopen(self.uri.url)#,timeout=URL_TIMEOUT)
        except urllib2.HTTPError, e:
            return self.set_urierr(models.URIE_HTTP_ERROR, 'http error: %i' % e.code)
        except urllib2.URLError, e:
            if str(e.reason) == 'timed out':
                code = models.URIE_TIMEOUT
                msg = ''
            else:
                code = models.URIE_URL_ERROR
                msg =  '%s: %s' % (models.URI_ERR_CODES[code], str(e.reason))
            return self.set_urierr(code, msg)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR, 'Unhandled error when checking url')

        if itm.code != 200:
            return self.set_urierr(models.URIE_HTML_ERROR, 'HTML status: %i' % itm.code)
        try:
            content_t = itm.headers['content-type']
        except:
            return self.set_urierr(models.URIE_MIMETYPE_ERROR, 'Failed to parse mime-type')

        self.uri.mime_type = content_t
        self.uri_state(models.URIS_VERIFIED)
        return itm



    def save_object(self, itm):
        if self.uri.mime_type == 'text/html':
            return self.set_urierr(models.URIE_WAS_HTML_PAGE_ERROR)
        data = itm.read()
        try:
            content_length = int(itm.headers['content-length'])
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR,
                                   'Bad header response, missing "content-length"')

        if len(data) != content_length:
            return self.set_urierr(models.URIE_WRONG_FILESIZE,
                                   'Wrong filesize, expected: %i recieved: %i' % (content_length, len(data)))

        self.uri.content_hash = self.generate_hash(data)

        base_fname = self.file_name_from_hash(self.generate_hash(self.uri.url))
        org_fname = os.path.join(SIP_OBJ_FILES, 'original', base_fname)
        #self.make_needed_dirs(org_fname)
        try:
            fp = open(org_fname, 'w')
            fp.write(data)
            fp.close()
        except:
            return self.set_urierr(models.URIE_FILE_STORAGE_FAILED,
                                   'Failed to save original')
        self.uri_state(models.URIS_ORG_SAVED)

        if USE_IMAGE_MAGIC:
            self.generate_images_magic(base_fname, org_fname)
        else:
            self.generate_images_pil(base_fname, org_fname)
        return True

    def NOT_make_needed_dirs(self, fname):
        dest_dir = os.path.split(fname)[0]
        try:
            if not os.path.exists(dest_dir):
                os.makedirs(dest_dir)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR,
                                   'Failed to create dir [%s] for storing object file' % dest_dir)
        return True


    def generate_hash(self, thing):
        hex_hash = hashlib.sha256(thing).hexdigest().upper()
        return hex_hash

    def file_name_from_hash(self, url_hash):
        fname = '%s/%s/%s' % (url_hash[:2], url_hash[2:4],url_hash)
        return fname

    def file_name_from_hash_old_style(self, url_hash):
        "Old style (<= 0.6 release) way of generating filenames from hash."
        fname = '%s/%s' % (url_hash[:3], url_hash[3:])
        return fname


    def set_urierr(self, code, msg=''):
        if code not in models.URI_ERR_CODES:
            raise SipProcessException('set_urierr called with invalid errcode')
        self.uri.err_code = code
        if msg:
            self.uri.err_msg = msg
        else:
            # give name of error as default message
            self.uri.err_msg = models.URI_ERR_CODES[code]
        self.uri.save()
        return False # propagate error


    def uri_state(self, state):
        self.uri.status = state
        self.uri.save()

    #------------------   PIL utility methods   -------------------------------
    def generate_images_pil(self, base_fname, org_fname):

        try:
            img = Image.open(org_fname)
        except IOError as inst:
            return self.set_urierr(models.URIE_UNRECOGNIZED_FORMAT,
                                   inst.args)

        fname = os.path.join(SIP_OBJ_FILES, 'FULL_DOC', '%s.jpg' % base_fname)
        img_full = self.clone_and_save_img(img, FULLDOC_SIZE, fname)
        self.uri_state(models.URIS_FULL_GENERATED)

        fname = os.path.join(SIP_OBJ_FILES, 'BRIEF_DOC', '%s.jpg' % base_fname)
        self.clone_and_save_img(img_full, BRIEFDOC_SIZE, fname)
        self.uri_state(models.URIS_BRIEF_GENERATED)
        return True


    def clone_and_save_img(self, org_img, dest_size, fname):
        img2 = org_img.resize(self.resize_proportional(org_img.size, dest_size))
        #self.make_needed_dirs(fname)
        img2.save(fname)
        return img2

    def resize_proportional(self, org_size, max_size):
        org_x, org_y = org_size
        max_x,max_y = max_size
        x_factor = max_x / float(org_x)
        y_factor = max_y / float(org_y)
        factor = min(x_factor, y_factor)
        format_corrected = (int(org_x * factor), int(org_y * factor))
        return format_corrected

    #---------------   End of PIL utility methods   ---------------------------






    #--------------   ImageMagic utility methods   ----------------------------
    def generate_images_magic(self, base_fname, org_fname):
        """
        Old img generation
            FULL_DOC
            mogrify -path /dest_dir
                -format jpg
                -define jpeg:size=260x200
                -thumbnail 200x [one orginals sub dir]/*.original


            BRIEF_DOC
            mogrify -path BRIEF_DOC/subdir1/subdir2
                -format jpg
                -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg

        """
        fname_full = os.path.join(SIP_OBJ_FILES, 'FULL_DOC', '%s.jpg' % base_fname)
        #self.make_needed_dirs(fname_full)
        cmd = ['convert -resize 200x']
        cmd.append(org_fname)
        cmd.append(fname_full)
        err_msg = self.cmd_execute(cmd)
        if err_msg:
            return self.set_urierr(models.URIE_OBJ_CONVERTION_ERROR,
                                   'Failed to generate FULL_DOC\n%s' % err_msg)
        self.uri_state(models.URIS_FULL_GENERATED)

        fname_brief = os.path.join(SIP_OBJ_FILES, 'BRIEF_DOC', '%s.jpg' % base_fname)
        #self.make_needed_dirs(fname_brief)
        cmd = ['convert -resize x110']
        cmd.append(fname_full)
        cmd.append(fname_brief)
        err_msg = self.cmd_execute(cmd)
        if err_msg:
            return self.set_urierr(models.URIE_OBJ_CONVERTION_ERROR,
                                   'Failed to generate BRIEF_DOC\n%s' % err_msg)
        self.uri_state(models.URIS_BRIEF_GENERATED)
        return True

    def cmd_execute(self, cmd):
        if isinstance(cmd, (list, tuple)):
            cmd = ' '.join(cmd)
        err_msg = ''
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            err_msg = p.stderr.read()
        return err_msg



class UriCleanup(SipProcess):
    SHORT_DESCRIPTION = 'Remove uri records no longer having a request'

    def run_it(self):
        return True


class UriFileTreeMonitor(SipProcess):
    SHORT_DESCRIPTION = 'Walks file tree and finds orphan files'

    def run_it(self):
        return True # does nothing for the moment



# List of active plugins from this file
task_list = [UriPepareStorageDirs,
             UriCreateNewRecords,
             UriProcessNewRecords,
             #UriCleanup,
             #UriFileTreeMonitor,
             ]