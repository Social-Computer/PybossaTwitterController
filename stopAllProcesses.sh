#!/bin/bash
set -e
read -p "Are you sure you want to KILL all processes related to TwitterCrowdSourcingController (Y/n) : " answer
if [ "$answer" == "Y" ] then
	ps -ef | grep "recoin.mongodb_version" | grep -v grep | awk '{print $2}' | xargs kill -9
else
	echo "No actions were taken :)"
fi