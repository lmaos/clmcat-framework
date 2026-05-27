package com.clmcat.basics.commons.util;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkUtilsUnitTest {

    @Test
    void shouldMovePreferredInterfaceAddressesToFront() throws Exception {
        List<String> addresses = NetworkUtils.sortAddressTexts(List.of(
                new NetworkUtils.AddressEntry(InetAddress.getByName("8.8.8.8"), false, "eth0", "WAN"),
                new NetworkUtils.AddressEntry(InetAddress.getByName("192.168.1.20"), false, "eth1", "LAN")
        ), "eth1");

        assertEquals(List.of("192.168.1.20", "8.8.8.8"), addresses);
    }

    @Test
    void shouldMatchPreferredInterfaceByAliasBaseName() throws Exception {
        List<String> addresses = NetworkUtils.sortAddressTexts(List.of(
                new NetworkUtils.AddressEntry(InetAddress.getByName("8.8.4.4"), false, "eth0", "WAN"),
                new NetworkUtils.AddressEntry(InetAddress.getByName("192.168.1.30"), false, "eth0:1", "WAN Alias")
        ), "eth0");

        assertEquals(List.of("8.8.4.4", "192.168.1.30"), addresses);
    }

    @Test
    void shouldMatchPreferredInterfaceByDisplayName() throws Exception {
        List<String> addresses = NetworkUtils.sortAddressTexts(List.of(
                new NetworkUtils.AddressEntry(InetAddress.getByName("1.1.1.1"), false, "eth0", "Primary Ethernet"),
                new NetworkUtils.AddressEntry(InetAddress.getByName("192.168.1.40"), false, "eth1", "Secondary Ethernet")
        ), "Secondary Ethernet");

        assertEquals(List.of("192.168.1.40", "1.1.1.1"), addresses);
    }
}
