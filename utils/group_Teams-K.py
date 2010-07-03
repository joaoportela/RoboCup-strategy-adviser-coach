#! /usr/bin/env python

import collections
import os

TeamK=collections.namedtuple('TeamK','team k')

def usage():
    return "usage: python {0} <input_file> <output_folder>".format(sys.argv[0])

def valid_arguments(args):
    if len(args) != 2:
        return False
    if not os.path.isfile(args[0]):
        return False
    if not os.path.isdir(args[1]):
        return False

    return True

def ensure_default(data, team, k):
    if team not in data:
        data[team]={}
    if k not in data[team]:
        data[team][k]=[]

def team_k(line):
    """
    returns a pair with the team name and the respective k value.

    >>> team_k('something.xml;FCPortugalD;32;9;6;0;0;12;27;25;319;29;59;92;61;8;3')
    TeamK(team='FCPortugalD', k='3')
    >>> team_k('/home/joao/autorun/matches/bahia2d__vs__fcportugalD-formation9_mentality2_gamepace1/201004071557-FCPortugalD_9-vs-Bahia2D_0_convert.rcg.gz.xml;Bahia2D;13;0;1;1;0;2;15;6;49;18;262;100;117;15;2')
    TeamK(team='Bahia2D', k='2')
    """

    line_sp=line.strip().split(';')
    return TeamK(team=line_sp[1].strip("\""), k=line_sp[-1])

def process_file(infile, outfolder):
    with open(infile) as f:
        firstline=f.readline() #the first line

        data_by_teamk={}
        data_by_k={}
        for line in f:
            (team, k)=team_k(line)

            # by team-k
            ensure_default(data_by_teamk, team, k)
            data_by_teamk[team][k].append(line)

            # by k only
            if k not in data_by_k:
                data_by_k[k]=[]
            data_by_k[k].append(line)


    for team, teamdata in data_by_teamk.iteritems():
        for k, lines in teamdata.iteritems():
            outfile=os.path.join(outfolder, team+"_"+k+".csv")
            with open(outfile, 'w') as f:
                f.write(firstline)
                f.write("".join(lines))

    for k, lines in data_by_k.iteritems():
        outfile=os.path.join(outfolder, "k"+k+".csv")
        with open(outfile, 'w') as f:
            f.write(firstline)
            f.write("".join(lines))

if __name__ == '__main__':
    import doctest
    doctest.testmod()

    import sys
    if valid_arguments(sys.argv[1:]):
        process_file(sys.argv[1], sys.argv[2])
    else:
        print usage()

