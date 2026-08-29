// Тестовый бот: заходит на сервер и репортит своё клиентское состояние.
const mineflayer = require('mineflayer')

const bot = mineflayer.createBot({
  host: 'localhost',
  port: 25565,
  username: 'TestBot',
  version: '1.21.1',
})

bot.once('spawn', () => {
  console.log('[BOT] заспавнился, позиция:', bot.entity.position)
})

// состояние каждые 2 секунды
setInterval(() => {
  if (!bot.entity) return
  const t = bot.time ? Math.round(bot.time.timeOfDay) : -1
  let chunks = -1
  let chunkErr = ''
  try { chunks = Object.keys(bot.world.async.columns).length } catch (e) { chunkErr = e.message }
  console.log(`[STATE] time=${t} raining=${bot.isRaining} thunder=${bot.thunderState} rain=${bot.rainState} chunks=${chunks}${chunkErr ? ' ERR=' + chunkErr : ''}`)
}, 2000)

// звуки (ловим гром)
bot.on('soundEffectHeard', (soundName, position, volume, pitch) => {
  console.log(`[SOUND] ${soundName} vol=${volume}`)
})

// частицы (туман): в 1.21.1 пакет называется world_particles
let particleCount = 0
try {
  bot._client.on('world_particles', () => {
    particleCount++
    if (particleCount <= 5 || particleCount % 50 === 0) {
      console.log(`[PARTICLES] получено пакетов частиц: ${particleCount}`)
    }
  })
} catch (e) { /* ок */ }

bot.on('kicked', r => console.log('[KICKED]', JSON.stringify(r)))
bot.on('error', e => console.log('[ERROR]', e.message))
