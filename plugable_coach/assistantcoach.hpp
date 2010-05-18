#ifndef ASSISTANTCOACH
#define ASSISTANTCOACH

#include <string>
#include <list>
#include <vector>
#include <boost/scoped_ptr.hpp>
#include <boost/process.hpp> 
#include <boost/asio.hpp>
#include <boost/thread.hpp>
#include "syncronizedqueue.hpp"

#ifndef NDEBUG
#define CHILD_REDIRECT_TO_FILE
#endif

namespace bp = ::boost::process;
using boost::asio::ip::udp;

/**
  Class that gets data from the main coach and replies with
  instructions for the team.
  This class is supposed to be used by feeding it though message_received
  function whenever messages arrive and getting instructions for the team
  whenever they are available.
  */
class AssistantCoach 
{
    public:
        AssistantCoach(int listen_port=0xbeef);
        virtual ~AssistantCoach();
        // input:

        /**
          Whenever data arrives from the server it should be fed to the assistant coach
          using this method.
          */
        void inform(std::string const & message);

        // output:

    protected:
        /**
         * method that is called when a new instruction arrives.
         * should be overridden to achieve the desired behaviour.
         *
         * note: this method will be run in the AssistantCoach thread, as
         * such, necessary precautions to avoid race conditions or other
         * multithread problems should be taken.
         */
        virtual void new_instruction(std::string instruction) = 0; 

    private:
        /**
         * Function that runs on a different thread communicating with the
         * child process.
         *
         * Function that runs on a different thread communicating with the
         * child process. This function does most of the work.
         */
        void worker();
        /** thread that is running the worker. */
        boost::thread worker_thread();

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

        // parameters to launch the child.
        const int listen_port;
        const std::vector<std::string> args;
        static const std::string exec;
        static const std::string classpath;

        boost::asio::io_service io_service;

        udp::socket socket;

        udp::endpoint child_address;
        boost::scoped_ptr<bp::child> child;

#ifdef CHILD_REDIRECT_TO_FILE
        int childoutput;
        int childerror;
#endif

        // message queues
        SyncronizedQueue<std::string> messages_from_child;
        // SyncronizedQueue<std::string> messges_to_child;
};

#endif // ASSISTANTCOACH
