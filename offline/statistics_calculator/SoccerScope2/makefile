include classfiles
JPATH=.:java-xmlbuilder-0.3.jar
JAR_FILE=soccerscope.jar

.PHONY: all soccerscope build

all: soccerscope

build: $(JAR_FILE)
	cp *.jar ../../simulation_controller/

$(JAR_FILE): $(OBJECTS)
	CLASSPATH=$(JPATH) jar cf $@ soccerscope/

soccerscope: $(OBJECTS)

%.class:%.java
	CLASSPATH=$(JPATH) javac -Xlint $<

clean:
	rm -vf $(OBJECTS)
	find . -type f -name "*.class" -delete

