#include "assistantcoach.hpp"

#include <iostream>
#include <boost/foreach.hpp>
#include <boost/process.hpp> 
#include <boost/assign/list_of.hpp> 
#define foreach BOOST_FOREACH

using namespace std;
namespace bp = ::boost::process;

AssistantCoach::AssistantCoach() {
    // spawn child process
    string exec = bp::find_executable_in_path("hostname"); 
    vector<string> args = boost::assign::list_of("hostname"); 
    bp::context ctx; 
    ctx.environment = bp::self::get_environment(); 
    ctx.stdout_behavior = bp::inherit_stream(); 
    bp::child c = bp::launch(exec, args, ctx);
    bp::status s = c.wait();
    if (s.exited()) 
        cout << s.exit_status() << endl;
}

AssistantCoach::~AssistantCoach(){
}

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
//      cout << ch;
//  }
