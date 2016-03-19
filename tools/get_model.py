#!/usr/bin/env python
import sys
import os
import time
import tarfile

if sys.version_info >= (3,):
    import urllib.request as urllib2
    import urllib.parse as urlparse
else:
    import urllib2
    import urlparse
    import urllib

def download_file(url, desc=None, renamed_file=None):
    u = urllib2.urlopen(url)
    scheme, netloc, path, query, fragment = urlparse.urlsplit(url)
    filename = os.path.basename(path)
    if not filename:
        filename = 'downloaded.file'

    if not renamed_file is None:
        filename = renamed_file

    if desc:
        filename = os.path.join(desc, filename)

    with open(filename, 'wb') as f:
        meta = u.info()
        meta_func = meta.getheaders if hasattr(meta, 'getheaders') else meta.get_all
        meta_length = meta_func("Content-Length")
        file_size = None
        if meta_length:
            file_size = int(meta_length[0])
        print("Downloading: {0} Bytes: {1}".format(url, file_size))

        file_size_dl = 0
        block_sz = 8192
        while True:
            buffer = u.read(block_sz)
            if not buffer:
                break

            file_size_dl += len(buffer)
            f.write(buffer)

            status = "{0:16}".format(file_size_dl)
            if file_size:
                status += "   [{0:6.2f}%]".format(file_size_dl * 100 / file_size)
            status += chr(13)

    return filename

def extractTarfile(filename):
    tar = tarfile.open(filename)
    tar.extractall()
    tar.close()

if __name__== '__main__':
    download_url = 'https://www.dropbox.com/s/0i2fr9krb8wv8mp/phone_data.tar?dl=1'
    target_name = 'phone_data.tar'
    download_file(download_url, renamed_file=target_name)
    extractTarfile(target_name)
