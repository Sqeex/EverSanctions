package fr.evercraft.eversanctions.command.jail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.text.ETextBuilder;
import fr.evercraft.eversanctions.ESMessage.ESMessages;
import fr.evercraft.eversanctions.ESPermissions;
import fr.evercraft.eversanctions.EverSanctions;
import fr.evercraft.eversanctions.service.EJail;

public class ESJailsAdd extends ESubCommand<EverSanctions> {
	
	public ESJailsAdd(final EverSanctions plugin, final ESJails command) {
        super(plugin, command, "add");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ESPermissions.JAILS_ADD.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EChat.of(ESMessages.JAILS_ADD_DESCRIPTION.get());
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_JAIL.get() + "> [" + EAMessages.ARGS_RADIUS.get() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		List<String> suggests = new ArrayList<String>();
		if (args.size() == 1) {
			suggests.add("...");
		} else if (args.size() == 2) {
			suggests.add(String.valueOf(this.plugin.getConfigs().getJailRadius()));
		}
		return suggests;
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 1) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				resultat = this.commandJailSet((EPlayer) source, args.get(0)); 
			// La source n'est pas un joueur
			} else {
				source.sendMessage(ESMessages.PREFIX.getText().concat(EAMessages.COMMAND_ERROR_FOR_PLAYER.getText()));
			}
		} else if (args.size() == 2) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				try {
					resultat = this.commandJailSet((EPlayer) source, args.get(0), Integer.parseInt(args.get(1))); 
				} catch (NumberFormatException e) {
					source.sendMessage(EChat.of(ESMessages.PREFIX.get() + EAMessages.IS_NOT_NUMBER.get()
							.replaceAll("<number>", args.get(1))));
				}
			// La source n'est pas un joueur
			} else {
				source.sendMessage(ESMessages.PREFIX.getText().concat(EAMessages.COMMAND_ERROR_FOR_PLAYER.getText()));
			}
		// Nombre d'argument incorrect
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}
	
	private boolean commandJailSet(final EPlayer player, final String jail_name) {
		String name = EChat.fixLength(jail_name, this.plugin.getEverAPI().getConfigs().getMaxCaractere());
		
		Optional<EJail> jail = this.plugin.getJailService().getEJail(name);
		if (jail.isPresent()) {
			if (jail.get().update(player.getTransform())) {
				player.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
						.append(ESMessages.JAILS_ADD_REPLACE.get())
						.replace("<jail>", this.getButtonJail(name, player.getLocation()))
						.build());
				return true;
			} else {
				player.sendMessage(ESMessages.PREFIX.get() + ESMessages.JAILS_ADD_REPLACE_CANCEL.get().replaceAll("<jail>", name));
			}
		} else {
			if (this.plugin.getJailService().add(name, player.getTransform(), Optional.empty())) {
				player.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
						.append(ESMessages.JAILS_ADD_NEW.get())
						.replace("<jail>", this.getButtonJail(name, player.getLocation()))
						.build());
				return true;
			} else {
				player.sendMessage(ESMessages.PREFIX.get() + ESMessages.JAILS_ADD_NEW_CANCEL.get().replaceAll("<jail>", name));
			}
		}
		return false;
	}

	private boolean commandJailSet(final EPlayer player, final String jail_name, final int radius) {
		String name = EChat.fixLength(jail_name, this.plugin.getEverAPI().getConfigs().getMaxCaractere());
		
		Optional<EJail> jail = this.plugin.getJailService().getEJail(name);
		if (jail.isPresent()) {
			if (jail.get().update(player.getTransform(), Optional.of(radius))) {
				player.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
						.append(ESMessages.JAILS_ADD_REPLACE.get())
						.replace("<jail>", this.getButtonJail(name, player.getLocation()))
						.build());
				return true;
			} else {
				player.sendMessage(ESMessages.PREFIX.get() + ESMessages.JAILS_ADD_REPLACE_CANCEL.get().replaceAll("<jail>", name));
			}
		} else {
			if (this.plugin.getJailService().add(name, player.getTransform(), Optional.of(radius))) {
				player.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
						.append(ESMessages.JAILS_ADD_NEW.get())
						.replace("<jail>", this.getButtonJail(name, player.getLocation()))
						.build());
				return true;
			} else {
				player.sendMessage(ESMessages.PREFIX.get() + ESMessages.JAILS_ADD_NEW_CANCEL.get().replaceAll("<jail>", name));
			}
		}
		return false;
	}

	private Text getButtonJail(final String name, final Location<World> location){
		return EChat.of(ESMessages.JAIL_NAME.get().replaceAll("<name>", name)).toBuilder()
					.onHover(TextActions.showText(EChat.of(ESMessages.JAIL_NAME_HOVER.get()
							.replaceAll("<jail>", name)
							.replaceAll("<world>", location.getExtent().getName())
							.replaceAll("<x>", String.valueOf(location.getBlockX()))
							.replaceAll("<y>", String.valueOf(location.getBlockY()))
							.replaceAll("<z>", String.valueOf(location.getBlockZ())))))
					.build();
	}
}