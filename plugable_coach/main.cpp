#include <iostream>
#include <string>
#include <boost/foreach.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/bind.hpp>

#include "assistantcoach.hpp"
#include "dieroll.hpp"

#define foreach BOOST_FOREACH
using namespace std;

/**
 * main.cpp:
 * this file is where I functionally test (by visual inspection) the Assistant
 * coach class.
 */

/*
 * function where message receiving is reported.
 */
int counter = 0;
void report_func(string instruction) 
{
    cout << "rcv(" << counter++ << ")"<< instruction << endl;
}

// struct report_struct
// {
//     void call_report_func(string instruction)
//     {
//         instruction(instruction);
//     }
// };

int main( int /*argc*/, const char* /*argv*/[] )
{
    int counter = 0;
    int r;
    // report_struct rs;

    AssistantCoach acoach(&report_func);

    for(int i =0; i < 110; ++i) {
        string message=boost::lexical_cast<string>( i );
        message="(time " + message + ")";
        acoach.inform(message);
        cout << "snd(" << counter++ << ")"<< message << endl;
        r = rolldie(0,100);
        // boost::this_thread::sleep(boost::posix_time::milliseconds(r));
    }

    return 0;
}

