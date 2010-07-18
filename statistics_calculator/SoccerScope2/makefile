include sources
OBJECTS= $(subst .java,.class,$(SOURCES))#this macro didn't work: $(SOURCES,.java=.class)
JRI_JAR=/usr/local/lib/R/site-library/rJava/jri/JRI.jar
JPATH=.:java-xmlbuilder-0.3.jar:sexpr.jar:$(JRI_JAR)
JAR_FILE=soccerscope.jar
DEPEND_JARS=java-xmlbuilder-0.3.jar sexpr.jar $(JRI_JAR)
ALL_JARS=$(JAR_FILE) $(DEPEND_JARS)

.PHONY: all soccerscope build clean deploy

all: soccerscope

build: $(ALL_JARS)

deploy: build
	@echo "deploying jars in the simulation_controler"
	@(cp $(ALL_JARS) ../../simulation_controller/ -v)
	@echo "deploying jars in the plugable_coach"
	@(cp $(ALL_JARS) ../../plugable_coach/ -v)

sexpr.jar:
	@(cd ../SExpr; $(MAKE) $@)
	@(cp ../SExpr/$@ ./ -v)

$(OBJECTS): $(DEPEND_JARS)

$(JAR_FILE): $(OBJECTS)
	CLASSPATH=$(JPATH) jar cf $@ soccerscope/

soccerscope: $(OBJECTS)

%.class:%.java
	CLASSPATH=$(JPATH) javac -Xlint $<

clean:
	@(rm -vf $(OBJECTS))
	find . -type f -name "*.class" -delete
	@(rm -vf $(JAR_FILE) sexpr.jar)
