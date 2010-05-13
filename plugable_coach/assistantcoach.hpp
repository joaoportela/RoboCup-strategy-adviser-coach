#ifndef ASSISTANTCOACH
#define ASSISTANTCOACH

#include <string>
#include <list>
#include <vector>
#include <boost/scoped_ptr.hpp>
#include <boost/process.hpp> 
#include <boost/asio.hpp>

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
class AssistantCoach {

    public:
        AssistantCoach(int listen_port=0xbeef);
        virtual ~AssistantCoach();
        // input:

        /**
          Whenever data arrives from the server it should be fed to the assistant coach
          using this method.
          */
        void message_received(std::string const & message);

        // output:

        /**
          Whenever the assistant coach has instructions for the team this
          method returns the list of instructions that should be issued. Each
          instruction should be sent as a separate message.
          */
        std::list<std::string> instructions();

        /**
          Returns true when there are instructions pending. This method is
          useful to check whether there are instructions to ready without
          consuming them.
          */
        bool has_instructions() const;

    private:
        // parameters to launch the child.
        const int listen_port;
        const std::vector<std::string> args;
        static const std::string exec;
        static const std::string classpath;

        // io service.
        boost::asio::io_service io_service;

        // socket.
        udp::socket socket;

        // child data
        udp::endpoint child_address;
        boost::scoped_ptr<bp::child> child;

        // instructions waiting to be consumed.
        std::list<std::string> out_messages;

#ifdef CHILD_REDIRECT_TO_FILE
        int childoutput;
        int childerror;
#endif
};

#endif // ASSISTANTCOACH
