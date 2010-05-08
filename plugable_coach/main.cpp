#include <iostream>
#include <string>
#include <boost/foreach.hpp>
#include <boost/lexical_cast.hpp>

#include "assistantcoach.h"

#define foreach BOOST_FOREACH
using namespace std;

int main( int argc, const char* argv[] ) {
    AssistantCoach acoach;

    for(int i =0; i < 10; ++i) {
        string message=boost::lexical_cast<string>( i );
        acoach.message_received(message);
    }

    cout << (acoach.has_instructions()?"true":"false") << endl;

    foreach(string message, acoach.instructions()) {
        cout << message << endl;
    }

    return 0;
}

