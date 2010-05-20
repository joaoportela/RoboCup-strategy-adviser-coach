#ifndef ASSISTANTCOACH
#define ASSISTANTCOACH

#include <fstream>
#include <string>
#include <list>
#include <vector>
#include <boost/function.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/process.hpp> 
#include <boost/asio.hpp>
#include <boost/thread.hpp>

#define MAX_BUFFER_SIZE (16*2048)
#ifndef NDEBUG
#define CHILD_REDIRECT_TO_FILE
#define LOG_COMMUNICATION
#endif

namespace bp = ::boost::process;
using boost::asio::ip::udp;
typedef boost::array<char, MAX_BUFFER_SIZE> rcv_container_t;
typedef boost::function<void(std::string)> userhandler_t;

/**
  Class that gets data from the main coach and replies with
  instructions for the team.
  This class is supposed to be used by feeding it through inform method whenever
  messages arrive and getting instructions for the team by defining the receive
  method.
  */
class AssistantCoach 
{
    public:
        AssistantCoach(userhandler_t rcvfunc, int listen_port=0xbeef);
        virtual ~AssistantCoach();

        /**
          Whenever data arrives from the server it should be fed to the assistant coach
          using this method.
          */
        void inform(std::string const & message);

    private:

        /**
         * method that is called when a new instruction from the assistant
         * coach arrives. Should be provided by the user in order to achieve
         * the desired behaviour.
         *
         * note: this method will be run in the AssistantCoach thread, as
         * such, necessary precautions to avoid race conditions or other
         * multi-thread problems should be taken.
         */
        userhandler_t receive; 

        /** thread that is running the work. */
        boost::thread async_worker;

        /** handler for async_receive. */
        void handle_receive(
                boost::shared_ptr<rcv_container_t> recv_buf_ptr, 
                const boost::system::error_code& error,
                std::size_t bytes_transferred);

        /** handler for async_send. */
        void handle_send(boost::shared_ptr<std::string> message_ptr,
                const boost::system::error_code& error);

        /**
         * installs the receive handler
         */
        void install_receive();

#if 0
        // worker execution control:
        /**
         * Tells the worker thread to finish
         */
        void finish();

        /* checks if the worker thread has finished
        */
        bool isfinished();

        /*
         * mutex to lock the finished variable
         */
        boost::mutex _finished_mutex;

        /*
         * the variable that contains the state of the worker thread.
         */
        bool _finished;
#endif

        // parameters to launch the child.
        const std::vector<std::string> args;
        static const std::string exec;
        static const std::string classpath;

        boost::asio::io_service io_service;

        udp::socket socket;

        udp::endpoint child_address;
        boost::scoped_ptr<bp::child> child;

#ifdef LOG_COMMUNICATION
        std::ofstream to_child;
        std::ofstream from_child;
#endif
#ifdef CHILD_REDIRECT_TO_FILE
        int childoutput;
        int childerror;
#endif

};

#endif // ASSISTANTCOACH
