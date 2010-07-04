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
FILES_TO_DEPLOY = assistantcoach.hpp assistantcoach.cpp *.jar
DEPLOY_TARGET = ~/FCPortugal/2D/trunk/fcportugal/coach/

.PHONY: all clean depend fullclean deploy jars

all: jars $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS) 
	$(LD) $(LDFLAGS) $(OBJECTS) -o $@

jars:
	cd ../statistics_calculator/SoccerScope2/; make deploy

clean:
	$(RM) $(EXECUTABLE) $(OBJECTS) $(RMFLAGS)

fullclean: clean
	$(RM) *.log $(RMFLAGS)

depend:
	gcc -MM $(CXXFLAGS) *.cpp > makefile.d

deploy: $(EXECUTABLE) jars
	cp $(FILES_TO_DEPLOY) $(DEPLOY_TARGET)

# Dependencies tracking:
# implicit dependencies
%.o: %.cpp %.hpp
# generated dependencies
-include ./*.d

