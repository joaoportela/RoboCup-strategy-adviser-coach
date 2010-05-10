#include "assistantcoach.hpp"

#include <iostream>
#include <boost/foreach.hpp>
#include <boost/process.hpp> 
#include <boost/assign/list_of.hpp> 
#define foreach BOOST_FOREACH

using namespace std;
namespace bp = ::boost::process;

string const AssistantCoach::rcg = "/home/joao/201004151233-FCPortugalD_0-vs-FCPortugalX_0_convert.rcg.gz";
string const AssistantCoach::xml = "/home/joao/201004151233-FCPortugalD_0-vs-FCPortugalX_0_convert.rcg.gz.xml";
vector<string> const AssistantCoach::args = boost::assign::list_of(string("java"))
    (string("soccerscope.SoccerScope")) (string("--batch")) (rcg) (xml); 
string const AssistantCoach::exec = bp::find_executable_in_path("java");


AssistantCoach::AssistantCoach(){

    bp::context ctx; 
    ctx.environment = bp::self::get_environment(); 
    ctx.environment["CLASSPATH"] = "soccerscope.jar:java-xmlbuilder-0.3.jar:sexpr.jar";
    //ctx.work_directory = "";

    // ctx.stdout_behavior = bp::silence_stream(); // seems like close_stream() works too.

    // spawn child process
    bp::child c = bp::launch(AssistantCoach::exec, AssistantCoach::args, ctx);
    bp::status s = c.wait();
    if (s.exited()){ 
        cout << "exit status: " << s.exit_status() << endl;
    }
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
