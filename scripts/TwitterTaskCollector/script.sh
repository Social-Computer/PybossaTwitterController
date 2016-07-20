#!/bin/bash
cmd="java -Xmx5G -Dlog4j.configuration=file:../../log4j.properties -cp ../../target/TwitterCrowdSourcingController-0.0.1.jar:../../target/lib/* recoin.mongodb_version.TwitterTaskCollector" 
eval $cmd
echo "$cmd" | mail -s "Process is stopped" "$EMAIL"
