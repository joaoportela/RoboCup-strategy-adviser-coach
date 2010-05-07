#include "assistantcoach.h"

#include <iostream>
#include <boost/foreach.hpp>

using namespace std;

#define foreach BOOST_FOREACH

void AssistantCoach::message_received(string const &message){
    // just echoes for now
    this->out_messages.push_back(message);
}

list<string> AssistantCoach::instructions(){
    list<string> ret_msgs;
    this->out_messages.swap(ret_msgs);
    return ret_msgs;
}

bool AssistantCoach::has_instructions() const {
    return not(this->out_messages.empty());
}
//  foreach(string message, data) {
//      std::cout << ch;
//  }
