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
SOURCE_FILES_TO_DEPLOY = assistantcoach.hpp assistantcoach.cpp
SOURCE_FILES_DEPLOY_TARGET = ~/FCPortugal/2D/trunk/fcportugal/coach/
JARS_TO_DEPLOY =  *.jar
JARS_TARGET = ~/FCPortugal/2D/trunk/fcportugal/

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
	cp $(SOURCE_FILES_TO_DEPLOY) $(SOURCE_FILES_DEPLOY_TARGET)
	cp $(JARS_TO_DEPLOY) $(JARS_TARGET)

# Dependencies tracking:
# implicit dependencies
%.o: %.cpp %.hpp
# generated dependencies
-include ./*.d

