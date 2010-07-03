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
        "tactic" : range(1,32+1)
        }

NUMBER_OF_MATCHES=10

def n_strategies(data=STRATEGY_DATA):
    nconfigs=reduce(lambda x,y: x*len(y), data.values(), 1)
    return nconfigs

def runs_per_team(data=STRATEGY_DATA, number_of_matches=NUMBER_OF_MATCHES):
    """predicts the number of matches missing."""
    nconfigs=n_strategies(data)
    return number_of_matches*nconfigs

def fcpd_configurations(data=STRATEGY_DATA):
    for values in itertools.product(*data.itervalues()):
        args={}
        for i, key in enumerate(data.iterkeys()):
            args[key] = values[i]
        yield args

def args_to_str(args):
        dynamic_part = []
        for name, value in sorted(args.items()):
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
        if conf+"/" in str_with_config:
            return conf
        if conf+"_" in str_with_config:
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
    outline.append(name_column(lines[0])) # column 1 (team name->team name)

    # prepare the lines by turning them into lists and removing extra chars
    # like '"'
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

def print_minmax(counter):
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
    onlyzeros=lambda x: x[1] == 0
    n_excluded_configs=len(filter(onlyzeros, cnt_items))
    print_str="\tmin occurrences is {min_[1]}, {min_count} times. "
    print_str+="max occurrences is {max_[1]}, {max_count} times."
    print_str+="\n\tAverage number of strategy repetitions (in this cluster): {average_}"
    print_str+="\n\tnumber of excluded configs: {n_excluded_configs}"
    print print_str.format(**locals())

def print_strategy_count(counter):
    for strategy,count in counter.iteritems():
        if count != 0:
            print "\t", strategy, count

def write_grouped_data(fname_out, grouped_data, file_header):
    with open(fname_out,'w') as fout:
        fout.write(file_header)
        for config, lines in grouped_data.iteritems():
            if lines:
                newline=merge(lines)
                fout.write(newline)

def main(files, configs=list(fcpd_configurations_str())):
    for fname in files:
        (counter, grouped_data, file_header)=gather_data(fname, configs)

        path, basename = os.path.split(fname)

        print basename+":"
        print_minmax(counter)
        #print_strategy_count(counter)

        outdir=os.path.join(path, "merged")
        if not os.path.isdir(outdir):
            os.mkdir(outdir)
        fname_out=os.path.join(outdir,basename)
        write_grouped_data(fname_out, grouped_data,file_header)

if __name__ == '__main__':
    import sys
    if not valid_arguments(sys.argv[1:]):
        print usage()
        sys.exit(0)

    main(sys.argv[1:])

