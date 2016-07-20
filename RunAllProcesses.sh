#!/bin/bash
set -e
read -p "Enter your email to get notifications about stopped processes : " email
echo "Your email is: " $email
export EMAIL=$email
echo "Starting running all process in parallel!!"
sleep 3
echo "Starting ProjectCreator"
cd scripts/ProjectCreator/
nohup sh script.sh &
echo "Waiting 10 seconds"
sleep 10
echo "Starting TaskCreator"
cd ../TaskCreator/
nohup sh script.sh &
echo "Waiting 20 seconds"
sleep 20
echo "Starting TwitterTaskPerformer"
cd ../TwitterTaskPerformer/
nohup sh script.sh &
echo "Starting TwitterTaskCollector"
cd ../TwitterTaskCollector/
nohup sh script.sh &
echo "Starting FacebookTaskPerformer"
cd ../FacebookTaskPerformer/
nohup sh script.sh &
echo "Starting FacebookTaskCollector"
cd ../FacebookTaskCollector/
nohup sh script.sh &
echo "Starting InstructionSet"
cd ../InstructionSet/
nohup sh script.sh &
echo "Starting RestService"
cd ../RestService/
nohup sh script.sh &
echo "All processes are now running"