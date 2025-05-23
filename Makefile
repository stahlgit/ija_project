all: run

run:
	mvn javafx:run

install:
	mvn clean install

compile: 
	mvn clean compile

clean:
	rm -rf data/log/*