package com.neo.util.common.impl.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class JsonUtilTest {

    protected static final String BASIC_ADDRESS = "{\"city\":\"Baden\",\"street\":\"Bahnhofstrasse\",\"streetNumber\":1}";

    @Test
    void javaPojoToJsonTest() {
        // Arrange
        Address address = createAddress();

        // Act
        String addressAsJson = JsonUtil.toJson(address);

        // Assert
        Assertions.assertNotNull(addressAsJson);
        Assertions.assertEquals(BASIC_ADDRESS, addressAsJson);

    }

    @Test
    void fromJsonTest() {
        // Arrange
        Address address = createAddress();

        // Act
        Address deserializedAddress = JsonUtil.fromJson(BASIC_ADDRESS, Address.class);

        // Assert
        Assertions.assertNotNull(deserializedAddress);
        Assertions.assertEquals(address, deserializedAddress);
    }

    @Test
    void testSerializationNull() {
        String pojoString = JsonUtil.toJson(null);

        // no exception should be thrown
        Assertions.assertEquals("null", pojoString);
    }

    public Address createAddress() {
        return new Address("Baden","Bahnhofstrasse",1);
    }

    public static class Address {
        protected String city;
        protected String street;
        protected int streetNumber;

        /**
         * Empty constructor for Jackson
         */
        public Address() {}

        public Address(String city, String street, int streetNumber) {
            this.city = city;
            this.street = street;
            this.streetNumber = streetNumber;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public int getStreetNumber() {
            return streetNumber;
        }

        public void setStreetNumber(int streetNumber) {
            this.streetNumber = streetNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Address address = (Address) o;
            return streetNumber == address.streetNumber && Objects.equals(city, address.city) && Objects.equals(street,
                    address.street);
        }

        @Override public int hashCode() {
            return Objects.hash(city, street, streetNumber);
        }
    }
}
