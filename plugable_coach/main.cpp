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

struct report_struct
{
    int counter;
    report_struct():counter(0) {}
    void operator()(string instruction)
    {
        cout << "rcv(" << counter++ << ")"<< instruction << endl;
    }
};

boost::scoped_ptr<AssistantCoach> acoach_ptr;

int main( int /*argc*/, const char* /*argv*/[] )
{
    int counter = 0;
    int die_result;

    // instanciate the assistant coach
    BOOST_ASSERT(!acoach_ptr);
    acoach_ptr.reset(new AssistantCoach(report_struct(), "svm", 1000, "teste/"));

    for(int i =0; i < 110; ++i) 
    {
        string message=boost::lexical_cast<string>( i );
        message="(time " + message + ")";
        acoach_ptr->inform(message);
        cout << "snd(" << counter++ << ")"<< message << endl;
        die_result = rolldie(0,100);
        boost::this_thread::sleep(boost::posix_time::milliseconds(die_result));
    }

    return 0;
}

