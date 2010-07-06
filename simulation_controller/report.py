#! /usr/bin/env python

import os
import sys
from mechanize import Browser

import logging
import config

__all__ = ["report"]

def dotheupload(filename, passwd):
    URL="http://ni.fe.up.pt/~poncio/files/sp_upload.php"

    br = Browser()
    r=br.open(URL,timeout=2)
    # print br.viewing_html()
    # print "'{0}'".format(r.info())
    # print "'{0}'".format(r.read())

    br.select_form(name="upload")
    br.form.add_file(open(filename), filename=os.path.basename(filename))
    br['password'] = passwd
    r=br.submit()
    logging.debug("upload response: '{0}'".format(r.read()))
    logging.info("file must be in http://ni.fe.up.pt/~poncio/files/{0}".format(
        os.path.basename(filename)))


def report(*rtypes, **kwargs):
    if "passwd" in kwargs:
        passwd=kwargs["passwd"]
    else:
        passwd=None

    logging.info("reporting results...")

    # do it all...
    if "upload" in rtypes:
        if passwd is None:
            logging.error("cannot upload without password")
        else:
            try:
                dotheupload(config.logfile, passwd)
            except:
                import sys, traceback
                print >> sys.stderr, "upload failed."
                # traceback.print_exc()

    if "sound" in rtypes:
        os.system("aplay beep.wav")
    if "eject" in rtypes:
        os.system("eject -T")

"""note:

You don't include the HTML mentioned in the exception message ('<!
Others/0/WIN; Too') in the part of the HTML that you quote, but that
snippet is enough to see what's wrong, and lets you find exactly where in
the HTML the problem lies. Comments in HTML start with '<!--' and end
with '-->'. The comment sgmllib is complaining about is missing the '--'.

You can work around bad HTML using the .set_data() method on response
objects and the .set_response() method on Browser. Call the latter before
you call any other methods that would require parsing the HTML.

r = br.response()
r.set_data(clean_html(br.get_data()))
br.set_response(r)


You must write clean_html yourself (though you may use an external tool to
do so, of course).

Alternatively, use a more robust parser, e.g.

br = mechanize.Browser(factory=mechanize.RobustFactory())


(you may also integrate another parser of your choice with mechanize, with
more effort)
"""

