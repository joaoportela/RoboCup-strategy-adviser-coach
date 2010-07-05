#ifndef SYNCRONIZEDQUEUE
#define SYNCRONIZEDQUEUE

#include <boost/thread.hpp>
#include <queue>

/**
 * Very simple thread safe message queue.
 *
 * With thanks to
 * http://www.justsoftwaresolutions.co.uk/threading/implementing-a-thread-safe-queue-using-condition-variables.html
 * for the guidelines.
 *
 * Maybe in the future I can extend this to allow simultaneous (not mutually excluded by
 * mutex) push and pop ;) 
 * Although it would probably require that I did not use a std::queue.
 */
template <typename T>
class SyncronizedQueue {
    /**
     * the underlying stl queue that contains the data.
     */
    std::queue<T> _queue;
    /**
     * the mutex that controls the access to the data.
     */
    mutable boost::mutex _queue_mutex;
    /**
     * condition variable to wait for the queue to contain elements.
     */
    boost::condition_variable _queue_condition_variable;

    public:

    /**
     * pushes an element into the queue
     */
    void push(const T& x) {
        // lock
        boost::mutex::scoped_lock lock(this->_queue_mutex);
        this->_queue.push(x);
        lock.unlock();
        this->_queue_condition_variable.notify_one();
    }

    /**
     * pops the first element from the queue. When the queue is empty it locks
     * until it can pop.
     *
     * note: It has come to my attention that pp should return void in order to
     * make clear that it has side-effects on the SyncronizedQueue object.
     */
    const T& pop() {
        // lock
        boost::mutex::scoped_lock lock(this->_queue_mutex);
        // wait for data to be available
        while(this->_queue.empty()) {
            this->_queue_condition_variable.wait(lock);
        }

        // pop it.
        T &x = this->_queue.front();
        this->_queue.pop();

        return x;
    }

    /**
     * Pops an element from the queue, if available. If the queue is empty it
     * returns immediately.
     */
    bool try_pop(T& x) {
        // lock
        boost::mutex::scoped_lock lock(this->_queue_mutex);
        if(this->_queue.empty())
        {
            return false;
        }
        x=this->_queue.front();
        this->_queue.pop();
        return true;
    }
};

#endif // SYNCRONIZEDQUEUE
