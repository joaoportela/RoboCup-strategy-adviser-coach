CC=g++
CXXFLAGS=-c -Wall
LIBS= -lboost_system-mt
LDFLAGS = $(LIBS)
SOURCES=main.cpp assistantcoach.cpp
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=acoach
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

