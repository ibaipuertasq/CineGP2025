package org.datanucleus.jdo;

import static org.junit.Assert.*;

import org.datanucleus.categories.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import es.deusto.spq.server.jdo.TipoUsuario;

@Category(UnitTest.class)
public class TipoUsuarioTest {

    @Test
    public void testEnumValues() {
        assertEquals(3, TipoUsuario.values().length);
        assertEquals(TipoUsuario.USUARIO, TipoUsuario.valueOf("USUARIO"));
        assertEquals(TipoUsuario.CLIENTE, TipoUsuario.valueOf("CLIENTE"));
        assertEquals(TipoUsuario.ADMINISTRADOR, TipoUsuario.valueOf("ADMINISTRADOR"));
    }
}