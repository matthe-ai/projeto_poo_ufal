package biblioteca.view.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class FiltroData extends DocumentFilter {
	
	@Override
    public void insertString(
            FilterBypass fb,
            int offset,
            String texto,
            AttributeSet attr
    ) throws BadLocationException {

        replace(fb, offset, 0, texto, attr);
    }

    @Override
    public void replace(
            FilterBypass fb,
            int offset,
            int length,
            String texto,
            AttributeSet attr
    ) throws BadLocationException {

        String atual = fb.getDocument().getText(
            0, fb.getDocument().getLength()
        );

        // Converte a posição do cursor para a posição sem barras
        int posicao = atual.substring(0, offset)
                .replaceAll("[^0-9]", "").length();

        // Remove caracteres não numéricos
        String numeros = atual.replaceAll("[^0-9]", "");
        String novosNumeros = texto.replaceAll("[^0-9]", "");

        // Calcula quantos números serão removidos
        int inicio = posicao;
        int fim = inicio + atual.substring(offset, offset + length)
                .replaceAll("[^0-9]", "").length();

        StringBuilder resultado = new StringBuilder(numeros);

        resultado.replace(inicio, fim, novosNumeros);

        // Limita a 8 dígitos
        if (resultado.length() > 8) {
            resultado.setLength(8);
        }

        // Formata dd/mm/aaaa
        StringBuilder formatado = new StringBuilder();

        for (int i = 0; i < resultado.length(); i++) {

            if (i == 2 || i == 4) {
                formatado.append("/");
            }

            formatado.append(resultado.charAt(i));
        }

        fb.replace(
            0,
            fb.getDocument().getLength(),
            formatado.toString(),
            attr
        );
    }
}
