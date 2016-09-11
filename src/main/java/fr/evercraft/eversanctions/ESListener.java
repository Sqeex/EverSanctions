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
package fr.evercraft.eversanctions;

import java.util.Optional;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban.Profile;

import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.services.sanction.SanctionService;
import fr.evercraft.eversanctions.ESMessage.ESMessages;

public class ESListener {
	private EverSanctions plugin;
	
	public ESListener(final EverSanctions plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerJoin(final ClientConnectionEvent.Auth event) {
		Optional<Profile> ban = this.plugin.getSanctionService().getBanFor(event.getProfile());
		if (ban.isPresent()) {
			if(ban.get().isIndefinite()) {
				event.setMessage(EChat.of(ESMessages.CONNECTION_BAN_UNLIMITED.get()
						.replaceAll("<staff>", EChat.serialize(ban.get().getBanSource().orElse(Text.of(SanctionService.UNKNOWN))))
						.replaceAll("<reason>", EChat.serialize(ban.get().getReason().orElse(Text.EMPTY)))));
			} else {
				long time = ban.get().getExpirationDate().get().toEpochMilli();
				event.setMessage(EChat.of(ESMessages.CONNECTION_BAN_TEMP.get()
						.replaceAll("<staff>", EChat.serialize(ban.get().getBanSource().orElse(Text.of(SanctionService.UNKNOWN))))
						.replaceAll("<reason>", EChat.serialize(ban.get().getReason().orElse(Text.EMPTY)))
						.replaceAll("<duration>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDate(time))
						.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().parseTime(time))
						.replaceAll("<date>", this.plugin.getEverAPI().getManagerUtils().getDate().parseDate(time))
						.replaceAll("<datetime>", this.plugin.getEverAPI().getManagerUtils().getDate().parseDateTime(time))));
			}
			event.setMessageCancelled(false);
			event.setCancelled(true);
		}
	}
}
