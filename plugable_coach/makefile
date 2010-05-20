CXXFLAGS = -c -Wall -g 
LIBS= -lboost_system-mt -lboost_thread-mt -lboost_date_time-mt
LDFLAGS = $(LIBS)
SOURCES = main.cpp dieroll.cpp assistantcoach.cpp
OBJECTS = $(SOURCES:.cpp=.o)
EXECUTABLE = acoach
CXX = g++
LD = g++
RM = rm
RMFLAGS = -vf

.PHONY: all clean depend fullclean

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS) 
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

# implicit dependencies
%.o: %.hpp

clean:
	$(RM) $(EXECUTABLE) $(OBJECTS) $(RMFLAGS)

fullclean: clean
	$(RM) *.log $(RMFLAGS)

depend:
	gcc -MM $(CXXFLAGS) *.cpp > makefile.d

# Dependencies tracking:
-include ./*.d

