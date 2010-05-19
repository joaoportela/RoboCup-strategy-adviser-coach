#include <boost/random/mersenne_twister.hpp>
#include <boost/random/uniform_int.hpp>
#include <boost/random/variate_generator.hpp>

boost::mt19937 gen;
int rolldie(int lower_bound, int upper_bound) {
    boost::uniform_int<> dist(lower_bound, upper_bound);
    boost::variate_generator<boost::mt19937&, boost::uniform_int<> > die(gen, dist);
    return die();
}

