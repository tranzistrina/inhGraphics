#!/bin/bash
# Авто-оп: каждому зашедшему игроку выдаётся оператор (тестовый локальный сервер)
cd ~/Desktop/lab/inhGraphics/server
tail -f -n 0 server.log | while read -r line; do
  if echo "$line" | grep -q "joined the game"; then
    nick=$(echo "$line" | sed 's/\x1b\[[0-9;]*m//g' | sed -n 's/.*INFO\]: \(.*\) joined the game/\1/p' | xargs)
    if [ -n "$nick" ]; then
      echo "[AUTO-OP] выдаю op игроку $nick" >> autoop.log
      python3 ~/Desktop/lab/inhGraphics/bot/rcon.py "op $nick" >> autoop.log 2>&1
    fi
  fi
done
