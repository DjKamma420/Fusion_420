package de.kamma.fusion420.wartung;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionspruefungTest {

	@Test
	void erkenntNeuereFassungen() {
		assertTrue(Versionspruefung.istNeuer("0.2.0", "0.1.0"));
		assertTrue(Versionspruefung.istNeuer("v1.0.0", "0.9.9"));
		assertTrue(Versionspruefung.istNeuer("0.1.1", "0.1.0"));
	}

	@Test
	void zaehltZahlenStattZeichen() {
		// Alphabetisch waere "0.10.0" kleiner als "0.9.0" — genau der Fehler,
		// den ein Zeichenkettenvergleich machen wuerde.
		assertTrue(Versionspruefung.istNeuer("0.10.0", "0.9.0"));
		assertFalse(Versionspruefung.istNeuer("0.9.0", "0.10.0"));
	}

	@Test
	void gleichstandIstNichtNeuer() {
		assertFalse(Versionspruefung.istNeuer("0.1.0", "0.1.0"));
		assertFalse(Versionspruefung.istNeuer("v0.1.0", "0.1.0"));
		assertFalse(Versionspruefung.istNeuer("0.1", "0.1.0"));
	}

	@Test
	void kommtMitUngewoehnlichenNummernZurecht() {
		assertTrue(Versionspruefung.istNeuer("1.0", "0.9.9"));
		assertFalse(Versionspruefung.istNeuer("0.1.0-rc1", "0.1.0"));
		assertFalse(Versionspruefung.istNeuer(null, "0.1.0"));
		assertFalse(Versionspruefung.istNeuer("0.1.0", null));
	}

	@Test
	void bereinigtVersionsnummern() {
		assertEquals("0.1.0", Versionspruefung.bereinige("v0.1.0"));
		assertEquals("0.1.0", Versionspruefung.bereinige("0.1.0-rc1"));
		assertEquals("1.2.3", Versionspruefung.bereinige("  V1.2.3 "));
	}
}
