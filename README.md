## what

A quick sketch poc of a Reactor pipeline that uses JRuby's Red Bridge API. 

The reactor bits were based on the original from spring-guides (see: https://github.com/spring-guides/gs-messaging-reactor/tree/master/complete)

## build and run

    mvn package
    ava -server -jar target/logstash-jruby-reactor-alpha.0.jar
    
