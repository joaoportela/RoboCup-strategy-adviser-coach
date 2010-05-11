#include "assistantcoach.hpp"

#include <iostream>
#include <boost/foreach.hpp>
#include <boost/assign/list_of.hpp> 
#include <boost/lexical_cast.hpp>
#define foreach BOOST_FOREACH

using namespace std;

// initialization of static constants.
const string AssistantCoach::exec = bp::find_executable_in_path("java");
const string AssistantCoach::classpath="soccerscope.jar:java-xmlbuilder-0.3.jar:sexpr.jar";

vector<string> buildargs(int listen_port) {
    return (boost::assign::list_of(string("java"))
            (string("soccerscope.SoccerScope")) (string("--udp"))
            (boost::lexical_cast<string>(listen_port)));
}

// TODO - ver funcao "to_adapter" (faz parte do boost::assing::list_of)

AssistantCoach::AssistantCoach(int listen_port):
    listen_port(listen_port),
    args(buildargs(listen_port))
{
    bp::context ctx; 
    ctx.environment = bp::self::get_environment(); 
    ctx.environment["CLASSPATH"] = AssistantCoach::classpath;

    // ctx.work_directory = "";
    // ctx.stdout_behavior = bp::silence_stream(); // seems like close_stream() works too.

    cout << "args:" << endl;
    foreach(string s, this->args) {
        cout << s << endl;
    }

    // spawn child process
    bp::child child = bp::launch(AssistantCoach::exec, this->args, ctx);

    // listen on udp socket for the message "(start)"
    // store the udp packet port on the child_port
}

AssistantCoach::~AssistantCoach(){
    // send udp message "(end)"

    // wait for the child to terminate.

    cout << "waiting" << endl;
    // bp::status s = child->wait();
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
