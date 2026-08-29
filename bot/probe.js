// Диагностика: слушаем все game_state_change
const mineflayer = require('mineflayer')
const bot = mineflayer.createBot({ host:'localhost', port:25565, username:'TestBot', version:'1.21.1' })
bot._client.on('game_state_change', p => console.log(`[GAMEEVENT] reason=${p.reason} value=${p.gameMode}`))
bot.once('spawn', () => console.log('[BOT] spawned'))
setInterval(()=>{}, 1e9)
bot.on('error', e => console.log('[ERR]', e.message))
