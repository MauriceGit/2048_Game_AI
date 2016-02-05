#!/bin/bash

finished="true"
moveCnt=0
movesTo2048=-1
maxTile=2

while read line
do
	if [[ "$finished" = "true" ]]
	then
		echo "go"
	fi
	
	finished="false"
	
	move=$(echo "$line" | grep "bestmove" | sed 's/bestmove //g')
	if [[ -n "$move" ]]
	then
		move="apply $move"
		echo "$move"
		let moveCnt=moveCnt+1
		finished="true"
	fi
done

echo -e "Insgesamt wurden $moveCnt Züge getätigt.\n" >> "./2048_watch_game.log"

