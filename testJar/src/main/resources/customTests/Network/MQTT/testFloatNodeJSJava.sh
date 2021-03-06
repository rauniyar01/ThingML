#!/bin/bash

rm cliStdo.log &> /dev/null

timeout -s SIGINT 6 mvn -q -f ServerFloatCfg/pom.xml exec:java > srvStdo.log 2> srvStdr.log&
sleep 3
timeout -s SIGINT 4 nodejs ClientFloatCfg/main.js > cliStdo.log 2> cliStdr.log&

sleep 5

#printf "Cli stdo:\n\n"
cat cliStdo.log
#printf "\nCli stdr:\n\n"
>&2 cat cliStdr.log

#printf "\n\nSrv stdo:\n\n"
>&2 cat srvStdo.log
#printf "\nSrv stdr:\n\n"
>&2 cat srvStdr.log