package com.emandev.bifrost;

import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.spell_engine.api.spell.container.*;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.casting.SpellCaster;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Native active skill; custom combat remains in source but is deliberately dormant. */
public final class BifrostSkills {
    public static final Identifier TRAVEL=BifrostMod.id("travel");
    public static boolean customAbilitiesEnabled(){return false;}
    public static void setup(FMLCommonSetupEvent event){
        event.enqueueWork(()->SpellHandlers.registerCustomDelivery(TRAVEL,(level,spell,caster,targets,context,location)->
            caster instanceof ServerPlayer p && open(p)));
    }
    public static boolean open(ServerPlayer player){
        var stack=player.getMainHandItem();
        if(!AsgardWeapons.ready(player,stack))return false;
        // This is the completion of this instant cast. Other casts cannot overlap it
        // through Spell Engine, so do not reject our own current spell process here.
        BifrostMenuService.open(player,null,stack.getHoverName().getString());
        return true;
    }
    public static void append(Player player){
        var stack=player.getMainHandItem();
        if(AsgardWeapons.kind(stack).isEmpty())return;
        var owner=(SpellContainerSource.Owner)player;var result=owner.getSpellContainers();
        if(!result.actives().isEmpty()&&result.actives().getLast().is(TRAVEL))return;
        var spell=SpellRegistry.from(player.level()).get(TRAVEL).orElse(null);
        if(spell==null||result.activeContainer()==null)return;
        var actives=new ArrayList<>(result.actives());actives.removeIf(h->h.is(TRAVEL));actives.add(spell);
        var sources=new ArrayList<>(result.sources());
        sources.removeIf(s->s.name().equals("bifrost:weapon_skills"));
        var container=SpellContainers.forWeapon(SpellContainer.ContentType.ANY,List.of(TRAVEL));
        sources.add(new SpellContainerSource.SourcedContainer("bifrost:weapon_skills",stack,container));
        var activeContainer=result.activeContainer().withAdditionalSpell(List.of(TRAVEL.toString()));
        owner.setSpellContainers(new SpellContainerSource.Result(activeContainer,List.copyOf(actives),result.passives(),result.modifiers(),List.copyOf(sources)));
        if(!player.level().isClientSide())((SpellCaster.Player)player).getInteractor().invalidateOptions();
    }
}
