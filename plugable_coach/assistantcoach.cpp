#include "assistantcoach.hpp"

#include <iostream>
#include <boost/foreach.hpp>
#include <boost/process.hpp> 
#include <boost/assign/list_of.hpp> 
#include <boost/lexical_cast.hpp>
#define foreach BOOST_FOREACH

using namespace std;
namespace bp = ::boost::process;

const int AssistantCoach::listen_port = 1337;
const vector<string> AssistantCoach::args = boost::assign::list_of(string("java"))
    (string("soccerscope.SoccerScope")) (string("--udp")) (boost::lexical_cast<string>(AssistantCoach::listen_port)); 
const string AssistantCoach::exec = bp::find_executable_in_path("java");


AssistantCoach::AssistantCoach(){

    bp::context ctx; 
    ctx.environment = bp::self::get_environment(); 
    ctx.environment["CLASSPATH"] = "soccerscope.jar:java-xmlbuilder-0.3.jar:sexpr.jar";

    // ctx.work_directory = "";
    // ctx.stdout_behavior = bp::silence_stream(); // seems like close_stream() works too.

    // spawn child process
    bp::child c = bp::launch(AssistantCoach::exec, AssistantCoach::args, ctx);

    // listen on udp socket for the message "(start)"
}

AssistantCoach::~AssistantCoach(){
    // send udp message "(end)"

    // wait for the child to terminate.

    // bp::status s = c.wait();
    // if (s.exited()){ 
    //     cout << "exit status: " << s.exit_status() << endl;
    // }
}

void AssistantCoach::message_received(string const &message){
    // just echoes for now
    this->out_messages.push_back(message);

    // send message to the child.
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
