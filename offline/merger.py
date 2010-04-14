#! /usr/bin/env python

import os
import gzip
import subprocess
import sys
import re
import glob
import shutil

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
def theid(name):
    """from the filename (basename) get the numbers in the beggining (the
    date)"""
    NUMBERS_PATTERN = re.compile(r'\d+')
    match = NUMBERS_PATTERN.match(os.path.basename(name))
    if match and len(match.group(0)) == 12:
        return match.group(0)
    return None

def dict_by_id(list_):
    id_dict={}
    for f in list_:
        key=theid(f)
        id_dict.setdefault(key,[]).append(f)
    return id_dict

def migrate(source,target, move=False):
    """copies/moves the directory from source to target assuming some special
    conditions related to the problem at hand
    """

    # if necessary, create the target dir.
    if not os.path.isdir(target):
        os.mkdir(target)

    files_to_move = os.listdir(source)
    # only include files that have ids
    files_to_move = filter(theid, files_to_move)
    # get all the diferent ids
    source_dict=dict_by_id(files_to_move)

    # get the files and ids that exist in the target
    files_in_target=filter(theid, os.listdir(target))
    target_ids = set(map(theid, files_in_target))

    # check for name clashing
    for id_, fnames in source_dict.iteritems():
        if id_ in target_ids:
            # apply resolution techniques...
            oldid=id_

            # find an id that does not clash
            newid=int(id_)
            while str(newid) in target_ids:
                newid+=1

            id_=str(newid)
            target_transform = lambda x: x.replace(oldid, id_, 1)
            print >> sys.stderr, "converted", oldid, "to", id_
        else:
            target_transform = lambda x: x

        if move:
            action = shutil.move
        else:
            action = shutil.copyfile

        for fname in fnames:
            src=os.path.join(source, fname)

            # the target may require transformation
            fname=target_transform(fname)
            dst=os.path.join(target,fname)

            # do the actual copy/move...
            action(src,dst)

        # the id is now on the target
        target_ids.add(id_)

def merge(target,sources, move=False):
    j=os.path.join
    for source in sources:
        print "processing source", source
        directories = os.listdir(source)
        # filter out non-directories
        directories = filter(os.path.isdir, directories)
        for directory in directories:
            print "\tprocessing directory ", directory
            migrate(j(source,directory), j(target,directory), move=move)


if __name__ == '__main__':
    if len(sys.argv) > 2 and os.path.isdir(sys.argv[1]):
        target_dir=sys.argv[1]
        source_dirs=sys.argv[2:]
        if not all(map(os.path.isdir, source_dirs)):
            raise "fail"
        print "target_dir: ", target_dir
        print "source_dirs: ", source_dirs
        merge(target_dir,source_dirs,move=True)
    else:
        print "running doctest"
        import doctest
        doctest.testmod()

