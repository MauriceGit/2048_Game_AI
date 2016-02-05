#!/bin/bash

finished="true"
moveCnt=0
movesTo2048=-1
maxTile=2
i=0
roundCount=2
completeTime=$(date +%s)

echo "Runde: "
> 2048_results.log

while [[ $i -lt $roundCount ]]
do
    time=$(date +%s)

    java -jar run_2048_bot.jar < fifo | ./controller.sh > fifo

    newtime=$(date +%s)
    let time=newtime-time

    echo -n "$i "
    cat 2048_watch_game.log | tail -n 3 | head -n 2 >> 2048_results.log


    array=($(cat 2048_results.log | tail -n 2 | head -n 1))
    moveCnt=${array[2]}

    let averageMoveTime=time*1000/moveCnt

    echo "average move time: $averageMoveTime ms" >> 2048_results.log

    echo -e "\n" >> 2048_results.log

    let i=i+1

done

let completeTime=($(date +%s)-completeTime)/60

echo -e "\n"

echo "insgesamt $i Runden gespielt in $completeTime Minuten."
echo -e "4096 : $(cat 2048_results.log | grep 'Zahl: 4096' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 4096' | wc -l)*100/$roundCount))%)"
echo -e "2048 : $(cat 2048_results.log | grep 'Zahl: 2048' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 2048' | wc -l)*100/$roundCount))%)"
echo -e "1024 : $(cat 2048_results.log | grep 'Zahl: 1024' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 1024' | wc -l)*100/$roundCount))%)"
echo -e "512  : $(cat 2048_results.log | grep 'Zahl: 512' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 512' | wc -l)*100/$roundCount))%)"
echo -e "256  : $(cat 2048_results.log | grep 'Zahl: 256' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 256' | wc -l)*100/$roundCount))%)"
echo -e "128  : $(cat 2048_results.log | grep 'Zahl: 128' | wc -l)\t($(($(cat 2048_results.log | grep 'Zahl: 128' | wc -l)*100/$roundCount))%)"


