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

def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

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
    for args in fcpd_configurations(data):
        args_str=args_to_str(args)
        yield args_str

def initial_configs_counter(configs=list(fcpd_configurations_str())):
    counter={}
    for conf in configs:
        counter[conf]=0
    return counter

def initial_configs_groupeddata(configs=list(fcpd_configurations_str())):
    data={}
    for conf in configs:
        data[conf]=[]
    return data

def which_config_is(str_with_config,configs=list(fcpd_configurations_str())):
    for conf in configs:
        if conf in str_with_config:
            return conf
    else:
        assert False, "unknown configuration "+str_with_config

def config_column(line):
    return line.split(";")[0]

def name_column(line):
    return line.split(";")[1]

def merge(lines):
    outline=[]
    config=which_config_is(config_column(lines[0]))
    outline.append(config) # column 0 (xmlpath->config)
    outline.append(name_column(lines[0])) # column 1 (team name -> team name)

    # prepare the lines
    for i, line in enumerate(lines):
        newline=[]
        for col in line.split(";"):
            newline.append(col.strip("\""))
        lines[i] = newline

    number_of_lines=len(lines)
    number_of_columns=len(lines[0])

    for index in range(2,number_of_columns):
        acumulator=[]
        for line in lines:
            acumulator.append(float(line[index]))
        outline.append(average(acumulator))

    # transform outline back to strings
    outline_str=";".join([str(x) for x in outline])
    if not outline_str.endswith("\n"):
        outline_str+="\n"

    return outline_str

def gather_data(fname, configs):
    with open(fname) as f:
        # initialization
        counter=initial_configs_counter(configs)
        grouped_data=initial_configs_groupeddata(configs)

        file_header=f.readline()# the first line is the header

        # count how many times each config appears appearances 
        for line in f:
            conf=which_config_is(config_column(line))
            grouped_data[conf].append(line)
            counter[conf]+=1

    return counter, grouped_data, file_header

def print_minmax(fname_,counter):
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
        average_=average(game_values_no_zeros)
        print_str="{fname_} {min_[1]}({min_count}) "
        print_str+="{max_[1]}({max_count}) "
        print_str+="{average_}"
        print print_str.format(**locals())

def write_grouped_data(fname_out, grouped_data, file_header):
        with open(fname_out,'w') as fout:
            fout.write(file_header)
            for config, lines in grouped_data.iteritems():
                if lines:
                    newline=merge(lines)
                    fout.write(newline)

def find_minmax(files, configs=list(fcpd_configurations_str())):
    for fname in files:
        (counter, grouped_data, file_header)=gather_data(fname, configs)

        path, basename = os.path.split(fname)
        print_minmax(basename,counter)

        fname_out=fname+".merged"
        write_grouped_data(fname_out, grouped_data,file_header)

if __name__ == '__main__':
    import sys
    if not valid_arguments(sys.argv[1:]):
        print usage()
        sys.exit(0)

    find_minmax(sys.argv[1:])

