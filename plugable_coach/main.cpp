#include <iostream>
#include <string>
#include <boost/foreach.hpp>
#include <boost/lexical_cast.hpp>

#include "assistantcoach.hpp"
#include "dieroll.hpp"

#define foreach BOOST_FOREACH
using namespace std;

/**
 * main.cpp:
 * this file is where I functionally test the Assistant coach class.
 */

/*
 * overriding new_instruction event.
 *
 * this is done in this particular way to test calling functions that use
 * global objects.
 */
int counter = 0;
void report_func(string instruction) {
    cout << "rcv(" << counter++ << ")"<< instruction << endl;
}

class AssistantCoach2 : public AssistantCoach 
{
    protected:
        void receive(string instruction) {
            report_func(instruction);
        }

};

int main( int /*argc*/, const char* /*argv*/[] )
{
    int counter = 0;
    int r;

    AssistantCoach2 acoach;

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

