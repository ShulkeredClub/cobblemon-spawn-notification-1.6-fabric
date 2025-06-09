package us.timinc.mc.cobblemon.spawnnotification.events

import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawner
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config
import us.timinc.mc.cobblemon.spawnnotification.broadcasters.SpawnBroadcaster
import us.timinc.mc.cobblemon.spawnnotification.util.Broadcast
import us.timinc.mc.cobblemon.spawnnotification.util.PlayerUtil.getValidPlayers

object BroadcastSpawn {
    fun handle(evt: SpawnEvent<PokemonEntity>) {
        val world = evt.spawnablePosition.world
        val pos = evt.entity.positionTarget
        val pokemon = evt.entity.pokemon

        if (world.isClient) return
        if (pokemon.isPlayerOwned()) return

        SpawnBroadcaster(
            evt.entity.pokemon,
            evt.spawnablePosition.spawner.getSpawnPool(), //fix
            evt.spawnablePosition.position,
            evt.spawnablePosition.biomeName,
            evt.spawnablePosition.world.dimensionEntry.key.get().value,
            if (evt.spawnablePosition.spawner is PlayerSpawner) (evt.spawnablePosition.spawner as PlayerSpawner).getCauseEntity() else null
        ).getBroadcast()?.let { message ->
            if (config.announceCrossDimensions) {
                Broadcast.broadcastMessage(message)
            } else if (config.broadcastRangeEnabled) {
                Broadcast.broadcastMessage(getValidPlayers(world.dimensionEntry.key.get(), pos), message)
            } else {
                Broadcast.broadcastMessage(world, message)
            }
        }
    }
}