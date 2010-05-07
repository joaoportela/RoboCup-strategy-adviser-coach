#ifndef ASSISTANT_COACH
#define ASSISTANT_COACH

#include <string>
#include <list>

/**
  Class that gets data from the main coach and replies with
  instructions for the team.
  This class is supposed to be used by feeding it though message_received
  function whenever messages arrive and getting instructions for the team
  whenever they are available.
  */
class AssistantCoach {

    public:
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

    protected:
        // connection and some other things I don't know about.

        std::list<std::string> out_messages;
};

#endif // ASSISTANT_COACH
