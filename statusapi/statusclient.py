#! /usr/bin/python

import urllib2
import urllib
import platform 

__all__=['live', 'finish']

id=platform.node()
apiuri='http://ni.fe.up.pt/~poncio/statusserver.php'

_livemsgA='(live {id})'
_livemsgB='(live {id} {delta})'
_livemsgC='(live {id} {delta} {criticaldelta})'
_finishmsg='(finish {id})'


# protocol summary:
# (live Machineid) - indicates that the algorithm is working.
# (live Machineid NextComm) - same as previous, but indicates the expected time
# until the next communication. no communication for more than 'NextComm' time
# raises a yellow flag -- the actions to take are up to the server.
# (live Machineid NextComm NextCommCritical) - NextComm < NextCommCritical. same as previous but also
# indicates the critical no communication time. no communication for more time than NextCommCritical means
# serious problems. it is up to the server to take actions. -- the actions to take are defined by the server.
# (finish Machineid) - indicates that the algorithm as finished

# NextComm and NextCommCritical must be in seconds.

def live(delta=None, criticaldelta=None):
    if delta is None:
        apimsg=_livemsgA.format(id=id)
    elif criticaldelta is None:
        apimsg=_livemsgB.format(id=id, delta=delta)
    else:
        apimsg=_livemsgC.format(id=id, delta=delta, criticaldelta=criticaldelta)

    return _request(apimsg)

def finish():
    return _request(_finishmsg.format(id=id))

def _request(msg):
    data=urllib.urlencode([('inform', msg)])
    response = urllib2.urlopen(apiuri,data)
    return response.read()

if __name__ == '__main__':
    from datetime import timedelta
    _12min=timedelta(minutes=12)
    print live(_12min.seconds)

