#!/bin/bash

timeout 8 socat -x -v PTY,link=modem0 PTY,link=modem1&
sleep 1
timeout -s SIGINT 6 node ServerEmptyCfg/main.js > srvStdo.log 2> srvStdr.log&
sleep 1
timeout -s SIGINT 5 mvn -q -f ClientEmptyCfg/pom.xml exec:java > cliStdo.log 2> cliStdr.log&

sleep 6

#printf "Cli stdo:\n\n"
cat cliStdo.log
#printf "\nCli stdr:\n\n"
>&2 cat cliStdr.log

#printf "\n\nSrv stdo:\n\n"
>&2 cat srvStdo.log
#printf "\nSrv stdr:\n\n"
>&2 cat srvStdr.log