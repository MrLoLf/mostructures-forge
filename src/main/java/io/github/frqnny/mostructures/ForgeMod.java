package io.github.frqnny.mostructures;

import com.mojang.serialization.Codec;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Mod("mostructures")
public class ForgeMod {

    private static Method GETCODEC_METHOD;


    public ForgeMod() {
        // For registration and init stuff.
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MoStructures.STRUCTURE_FEATURES.register(modEventBus);
        modEventBus.addListener(this::setup);

        // For events that happen after initialization. This is probably going to be use a lot.
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.NORMAL, this::addDimensionalSpacing);

        // The comments for BiomeLoadingEvent and StructureSpawnListGatherEvent says to do HIGH for additions.
        forgeBus.addListener(EventPriority.HIGH, this::biomeModification);
    }

    public void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MoStructures::register);
    }

    public void biomeModification(final BiomeLoadingEvent event) {

        MoStructures.putStructures(event);
        //event.getGeneration().getStructures().add(MoStructures::putStructures);
    }

    public void addDimensionalSpacing(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();


            try {
                if (GETCODEC_METHOD == null)
                    GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey((Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(serverWorld.getChunkSource().generator));
                if (cgRL != null && cgRL.getNamespace().equals("terraforged")) return;
            } catch (Exception e) {
                //StructureTutorialMain.LOGGER.error("Was unable to check if " + serverWorld.dimension().location() + " is using Terraforged's ChunkGenerator.");
            }


            if (serverWorld.getChunkSource().getGenerator() instanceof FlatChunkGenerator &&
                    serverWorld.dimension().equals(World.OVERWORLD)) {
                return;
            }

            Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
            //tempMap.putIfAbsent(STStructures.RUN_DOWN_HOUSE.get(), DimensionStructuresSettings.DEFAULTS.get(STStructures.RUN_DOWN_HOUSE.get()));
            //serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
        }
    }
}
