package br.com.comcet.tp6;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FrontendSmokeTest {

    @Test
    void testaProgramaValido() {
        String codigo = "program p; var x: integer; begin x := 10; end.";

        Compiler compiler = new Compiler();

        assertDoesNotThrow(() -> {
            compiler.compileFrontendOnly(codigo);
        });
    }

    @Test
    void testaErroSintatico() {
        // Falta o ponto-e-vírgula depois de 'p'
        String codigo = "program p var x: integer; begin x := 10; end.";

        Compiler compiler = new Compiler();

        assertThrows(Exception.class, () -> {
            compiler.compileFrontendOnly(codigo);
        });
    }

    @Test
    void testaErroSemanticoVariavelNaoDeclarada() {
        // Uso de variável 'y' que não foi declarada
        String codigo = "program p; var x: integer; begin y := 10; end.";

        Compiler compiler = new Compiler();

        assertThrows(Exception.class, () -> {
            compiler.compileFrontendOnly(codigo);
        });
    }

    @Test
    void testaProgramaComFuncaoValida() {
        String codigo = "program p; var x: integer; "
                + "function dobro(n: integer): integer; "
                + "begin dobro := n * 2; end; "
                + "begin x := dobro(5); end.";

        Compiler compiler = new Compiler();

        assertDoesNotThrow(() -> {
            compiler.compileFrontendOnly(codigo);
        });
    }

    @Test
    void testaErroTipoIncompativel() {
        String codigo = "program p; var x: integer; begin x := 10 + true; end.";

        Compiler compiler = new Compiler();

        assertThrows(Exception.class, () -> {
            compiler.compileFrontendOnly(codigo);
        });
    }
}