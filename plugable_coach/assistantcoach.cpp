#include "assistantcoach.hpp"

#include <iostream>
#include <fstream>
#include <boost/bind.hpp>
#include <boost/foreach.hpp>
#include <boost/assign/list_of.hpp> 
#include <boost/lexical_cast.hpp>
#include <boost/asio.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#define foreach BOOST_FOREACH

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

AssistantCoach::AssistantCoach(userhandler_t rcvfunc, int listen_port):
    receive(rcvfunc),
    //_finished(false),
    args(buildargs(listen_port)),
    socket(this->io_service, udp::endpoint(udp::v4(), listen_port))
#ifdef LOG_COMMUNICATION
    ,to_child("messages-tochild.log"),
    from_child("messages-fromchild.log")
#endif
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
    rcv_container_t recv_buf;
    boost::system::error_code error;
    size_t size = this->socket.receive_from(boost::asio::buffer(recv_buf),
            this->child_address, 0, error);

    if (error) 
    {
        cerr << "error waiting for \"(start)\"" << endl;
        throw boost::system::system_error(error);
    }

    string recvdata(recv_buf.data(), size);
#ifdef LOG_COMMUNICATION
    from_child << this->current_time() << "[thread:" << boost::this_thread::get_id()  << "]" << recvdata << endl;
#endif
    if(recvdata != "(start)"){
        cerr << "recvdata != start" << endl;
        // TODO - what? exception?
    }

    install_receive();

    // launch the thread running the io service
    async_worker = boost::thread(boost::bind(&boost::asio::io_service::run, &(this->io_service)));
}

AssistantCoach::~AssistantCoach()
{
    // send udp message "(end)"
    string message("(end)");

    boost::system::error_code ignored_error;
    this->socket.send_to(boost::asio::buffer(message),
             this->child_address, 0, ignored_error);
#ifdef LOG_COMMUNICATION
    to_child << this->current_time() << "(" << boost::this_thread::get_id()  << ")" << message << endl;
#endif

    // wait for the worker and the child to terminate.
    bp::status s = this->child->wait();
    if (s.exited()) {
        cout << "acoach-child exit status: " << s.exit_status() << endl;
    }
    async_worker.join();

#ifdef LOG_COMMUNICATION
    to_child.close();
    from_child.close();
#endif
#ifdef CHILD_REDIRECT_TO_FILE
    close(this->childoutput);
    close(this->childerror);
#endif
}

#if 0
bool AssistantCoach::isfinished() 
{
    boost::unique_lock<boost::mutex> lock(this->_finished_mutex);
    return this->_finished;
}

void AssistantCoach::finish() {
    boost::unique_lock<boost::mutex> lock(this->_finished_mutex);
    this->_finished = true;
}
#endif

void AssistantCoach::inform(string const &message)
{
    // make a copy of the message to send
    boost::shared_ptr<std::string> msg_ptr(new std::string(message)); 

    // send message to the child.
    // DISABLED!
    // this->socket.async_send_to(boost::asio::buffer(*msg_ptr), child_address, 
    //         boost::bind(&AssistantCoach::handle_send, this,
    //             msg_ptr, boost::asio::placeholders::error));
}

void AssistantCoach::install_receive()
{
    boost::shared_ptr<rcv_container_t> recv_buf_ptr(new rcv_container_t());
    this->socket.async_receive_from(
            boost::asio::buffer(boost::asio::buffer(*recv_buf_ptr)), child_address,
            boost::bind(&AssistantCoach::handle_receive, this,
                recv_buf_ptr, boost::asio::placeholders::error,
                boost::asio::placeholders::bytes_transferred));
}

void AssistantCoach::handle_send(boost::shared_ptr<std::string> message_ptr,
        const boost::system::error_code& error)
{
    if (error)
    {
        cerr << "error sending message: " << *message_ptr << endl;
        throw boost::system::system_error(error);
    }

#ifdef LOG_COMMUNICATION
    to_child << this->current_time() << "[thread:" << boost::this_thread::get_id()  << "]" << *message_ptr << endl;
#endif
}

void AssistantCoach::handle_receive(
        boost::shared_ptr<rcv_container_t> recv_buf_ptr, 
        const boost::system::error_code& error,
        std::size_t bytes_transferred)
{
    if(error) 
    {
        cerr << "error receiving message" << endl;
        throw boost::system::system_error(error);
    }

    // fetch the message
    string message(recv_buf_ptr->data(), bytes_transferred);

#ifdef LOG_COMMUNICATION
    // log the message
    from_child << this->current_time() << "[thread:" << boost::this_thread::get_id()  << "]" << message << endl;
#endif

    if(message != "(end)")
    {
        // call the user defined handler
        this->receive(message);

        // prepare to receive more messages
        install_receive();
    }
    else
    {
        cerr << "not installing handler" << endl;
    }

}

const string AssistantCoach::current_time() const
{
    // std::ostringstream msg;
    // const boost::posix_time::ptime now=
    //     boost::posix_time::second_clock::local_time();
    // boost::posix_time::time_facet*const f=
    //     new boost::posix_time::time_facet("%H-%M-%S");
    // msg.imbue(std::locale(msg.getloc(),f));
    // msg << now;
    // return msg.str();
    return boost::posix_time::to_iso_string(
           boost::posix_time::second_clock::local_time()
           );
}
