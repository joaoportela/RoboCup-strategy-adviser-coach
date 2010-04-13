#! /usr/bin/env python

import os
import gzip
import subprocess
import sys
import re

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

def fix_uncompressed(dir_):
    for root, dirs, files in os.walk(dir_):
        gzfiles = filter(lambda x: x.endswith(".gz"),files)
        for file_ in gzfiles:
            file_=os.path.join(root,file_)
            if not is_compressed(file_):
                compress(file_)

def special_move(directory,source,target):
    """moves the directory from source to target assuming some special
    conditions related to the problem at hand
    """
    NUMBERS_PATTERN = re.compile(r'\d+')
    # return NUMBERS_PATTERN.match(os.path.basename(name)).group(0)
    files_to_move = os.listdir(os.path.join())

    # filter to only include files that have ids...

    # create the dir...
    # check for name clashing...
    # apply resolution techniques...
    # do the actual move...

def merge(target,sources):
    for source in sources:
        directories = os.listdir(source)
        for directory in directories:
            merge(directory, source, target)


if __name__ == '__main__':
    if len(sys.argv) > 2 and os.path.isdir(sys.argv[1]):
        target_dir=sys.argv[1]
        source_dirs=sys.argv[2:]
        if not all(map(os.path.isdir, source_dirs)):
            raise "fail"
        print "target_dir: ", target_dir
        print "source_dirs: ", source_dirs
        premerge(target_dir,source_dirs)
    else:
        print "running doctest"
        import doctest
        doctest.testmod()

