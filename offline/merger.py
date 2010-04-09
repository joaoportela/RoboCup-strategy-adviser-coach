#! /usr/bin/env python

import os
import gzip
import subprocess
import sys

def is_compressed(file_):
    command = "/usr/bin/file -i {file_}".format(**locals())
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
    mime = process.communicate()[0];
    return "application/x-gzip;" in mime

def compress(file_):
    file_new=file_+".new"
    # compress file_ to file_new
    print "compressing ", file_,
    sys.stdout.flush()
    with open(file_, 'rb') as f_in:
        f_out = gzip.open(file_new, 'wb', compresslevel=5)
        f_out.writelines(f_in)
        f_out.close()

    print "done"
    # replace file_ with the compressed version
    os.rename(file_new,file_)

def fixfiles(dir_):
    for root, dirs, files in os.walk(dir_):
        gzfiles = filter(lambda x: x.endswith(".gz"),files)
        for file_ in gzfiles:
            file_=os.path.join(root,file_)
            if not is_compressed(file_):
                compress(file_)

if __name__ == '__main__':
    if len(sys.argv) == 2 and os.path.isdir(sys.argv[1]):
        print "fixing files in {sys.argv[1]}".format(**locals())
        fixfiles(sys.argv[1])
    else:
        print "running doctest"
        import doctest
        doctest.testmod()

