#include "assistantcoach.hpp"

#include <iostream>
#include <fstream>
#include <boost/foreach.hpp>
#include <boost/assign/list_of.hpp> 
#include <boost/lexical_cast.hpp>
#include <boost/asio.hpp>
#define foreach BOOST_FOREACH
#define MAX_BUFFER_SIZE (16*2048)

#ifdef CHILD_REDIRECT_TO_FILE
#include <fcntl.h>
#include <unistd.h>
#endif

using namespace std;

// initialization of static constants.
const string AssistantCoach::exec = bp::find_executable_in_path("java");
const string AssistantCoach::classpath="soccerscope.jar:java-xmlbuilder-0.3.jar:sexpr.jar";

vector<string> buildargs(int listen_port) 
{
    return (boost::assign::list_of(string("java"))
            ("soccerscope.SoccerScope") ("--udp")
            (boost::lexical_cast<string>(listen_port)));
}

AssistantCoach::AssistantCoach(int listen_port):
    listen_port(listen_port),
    args(buildargs(listen_port)),
    socket(this->io_service, udp::endpoint(udp::v4(), listen_port))
#ifdef CHILD_REDIRECT_TO_FILE
    ,childoutput(creat("child-output.log", 0644)),
    childerror(creat("child-error.log", 0644))
#endif
{
    bp::context ctx; 
    ctx.environment = bp::self::get_environment(); 
    ctx.environment["CLASSPATH"] = AssistantCoach::classpath;

#ifdef CHILD_REDIRECT_TO_FILE
    // this is not the proper way to check but i use it just for debug.
    BOOST_ASSERT(this->childoutput != -1);
    BOOST_ASSERT(this->childerror != -1);

    // redirect streams when in debug mode.
    ctx.stdout_behavior = bp::posix_redirect_stream(this->childoutput);
    ctx.stderr_behavior = bp::posix_redirect_stream(this->childerror);
#endif

    BOOST_ASSERT(!this->child);
    // spawn child process
    this->child.reset(new bp::child(bp::launch(AssistantCoach::exec, this->args, ctx)));

    // listen on udp socket for the message "(start)"
    boost::array<char, MAX_BUFFER_SIZE> recv_buf;
    boost::system::error_code error;
    size_t s = this->socket.receive_from(boost::asio::buffer(recv_buf),
            this->child_address, 0, error);

    if (error)
        throw boost::system::system_error(error);

    string recvdata(recv_buf.data(), s);
    if(recvdata != "(start)"){
        // TODO - what? exception?
    }
}

AssistantCoach::~AssistantCoach()
{
    // send udp message "(end)"
    string message("(end)");

    boost::system::error_code ignored_error;
    this->socket.send_to(boost::asio::buffer(message),
             this->child_address, 0, ignored_error);

    // wait for the child to terminate.
    cout << "waiting" << endl;
    bp::status s = this->child->wait();
    if (s.exited()) {
        cout << "exit status: " << s.exit_status() << endl;
    }

#ifdef CHILD_REDIRECT_TO_FILE
    close(this->childoutput);
    close(this->childerror);
#endif
}

void AssistantCoach::message_received(string const &message)
{
    // just echoes for now
    this->out_messages.push_back(message);

    // send message to the child.
}

list<string> AssistantCoach::instructions()
{
    list<string> ret_msgs;
    this->out_messages.swap(ret_msgs);
    return ret_msgs;
}

bool AssistantCoach::has_instructions() const 
{
    return not(this->out_messages.empty());
}
//  foreach(string message, data) {
//      cout << ch;
//  }
