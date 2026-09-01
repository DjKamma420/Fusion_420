package de.kamma.fusion420;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZahlenTest {

	@Test
	void kuerztGrosseBetraege() {
		assertEquals("812", Zahlen.kurz(812.4));
		assertEquals("1,2k", Zahlen.kurz(1234.0));
		assertEquals("12k", Zahlen.kurz(12_345.0));
		assertEquals("1,5M", Zahlen.kurz(1_500_000.0));
		assertEquals("2,3B", Zahlen.kurz(2_300_000_000.0));
		assertEquals("-4,5M", Zahlen.kurz(-4_500_000.0));
	}

	@Test
	void setztVorzeichenNurBeiGewinn() {
		assertEquals("+612k", Zahlen.mitVorzeichen(612_000.0));
		assertEquals("-42k", Zahlen.mitVorzeichen(-42_000.0));
		assertEquals("0", Zahlen.mitVorzeichen(0.0));
	}

	@Test
	void schreibtProzenteMitVorzeichen() {
		assertEquals("+51%", Zahlen.prozent(0.512));
		assertEquals("-30%", Zahlen.prozent(-0.3));
	}
}
