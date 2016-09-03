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
package fr.evercraft.eversanctions.service.manual;

import java.net.InetAddress;

import org.spongepowered.api.text.Text;

import fr.evercraft.everapi.services.sanction.manual.SanctionManualProfile;

public class EManualProfileBanIp extends EManualProfile implements SanctionManualProfile.BanIp {
	
	private InetAddress address;
	
	public EManualProfileBanIp(final InetAddress address, final long date_start, final long date_end, final Text reason, final String source) {
		this(address, date_start, date_end, reason, source, null, null, null);
	}
	
	public EManualProfileBanIp(final InetAddress address, final long date_start, final long date_end, final Text reason, final String source, 
			final Long pardon_date, final Text pardon_reason, final String pardon_source) {
		super(date_start, date_end, reason, source, pardon_date, pardon_reason, pardon_source);
		
		this.address = address;
	}

	@Override
	public InetAddress getAddress() {
		return this.address;
	}
}
