package biblioteca.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.ImageIcon;

public class GerenciarCapas {
	private static final File DIRETORIO_APP = new File(System.getProperty("user.dir"));
    
    public static Path getPastaUploads() throws IOException {
        Path pasta = DIRETORIO_APP.toPath().resolve("upload");

        Files.createDirectories(pasta);

        return pasta;
    }
    
    public static ImageIcon carregarCapa(String nomeCapa) {

        // Procura na pasta externa uploads
        if (nomeCapa != null && !nomeCapa.isBlank()) {

            File arquivo = getCapa(nomeCapa);

            if (arquivo.isFile()) {
                return new ImageIcon(arquivo.getAbsolutePath());
            }

            // Procura dentro do classpath (Eclipse ou JAR)
            URL recurso = GerenciarCapas.class.getResource(
                "/" + nomeCapa
            );

            if (recurso != null) {
                return new ImageIcon(recurso);
            }
        }

        // Capa padrão
        URL padrao = GerenciarCapas.class.getResource(
            "/default.jpg"
        );

        if (padrao != null) {
            return new ImageIcon(padrao);
        }

        throw new IllegalStateException(
            "Imagem padrão não encontrada!"
        );
    }

    public static File getDiretorioUploads() {
        File uploads = new File(DIRETORIO_APP, "upload");

        if (!uploads.exists()) {
            uploads.mkdirs();
        }

        return uploads;
    }

    public static File getCapa(String nomeArquivo) {
        try {
			return new File(getPastaUploads().toFile(), nomeArquivo);
		} catch (IOException e) {
			return null;
		}
    }
}
