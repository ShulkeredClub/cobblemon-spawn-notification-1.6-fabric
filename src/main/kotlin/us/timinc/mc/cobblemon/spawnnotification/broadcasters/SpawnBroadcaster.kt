package us.timinc.mc.cobblemon.spawnnotification.broadcasters

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import us.timinc.mc.cobblemon.spawnnotification.SpawnNotification.config

class SpawnBroadcaster(
    val pokemon: Pokemon,
    val spawnPool: SpawnPool,
    val coords: BlockPos,
    val biome: Identifier,
    val dimension: Identifier,
    val player: ServerPlayerEntity?
) {
    private val shiny
        get() = pokemon.shiny
    private val label
        get() = pokemon.form.labels.firstOrNull { it in config.labelsForBroadcast }
    private val buckets
        get() = spawnPool
            .mapNotNull { if (it is PokemonSpawnDetail) it else null }
            .filter { it.pokemon.matches(pokemon) }
            .map { it.bucket.name }
    private val bucket
        get() = config.bucketsForBroadcast.firstOrNull { it in buckets }
    private val shouldBroadcast
        get() = (shiny && config.broadcastShiny) || label != null || bucket != null

    fun getBroadcast(): Text? {
        if (!shouldBroadcast) return null

        var msg = "Un%poke%%type%%bucket% è spawnato%biome%%pos%%cross%%near%"

        if(shiny && config.broadcastShiny) msg = msg.replace("%type%", " shiny")
        else msg = msg.replace("%type%", "")

        /*if (label != null) msg = msg.replace("2", " ${label}")
        else msg = msg.replace("2", "")*/

        if (bucket != null) msg = msg.replace("%bucket%", " $bucket")
        else msg = msg.replace("%bucket%", "")

        if(config.broadcastSpeciesName) msg = msg.replace("%poke%", " ${pokemon.species.translatedName.string}")
        else msg = msg.replace("%poke%", Text.translatable("cobblemon.entity.pokemon").string)

        if (config.broadcastBiome) msg = msg.replace("%biome%", " ${biome.toTranslationKey()}")
        else msg = msg.replace("%biome%", "")

        if (config.announceCrossDimensions) msg = msg.replace("%cross%", " in ${dimension.toTranslationKey().replace("minecraft.", "")}")
        else msg = msg.replace("%cross%", "")

        if (config.broadcastPlayerSpawnedOn && player != null) msg = msg.replace("%near%", " vicino a ${player.name.string}")
        else msg = msg.replace("%near%", "")

        if (config.broadcastCoords) msg = msg.replace("%pos%", " a ${coords.x}, ${coords.y}, ${coords.z}")
        else msg = msg.replace("%pos%", "")

        return Text.of(msg)
    }
}