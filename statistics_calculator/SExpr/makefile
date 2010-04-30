include sources
OBJECTS= $(subst .java,.class,$(SOURCES))
JAR_FILE=sexpr.jar

.PHONY: all sexpr build clean

all: sexpr

build: $(JAR_FILE)

$(JAR_FILE): $(OBJECTS)
	jar cf $@ org/

sexpr: $(OBJECTS)

%.class:%.java
	javac -Xlint $<

clean:
	@(rm -vf $(OBJECTS))
	find . -type f -name "*.class" -delete
	@(rm -vf $(JAR_FILE))

