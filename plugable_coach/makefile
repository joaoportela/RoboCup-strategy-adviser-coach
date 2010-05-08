CC=g++
CFLAGS=-c -Wall
SOURCES=main.cpp assistantcoach.cpp
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=acoach

.PHONY: all clean

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS) 
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

# implicit dependencies
%.o: %.h
# explicit dependencies
main.o: assistantcoach.h

clean:
	rm $(EXECUTABLE) $(OBJECTS) -vf

