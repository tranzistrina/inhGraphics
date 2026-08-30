// Шпион: логирует ВООБЩЕ всё, что приходит в чат/системные сообщения
const mineflayer = require('mineflayer')
const bot = mineflayer.createBot({
  host: 'localhost', port: 25565,
  username: 'ChatSpy', version: '1.21.1',
})
bot.on('message', (jsonMsg) => {
  console.log('[CHAT]', jsonMsg.toString())
})
bot.on('systemChat', (msg) => {
  console.log('[SYSTEMCHAT]', msg.toString ? msg.toString() : JSON.stringify(msg))
})
bot.once('spawn', () => console.log('[SPY] на месте, слушаю чат'))
bot.on('kicked', r => console.log('[KICKED]', JSON.stringify(r)))
bot.on('error', e => console.log('[ERR]', e.message))
