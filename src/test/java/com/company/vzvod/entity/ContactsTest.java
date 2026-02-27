package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContactsTest extends EntityTestSupport {

    private Contacts contacts;

    @BeforeEach
    void setUp() {
        contacts = dataManager.create(Contacts.class);
    }

    @Test
    void testNearestMetroStation() {
        assertNull(contacts.getNearestMetroStation());

        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        assertEquals(MetroStation.BALTIYSKAYA, contacts.getNearestMetroStation());
    }

    @Test
    void testHabitation() {
        assertNull(contacts.getHabitation());

        Address address = dataManager.create(Address.class);
        contacts.setHabitation(address);
        assertSame(address, contacts.getHabitation());
    }

    @Test
    void testRegistration() {
        assertNull(contacts.getRegistration());

        Address address = dataManager.create(Address.class);
        contacts.setRegistration(address);
        assertSame(address, contacts.getRegistration());
    }

    @Test
    void setPhoneNumber() {
        assertNull(contacts.getPhoneNumber());

        String phone1 = "89513657854";
        String phone2 = "+79112291515";
        String phone3 = "9687412365";
        String phone4 = "79112261515";

        contacts.setPhoneNumber(phone1);
        assertEquals("+79513657854", contacts.getPhoneNumber());

        contacts.setPhoneNumber(phone2);
        assertEquals("+79112291515", contacts.getPhoneNumber());

        contacts.setPhoneNumber(phone3);
        assertEquals("+79687412365", contacts.getPhoneNumber());

        contacts.setPhoneNumber(phone4);
        assertEquals("+79112261515", contacts.getPhoneNumber());
    }

    @Test
    void getId() {
        UUID originalId = contacts.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        contacts.setId(newId);

        assertEquals(newId, contacts.getId());
    }
}