#! /usr/bin/env python

mime = subprocess.Popen("/usr/bin/file -i {file}".format(**locals()), shell=True, stdout=subprocess.PIPE).communicate()[0];
