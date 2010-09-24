#! /usr/bin/python

import urllib2
import urllib
import platform

__all__=['live', 'finish', 'crash']

id_=platform.node()
apiuri='http://ni.fe.up.pt/~poncio/statusserver.php'

_livemsgA='(live {id_})'
_livemsgB='(live {id_} {delta})'
_livemsgC='(live {id_} {delta} {criticaldelta})'
_finishmsg='(finish {id_})'
_crashmsg='(crash {id_})'


# protocol summary:
# (live Machineid) - indicates that the algorithm is working.
# (live Machineid NextComm) - same as previous, but indicates the expected time
#  until the next communication. no communication for more than 'NextComm' time
#  indicates that something can be wrong. It is up to the server to take actions.
# (live Machineid NextComm NextCommCritical) - NextComm < NextCommCritical.
#  Same as previous but also indicates how much time without communications is
#  indicates a critical failure. No communication for more time than
#  NextCommCritical indicates serious problems. It is up to the server to take
#  actions.
# (crash Machineid) - indicates that the algorithm has crashed.
# (finish Machineid) - indicates that the algorithm as finished.

# NextComm and NextCommCritical must be in seconds.

def live(delta=None, criticaldelta=None):
    if delta is None:
        apimsg=_livemsgA.format(id_=id_)
    elif criticaldelta is None:
        apimsg=_livemsgB.format(id_=id_, delta=delta)
    else:
        apimsg=_livemsgC.format(id_=id_, delta=delta, criticaldelta=criticaldelta)

    return _request(apimsg)

def finish():
    return _request(_finishmsg.format(id_=id_))

def crash():
    return _request(_crashmsg.format(id_=id_))

def _request(msg):
    try:
        data=urllib.urlencode([('inform', msg)])
        response = urllib2.urlopen(apiuri,data)
        return response.read()
    except:
        import sys
        print "Unexpected error:", sys.exc_info()[0]
    return "error"

if __name__ == '__main__':
    from datetime import timedelta
    id_="a_test"
    _12min=timedelta(minutes=12)
    print live(_12min.seconds)

