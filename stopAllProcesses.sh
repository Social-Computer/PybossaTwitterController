#!/bin/bash
set -e
ps -ef | grep "recoin.mongodb_version"
read -p "Are you sure you want to KILL the above processes? (Y/n) : " answer
if [ "$answer" == "Y" ]; then
	echo "Preperting to kill these processes: "
	numberProcess=($(ps -ef | grep "recoin.mongodb_version" | wc -l))
	echo "There are $numberProcess Processes"
	if (($numberProcess > 1)); then
		ps -ef | grep "recoin.mongodb_version" | grep -v grep | awk '{print $2}' | xargs kill -9
		echo "Processes are killed!!"
	else
		echo "No processes were found!!"
	fi
else
	echo "No actions were taken :)"
fi