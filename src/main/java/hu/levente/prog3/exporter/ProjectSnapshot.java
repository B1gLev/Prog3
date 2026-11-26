package hu.levente.prog3.exporter;

import java.time.Instant;

/**
 * A projekt pillanatállapotát reprezentáló rekord.
 *
 * A {@link ProjectJsonCodec} használja a projekt metadatainak és a nyers DSL forráskódnak a JSON formátumba
 * történő szerializálásához és visszatöltéséhez.
 *
 * @param projectName a projekt neve, nem lehet {@code NULL}
 * @param rawCode a szerkezstőben szerepló DSL forráskód
 * @param savedAt a projektállapot lérehozásának vagy mentésének ideje
 * */
public record ProjectSnapshot(String projectName, String rawCode, Instant savedAt) {}
