package net.darkhax.ironwolfarmor.neoforge;
import net.darkhax.ironwolfarmor.common.WolfArmorBalance;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
@Mod("ironwolfarmor")
public class IronWolfArmorNeoforge {
 public IronWolfArmorNeoforge(IEventBus bus){bus.addListener(WolfArmorBalance::components);NeoForge.EVENT_BUS.addListener(WolfArmorBalance::incoming);}
}
