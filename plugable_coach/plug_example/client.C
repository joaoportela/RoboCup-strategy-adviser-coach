/* -*- Mode: C++ -*- */

/* coach/client.C
 * CMUnited99 (code for on-line/off-line coach)
 * Patrick Riley <pfr+@cs.cmu.edu>
 * Computer Science Department
 * Carnegie Mellon University
 * Copyright (C) 1999 Patrick Riley
 *
 * CMUnited-99 was created by Peter Stone, Patrick Riley, and Manuela Veloso
 *
 * You may copy and distribute this program freely as long as you retain this notice.
 * If you make any changes or have any comments we would appreciate a message.
 */

/* this file contains the main program for the on-line/off-line coach */

#include "client.h"
#include "types.h"
#include "netif.h"
#include "Memory.h"
#include "parse.h"
#include "utils.h"
#include "logfile.h"
#include "stdlib.h"
#ifdef USE_JAVA_ASSISTANT
#include "assistantcoach.hpp"
#endif

Bool      wait_for_signals(sigset_t *);
sigset_t  init_handler();
void      sigio_handler();
void      sigalrm_handler();
void AnalyzeLogLoop();
void ConnectToServerLoop();

void send_initialize_message();
Bool parse_initialize_message(char* mess) ;
/* Global variables -- don't want to reallocate buffers each time */

/* these are for network IO */
sigset_t sigiomask, sigalrmask;

Memory *Mem;

char     recvbuf[MAXMESG];
char     sendbuf[MAXMESG];

Socket sock;

int      max_alrs_since_io = 10;
int      alrsigs_since_iosig=0;



#ifdef USE_JAVA_ASSISTANT
boost::scoped_ptr<AssistantCoach> acoach_ptr;
/*
 * function object where message receiving is reported.
 *
 * this function is always called from the 
 */
struct report_struct
{
    char sendbuf_[MAXMESG];
    Socket& sock_;

    report_struct(Socket& sock): sock_(sock){}

    void operator() (std::string instruction)
    {
        // always fits in the buffer
        BOOST_ASSERT(instruction.size() <= (MAXMESG-19)); 
        sprintf(sendbuf_, "(say (freeform \"%s\"))",  instruction.c_str());
        std::cerr << "redirecting instruction: " << sendbuf_ << std::endl;
        send_message(sendbuf_, &(sock_));
        // memset(sendbuf_,0x00,MAXMESG*sizeof(char)); // no need.
    }

};
#endif
/****************************************************************************/
int main(int argc, char *argv[])
{
  //for (int i = 0; i < argc; i++)
  //  printf("%s ", argv[i]);
  //printf("\n");

  Mem = new Memory();

  if ( Mem == NULL ){
    my_error((char *)"couldn't allocate Mem");
    exit(0);
  }

  Mem->GetOptions(argc,argv);

  if (Mem->CP_analyze_log)
    AnalyzeLogLoop();
  else
    ConnectToServerLoop();

  //printf("Shutting down coach\n");

/*****
  fprintf(Mem->coach_log,"Fim do coach_log\n");

  fprintf(Mem->coach_log,"N_play_cycles=%d\n",Mem->N_play_cycles);
  fprintf(Mem->coach_log,"N_ball_their_side=%d\n",Mem->N_ball_their_side);
  fprintf(Mem->coach_log,"N_their_ball=%d\n",Mem->N_their_ball);
  fprintf(Mem->coach_log,"N_our_ball=%d\n",Mem->N_our_ball);
  fprintf(Mem->coach_log,"N_their_shoots=%d\n",Mem->N_their_shoots);
  fprintf(Mem->coach_log,"N_their_successful_shoots=%d\n",Mem->N_their_successful_shoots);
  fprintf(Mem->coach_log,"N_our_shoots=%d\n",Mem->N_our_shoots);
  fprintf(Mem->coach_log,"N_our_successful_shoots=%d\n",Mem->N_our_successful_shoots);
  fprintf(Mem->coach_log,"sum_cycles_gfk=%d\n",Mem->sum_cycles_gfk);
  fprintf(Mem->coach_log,"N_gfk=%d\n",Mem->N_gfk);
  fprintf(Mem->coach_log,"N_gfk_opposite=%d\n",Mem->N_gfk_opposite);
  fprintf(Mem->coach_log,"gfk_x=%1.2f..%1.2f\n",Mem->gfk_min_x,Mem->gfk_max_x);
  for(int i=0; i<Min(Mem->N_gfk,MAX_GFK); i++)
    fprintf(Mem->coach_log,"%d (time=%d): x=%1.2f, y=%1.2f, %1.0f to player %d\n",
      i,
      Mem->gfk_history[i].kick_time,
      Mem->gfk_history[i].goalie_pos.x,
      Mem->gfk_history[i].goalie_pos.y,
      Mem->gfk_history[i].ball_vel.dir(),
      Mem->gfk_history[i].player);
  fprintf(Mem->coach_log,"N_gfk_prot=%d\n",Mem->N_gfk_prot);
  for(int i=0; i<Mem->N_gfk_prot; i++)
    fprintf(Mem->coach_log,"%d (%d cases): x=%1.2f, y=%1.2f [%d %d]\n",
      i,
      Mem->gfk_prototypes[i].n_cases,
      Mem->gfk_prototypes[i].goalie_pos.x,
      Mem->gfk_prototypes[i].goalie_pos.y,
      Mem->gfk_prototypes[i].same_player?1:0,
      Mem->gfk_prototypes[i].player);
******/

  Mem->SendCoachMessage();

  //int xx = fclose(Mem->coach_log);  // LSL8
  //printf("Resultado do fclose: %d\n",xx);

  return 0;
}

/****************************************************************************/
/****************************************************************************/
/****************************************************************************/

void AnalyzeLogLoop()
{
  if (Mem->MP_cycles_to_store < 2) {
    my_error((char *)"Need to store at least 2 cycles to analyze log files");
    return;
  }

  if (!Mem->Initialize()) {
    my_error((char *)"Memory failed to initialize");
    return;
  }

  printf("Coach started, analyzing log file '%s'\n", Mem->CP_log_fn);

  FILE* logfp;
  RecVersion ver;
  dispinfo_t buf;

  logfp = OpenLogFileForRead(Mem->CP_log_fn , &ver);
  if (logfp == NULL) {
    my_error((char *)"Could not open log file");
    return;
  }

  while (!Mem->ShutDown && ReadLog(logfp, &buf, ver)) {
    int mode = IncorporateDispInfo(&buf, Mem);
    if (mode == SHOW_MODE && Mem->GetTime() % 1000 == 999)
      // cout << "Time: " << Mem->GetTime() << endl;

    if (mode == SHOW_MODE) {
      /* INSERT CODE HERE! */
      /* Call whatever functions you want called for processing the log file.
	 The position info of everything is in the Mem structure */
    }

  }

}


/****************************************************************************/
/****************************************************************************/
/****************************************************************************/

void ConnectToServerLoop()
{
    //this is an online coach
    sock = init_connection(Mem->SP_host,Mem->SP_olcoach_port);

    if(sock.socketfd == -1) {
        cerr << "Can't open connection for trainer" << endl;
        abort();
    }

    send_initialize_message();

#ifdef USE_JAVA_ASSISTANT
    // instanciate the assistant coach
    BOOST_ASSERT(!acoach_ptr);
    acoach_ptr.reset(new AssistantCoach(report_struct(sock)));
#endif

    if ( wait_message(recvbuf, &sock) == 0 )
        my_error((char *)"wait_message failed");

    if (!parse_initialize_message(recvbuf)) {
        my_error((char *)"Could not parse initialize message");
        return;
    }

    if (!Mem->Initialize()) {
        my_error((char *)"Memory failed to initialize");
        return;
    }

    printf("FCPortugal -> Starting Coach\n");

    max_alrs_since_io =
        (int) (((float)Mem->CP_max_cycles_since_io) * (float)Mem->SP_send_vi_step / (float)Mem->CP_alarm_interval + 2.0);  //int LPR 2003

    sigset_t sigfullmask = init_handler();

    Eye(TRUE);

    while ( !Mem->ShutDown == TRUE && wait_for_signals(&sigfullmask) )
        ;

#ifdef USE_JAVA_ASSISTANT
    // it is now safe to destroy the assistant coach object.  lets do it before
    // the connection is closed (not that it should make much difference).
    BOOST_ASSERT(acoach_ptr); // hoping unspecified-bool-type works :)
    acoach_ptr.reset();
#endif

    if (sock.socketfd != -1) close_connection(&sock);

    printf("FCPortugal -> Killing Coach\n");
}

/****************************************************************************/

void send_initialize_message()
{
  sprintf(sendbuf, "(init %s (version %.2f))", Mem->CP_team_name, Mem->SP_version);
  fprintf(stderr, "COACH: (init %s (version %.2f))\n", Mem->CP_team_name, Mem->SP_version);

  if(send_message(sendbuf, &sock) == -1) abort();
}

/****************************************************************************/

Bool parse_initialize_message(char* mess)
{
  if ( !(strncmp(recvbuf,"(init",4)) ) {
    /* It's an init msg */
    /* I don't have to do anything */
  } else {
    my_error((char *)"Didn't get an init message: '%s'",mess);
    Mem->ShutDown = TRUE;
    return FALSE;
  }
  return TRUE;
}


/****************************************************************************/


/* set time interval between the sensor receiving and command sending */
inline void set_timer() {
  struct itimerval itv;
  itv.it_interval.tv_sec = 0;
  itv.it_interval.tv_usec = Mem->CP_alarm_interval * 1000;
  itv.it_value.tv_sec = 0;
  itv.it_value.tv_usec = Mem->CP_alarm_interval * 1000;
  setitimer(ITIMER_REAL, &itv, NULL);
}

inline void set_timer(int usec) {
  struct itimerval itv;
  itv.it_interval.tv_sec = 0;
  itv.it_interval.tv_usec = Mem->CP_alarm_interval * 1000;
  itv.it_value.tv_sec = 0;
  itv.it_value.tv_usec = usec;
  setitimer(ITIMER_REAL, &itv, NULL);
}

/****************************************************************************/

sigset_t init_handler() {
  sigemptyset(&sigiomask);
  sigaddset(&sigiomask, SIGIO);

  struct sigaction sigact;
  sigact.sa_flags = 0;
  sigact.sa_mask = sigiomask;

#ifdef Solaris
  sigact.sa_handler = (void (*)(int))sigalrm_handler;
#else
  sigact.sa_handler = (void (*)(int))sigalrm_handler;
#endif

  sigaction(SIGALRM, &sigact, NULL);
  sigact.sa_mask = sigalrmask;

#ifdef Solaris
  sigact.sa_handler = (void (*)(int))sigio_handler;
#else
  sigact.sa_handler = (void (*)(int))sigio_handler;
#endif

  sigaction(SIGIO, &sigact, NULL);
  set_timer();
  sigprocmask(SIG_UNBLOCK, &sigiomask, NULL);
  sigprocmask(SIG_UNBLOCK, &sigalrmask, NULL);

  sigset_t sigsetmask;
  sigprocmask(SIG_BLOCK, NULL, &sigsetmask);   /* Get's the currently unblocked signals */
  return sigsetmask;
}


/****************************************************************************/

/* suspend the process until one of the signals comes through */
/* could check for situation to kill client, return FALSE     */
/* i.e. too many actions with no sensory input coming in      */
Bool wait_for_signals(sigset_t *mask){
  sigsuspend(mask);
  return TRUE;
}


/****************************************************************************/

/* SIGIO handler: receive and parse messages from server */
void sigio_handler() {
    sigprocmask(SIG_BLOCK, &sigalrmask, NULL);
    int counter = 0;

    while (receive_message(recvbuf, &sock) == 1) {

        recvbuf[strlen(recvbuf)+1]='\0';  // 2002 - para compensar a eliminacao do newline
        recvbuf[strlen(recvbuf)]='\n';    //        que apareceu em 2002

#ifdef USE_JAVA_ASSISTANT
        // fprintf(stderr, "client.C rcv[%s]\n",recvbuf);
        // inform the assistant coach
        acoach_ptr->inform(string(recvbuf));
#endif

        SenseType st = Parse(recvbuf, Mem);
        switch (st) {
            case ST_None:
                my_error((char *)"Could not incorporate sense");
                break;
            case ST_See:
                /* INSERT CODE HERE! */
                /* Call whatever functions you want for processing the reciept of visual
                   information.
                   The position info of everything is in the Mem structure */
                if (Mem->CP_coach_setplays) {
                    Mem->SetplaySightHandler();
                }
                break;
            case ST_HearPlayer:
                /* INSERT CODE HERE! */
                /* Call whatever functions you want for processing the reciept of auditory
                   information.
                   The position info of everything is in the Mem structure */
                if (Mem->CP_coach_setplays) {
                    Mem->SetplayPlayerSoundHandler();
                }
                break;
            case ST_HearReferee:
                /* the info is already incorporated... */
                break;
            case ST_HearCoach:
            case ST_HearTrainer:
                break;
                //version 7
            case ST_ServerParam:
            case ST_PlayerParam:
            case ST_PlayerType:
                break;
            case ST_Ok:
            case ST_Error:
                break;
            default:
                my_error((char *)"Illegal value of SenseType: %d", st);
                break;
        }

        counter++;
    }

    sigprocmask(SIG_UNBLOCK, &sigalrmask, NULL);

    alrsigs_since_iosig = 0;

    // if (counter>1) printf("Got %d messages\n",counter);
}

/*****************************************************************************/

/* we just use this to recognize when the server dies */
void sigalrm_handler()
{
  alrsigs_since_iosig++;
  if (alrsigs_since_iosig > max_alrs_since_io) {
    //my_error((char *)"Too many alarms between ios from server. Shutting down");
    Mem->ShutDown = TRUE;
  }

}

/****************************************************************************/
/****************************************************************************/
/****************************************************************************/

const char* PmodeStrings[PM_MAX+1] = PLAYMODE_STRINGS;

void ChangeMode(Pmode pm)
{
  sprintf(sendbuf, "(say %s)", PmodeStrings[pm]);
  send_message(sendbuf, &sock);
}

void CheckBall()
{
  send_message((char *)"(check_ball)", &sock);
}

void Ear(Bool OnOff)
{
  sprintf(sendbuf, "(ear %s)", OnOff ? "on" : "off");
  send_message(sendbuf, &sock);
}

void Eye(Bool OnOff)
{
  sprintf(sendbuf, "(eye %s)", OnOff ? "on" : "off");
  send_message(sendbuf, &sock);
}

void Look()
{
  send_message((char *)"(look)", &sock);
}

void MoveBall(Vector pos)
{
  sprintf(sendbuf, "(move (ball) %f %f)", pos.x, pos.y);
  send_message(sendbuf, &sock);
}

void MovePlayer(TeamSide side, Unum num, Vector pos, float ang)
{
  if (ang == NOANG)
    sprintf(sendbuf, "(move (player %c %d) %f %f)",
	    (side == TS_Left) ? 'l' : 'r', num,
	    pos.x, pos.y);
  else
    sprintf(sendbuf, "(move (player %c %d) %f %f %f)",
	    (side == TS_Left) ? 'l' : 'r', num,
	    pos.x, pos.y, ang);
  send_message(sendbuf, &sock);
}

void Say(char* mess)
{
  sprintf(sendbuf, "(say (freeform \"%s\"))",  mess);
  //cout << "Say: " << sendbuf << endl;
  cerr << Mem->GetTime() << ") Coach Talks " << endl;
  send_message(sendbuf, &sock);
}

void SayCLang(char* mess)
{
  sprintf(sendbuf, "(say %s)",  mess);
  cerr << "Say: " << sendbuf << endl;
  cerr << Mem->GetTime() << ") Coach Talks " << endl;
  send_message(sendbuf, &sock);
}

void ChangePlayerType(Unum num, int pl_type)
{
  sprintf(sendbuf, "(change_player_type %d %d)",  num,pl_type);
  //cout << "ChangePlayer: " << sendbuf << endl;
  send_message(sendbuf, &sock);
}

