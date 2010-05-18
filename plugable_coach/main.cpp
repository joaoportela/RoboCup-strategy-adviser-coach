#include <iostream>
#include <string>
#include <boost/foreach.hpp>
#include <boost/lexical_cast.hpp>

#include "assistantcoach.hpp"

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
    cout << "(" << counter++ << ")"<< instruction << endl;
}

class AssistantCoach2 : public AssistantCoach {
    protected:
        void new_instruction(string instruction) {
            report_func(instruction);
        }
            
};

int main( int /*argc*/, const char* /*argv*/[] )
{
    AssistantCoach2 acoach;

    for(int i =0; i < 10; ++i) {
        string message=boost::lexical_cast<string>( i );
        message="(time " + message + ")";
        acoach.inform(message);
    }

    cout << (acoach.has_instructions()?"true":"false") << endl;

    foreach(string message, acoach.instructions()) {
        cout << message << endl;
    }

    return 0;
}

