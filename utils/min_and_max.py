#! /usr/bin/env python

import itertools
import os
import math

def usage():
    import sys
    return "usage: python {0} <file_to_minmax>+"

def valid_arguments(args):
    if len(args) < 1:
        return False
    for arg in args:
        if not os.path.isfile(arg):
            return False
    return True

def average(list_):
    return math.fsum(list_)/len(list_)

def median(numeric_values):
  s_values = sorted(numeric_values)
  if len(s_values) % 2 == 1: # odd
    return s_values[(len(s_values)+1)/2-1]
  else: # even
    lower = s_values[len(s_values)/2-1]
    upper = s_values[len(s_values)/2]
    return (float(lower + upper)) / 2

STRATEGY_DATA = {
        "formation":
        [
            1,  # 433OPEN
            2,  # 442OPEN
            3,  # 443OPEN11Players
            4,  # 343
            #8,  # TUDOAMONTE
            9,  # 433OPENDef
            #12  # 4213 RiOne
            ]
        ,
        "mentality":
        [
            0,
            1,
            2,
            3
            ]
        ,
        "gamepace":
        [
            0,
            1,
            2,
            3
            ]
        }

def fcpd_configurations(data=STRATEGY_DATA):
    for values in itertools.product(*data.itervalues()):
        args={}
        for i, key in enumerate(data.iterkeys()):
            args[key] = values[i]
        yield args

def args_to_str(args):
        dynamic_part = []
        # hack - because at first the data was not in alphabetic order...
        # and after generating a lot of data I have to use it like this.
        PREDEFINED_ORDER=["formation", "mentality", "gamepace"]
        for name in PREDEFINED_ORDER:
            value = str(args[name])
            dynamic_part.append("{name}{value}".format(**locals()))
        for name, value in sorted(args.items()):
            if name not in PREDEFINED_ORDER: # this is part of the hack too
                value = str(value)
                dynamic_part.append("{name}{value}".format(**locals()))
        return "_".join(dynamic_part)

def fcpd_configurations_str(data=STRATEGY_DATA):
    for args in fcpd_configurations():
        args_str=args_to_str(args)
        yield args_str

def initial_configs_counter(configs=list(fcpd_configurations_str())):
    counter={}
    for conf in configs:
        counter[conf]=0
    return counter

def which_config_is(str_with_config,configs=list(fcpd_configurations_str())):
    for conf in configs:
        if conf in str_with_config:
            return conf
    else:
        assert False, "unknown configuration "+conf

def config_column(line):
    return line.split(";")[0]

def find_minmax(files, configs=list(fcpd_configurations_str())):
    for fname in files:
        with open(fname) as f:
            # initialization
            counter=initial_configs_counter(configs)

            f.readline()# ignore the first line

            # count how many times each config appears appearances 
            for line in f:
                conf=which_config_is(config_column(line))
                counter[conf]+=1

        fname_=os.path.basename(fname)
        cnt_items=counter.items()
        n_appearances=lambda x: x[1]
        # find the min and the max.
        min_=min(cnt_items,key=n_appearances)
        max_=max(cnt_items,key=n_appearances)

        only_mins=lambda x: x[1]==min_[1]
        only_max=lambda x: x[1]==max_[1]
        min_count=len(filter(only_mins,cnt_items))
        max_count=len(filter(only_max,cnt_items))

        nozeros=lambda x: x[1] != 0
        value_only=lambda x:x[1]
        game_values_no_zeros=map(value_only,filter(nozeros,cnt_items))
        median_=median(game_values_no_zeros)
        print "{fname_} {min_}({min_count}) \
{max_}({max_count}) \
{median_}".format(**locals())



if __name__ == '__main__':
    import sys
    if not valid_arguments(sys.argv[1:]):
        print usage()
        sys.exit(0)

    find_minmax(sys.argv[1:])

