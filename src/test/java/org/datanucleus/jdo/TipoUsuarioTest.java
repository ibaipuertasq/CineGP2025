package org.datanucleus.jdo;

import static org.junit.Assert.*;

import org.junit.Test;

import es.deusto.spq.server.jdo.TipoUsuario;

public class TipoUsuarioTest {

    @Test
    public void testEnumValues() {
        assertEquals(3, TipoUsuario.values().length);
        assertEquals(TipoUsuario.USUARIO, TipoUsuario.valueOf("USUARIO"));
        assertEquals(TipoUsuario.CLIENTE, TipoUsuario.valueOf("CLIENTE"));
        assertEquals(TipoUsuario.ADMINISTRADOR, TipoUsuario.valueOf("ADMINISTRADOR"));
    }
}