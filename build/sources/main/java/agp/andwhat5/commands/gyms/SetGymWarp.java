package agp.andwhat5.commands.gyms;

import agp.andwhat5.commands.Command;
import agp.andwhat5.Utils;
import agp.andwhat5.config.structs.ArenaStruc;
import agp.andwhat5.config.structs.GymStruc;
import agp.andwhat5.config.structs.Vec3dStruc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class SetGymWarp extends Command
{

	public SetGymWarp()
	{
		super("setgymwarp", "/setgymwarp <gym> <lobby|home|arena> [(if arena) <name> <stands|challenger|leader> <opt-(-delete)>]");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP user = requireEntityPlayer(sender);
		if (args.length == 2 && !args[1].equalsIgnoreCase("arena") ||
				((args.length == 4 || args.length == 5) && args[1].equalsIgnoreCase("arena")))
		{
			if (Utils.gymExists(args[0]))
			{
				GymStruc gs = Utils.getGym(args[0]);
				Vec3d pos = user.getPositionVector();
				Vec3dStruc loc = new Vec3dStruc(pos.x, pos.y, pos.z, user.rotationPitch, user.rotationYaw);
				String lName;
				switch (args[1].toLowerCase())
				{
					case "lobby":
						gs.Lobby = loc;
						lName = "lobby";
						sender.sendMessage(Utils.toText("&7The &b" + gs.Name + " &7Gym's &b" + lName + " &7has been set to your current location!", true));
						break;
					case "arena":
						ArenaStruc as = Utils.getArena(gs, args[2]);
						if (as == null)
						{
							as = new ArenaStruc(args[2]);
							gs.Arenas.add(as);
						}
						switch (args[3].toLowerCase())
						{
							case "stands":
								as.Stands = args.length == 5 && args[4].equalsIgnoreCase("-delete") ? null : loc;
								lName = args[2] + " stands";
								break;
							case "challenger":
								as.Challenger = args.length == 5 && args[4].equalsIgnoreCase("-delete") ? null : loc;
								lName = args[2] + " challenger stage";
								break;
							case "leader":
								as.Leader = args.length == 5 && args[4].equalsIgnoreCase("-delete") ? null : loc;
								lName = args[2] + " leader stage";
								break;
							default:
								sender.sendMessage(Utils.toText("&7The location &b" + args[3] + " &7was not recognized!", true));
								if (Utils.isArenaEmpty(as))
								{
									gs.Arenas.remove(as);
								}
								return;
						}
						if (args.length == 5 && args[4].equalsIgnoreCase("-delete"))
						{
							sender.sendMessage(Utils.toText("&7The &b" + gs.Name + " &7Gym's &b" + lName + " &7has been deleted!", true));
						} else
						{
							sender.sendMessage(Utils.toText("&7The &b" + gs.Name + " &7Gym's &b" + lName + " &7has been set to your current location!", true));
						}
						if (Utils.isArenaEmpty(as))
						{
							gs.Arenas.remove(as);
							sender.sendMessage(Utils.toText("&7The &b" + as.Name + " &7Arena is empty and has been deleted!", true));
						}
						break;
					default:
						sender.sendMessage(Utils.toText("&7The location &b" + args[1] + " &7was not recognized!", true));
						return;
				}
				Utils.editGym(gs);
				Utils.saveAGPData();
			} else
			{
				sender.sendMessage(Utils.toText("&7The &b" + args[0] + " &7Gym does not exist!", true));
			}
		} else
		{
			super.sendUsage(sender);
		}
	}

	@Override
	public List<String> getAliases() {
		return Lists.newArrayList("sgw", "setgwarp");
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, Utils.getGymNames(true));
		} else if (args.length == 2)
		{
			return getListOfStringsMatchingLastWord(args, Arrays.asList("arena", "lobby"));
		} else if (args.length == 3 && args[1].equalsIgnoreCase("arena") && Utils.gymExists(args[0]))
		{
			return getListOfStringsMatchingLastWord(args, Utils.getArenaNames(Utils.getGym(args[0]), true));
		} else if (args.length == 4)
		{
			return getListOfStringsMatchingLastWord(args, Arrays.asList("challenger", "leader", "stands"));
		} else if (args.length == 5)
		{
			return getListOfStringsMatchingLastWord(args, Arrays.asList("-delete"));
		}
		return null;
	}
}