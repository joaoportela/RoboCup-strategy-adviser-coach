#include <iostream>
#include <boost/thread.hpp>

#include "syncronizedqueue.hpp"

using namespace std;

#include <boost/random/mersenne_twister.hpp>
#include <boost/random/uniform_int.hpp>
#include <boost/random/variate_generator.hpp>

// could use this for random numbers
// #include <ctime>
// std::time(0)

boost::mt19937 gen;
int rolldie(int lower_bound = 1, int upper_bound = 6) {
    boost::uniform_int<> dist(lower_bound, upper_bound);
    boost::variate_generator<boost::mt19937&, boost::uniform_int<> > die(gen, dist);
    return die();
}

SyncronizedQueue<int> thequeue;

struct worker_thread
{
    void operator() (){
        cout <<"async: start" << endl;
        for(int i = 0;  i < 10; i++){
            int r = rolldie(1,2);
            // cout << "async die: " << r << endl;
            boost::this_thread::sleep(boost::posix_time::seconds(r));
            cout << "async: wakeup" << endl;
            thequeue.push(i);
            cout << "async: pushed " << i << endl;
        }
        cout <<"async: end" << endl;
    }
};

int main(int argc, const char* argv[]) {
    boost::thread async_worker= boost::thread(worker_thread());

    int popd = 0;
    int r = 0;
    do{
        if(popd < 4) {
            r = rolldie(1,4);
            // // cout << "main die: " << r << endl;
            boost::this_thread::sleep(boost::posix_time::seconds(r));
            // cout << "main: wakeup" << endl;
        }
        popd = thequeue.pop();
        cout << "popped " << popd << endl;
    }while(popd != 9);

    async_worker.join();
}
