CXXFLAGS = -c -Wall -g 
LIBS= -lboost_system-mt -lboost_thread-mt
LDFLAGS = $(LIBS)
SOURCES = main.cpp assistantcoach.cpp
OBJECTS = $(SOURCES:.cpp=.o)
EXECUTABLE = acoach
CXX = g++
LD = g++

.PHONY: all clean

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS) 
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

# implicit dependencies
%.o: %.hpp

# explicit dependencies
main.o: assistantcoach.hpp

clean:
	rm $(EXECUTABLE) $(OBJECTS) -vf

depend:
	gcc -MM $(CXXFLAGS) *.cpp > makefile.d

# Dependencies tracking:
-include ./*.d

