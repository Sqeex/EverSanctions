/*
 * This file is part of EverSanctions.
 *
 * EverSanctions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverSanctions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverSanctions.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.eversanctions.command.jails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.text.ETextBuilder;
import fr.evercraft.eversanctions.ESMessage.ESMessages;
import fr.evercraft.eversanctions.command.jail.ESJail;
import fr.evercraft.eversanctions.ESPermissions;
import fr.evercraft.eversanctions.EverSanctions;
import fr.evercraft.eversanctions.service.EJail;

public class ESJailsSetRadius extends ESubCommand<EverSanctions> {
	
	public ESJailsSetRadius(final EverSanctions plugin, final ESJails command) {
        super(plugin, command, "setradius");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ESPermissions.JAILS_SETRADIUS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EChat.of(ESMessages.JAILS_SETRADIUS_DESCRIPTION.get());
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
			this.plugin.getJailService().getAll().forEach(jail -> suggests.add(jail.getName()));
		}
		return suggests;
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 1) {
			resultat = this.commandJailSetRadius((EPlayer) source, args.get(0), Optional.empty());
		} else if (args.size() == 2) {
			resultat = this.commandJailSetRadius((EPlayer) source, args.get(0), Optional.of(args.get(1)));
		// Nombre d'argument incorrect
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}
	
	private boolean commandJailSetRadius(final CommandSource staff, final String jail_name, final Optional<String> radius_string) {
		String name = EChat.fixLength(jail_name, this.plugin.getEverAPI().getConfigs().getMaxCaractere());
		
		Optional<EJail> jail = this.plugin.getJailService().getEJail(name);
		if (!jail.isPresent()) {
			staff.sendMessage(EChat.of(ESMessages.PREFIX.get() + ESMessages.JAIL_UNKNOWN.get()
					.replaceAll("<jail>", jail_name)));
			return false;
		}
		
		if (!radius_string.isPresent()) {
			return this.commandJailSetRadius(staff, jail.get()); 
		} else {
			try {
				return this.commandJailSetRadius(staff, jail.get(), Integer.parseInt(radius_string.get())); 
			} catch (NumberFormatException e) {
				staff.sendMessage(EChat.of(ESMessages.PREFIX.get() + EAMessages.IS_NOT_NUMBER.get()
						.replaceAll("<number>", radius_string.get())));
			}
		}
		return false;
	}
	
	private boolean commandJailSetRadius(final CommandSource staff, final EJail jail) {
		if (jail.update(Optional.empty())) {
			staff.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
					.append(ESMessages.JAILS_SETRADIUS_DEFAULT.get()
							.replaceAll("<radius>", String.valueOf(jail.getRadius())))
					.replace("<jail>", ESJail.getButtonJail(jail))
					.build());
			return true;
		} else {
			staff.sendMessage(EChat.of(ESMessages.PREFIX.get() + ESMessages.JAILS_SETRADIUS_CANCEL_DEFAULT.get()
				.replaceAll("<radius>", String.valueOf(jail.getRadius()))
				.replaceAll("<jail>", jail.getName())));
		}
		return false;
	}

	private boolean commandJailSetRadius(final CommandSource staff, final EJail jail, final int radius) {
		if (jail.update(Optional.of(radius))) {
			staff.sendMessage(ETextBuilder.toBuilder(ESMessages.PREFIX.get())
					.append(ESMessages.JAILS_SETRADIUS_VALUE.get()
							.replaceAll("<radius>", String.valueOf(jail.getRadius())))
					.replace("<jail>", ESJail.getButtonJail(jail))
					.build());
			return true;
		} else {
			staff.sendMessage(EChat.of(ESMessages.PREFIX.get() + ESMessages.JAILS_SETRADIUS_CANCEL_VALUE.get()
				.replaceAll("<radius>", String.valueOf(jail.getRadius()))
				.replaceAll("<jail>", jail.getName())));
		}
		return false;
	}
}