package biblioteca.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import biblioteca.exception.ItemDuplicadoException;
import biblioteca.exception.RegistroNaoEncontradoException;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.material.TipoPeriodico;

public class MaterialRepositorio implements Repositorio<MaterialBibliografico>, RepositorioRemovivel<MaterialBibliografico> {

	private final ArrayList<MaterialBibliografico> materiais = new ArrayList<>();
	
	public final static String uploadsDirectory = Path.of("src", "biblioteca", "upload").toString();
	
	@Override
    public void adicionar(MaterialBibliografico material) throws ItemDuplicadoException {

        for (MaterialBibliografico m : materiais) {
            if (m.getId() == material.getId()) {
                throw new ItemDuplicadoException("material");
            }
        }
        materiais.add(material);
    }
	
	@Override
    public MaterialBibliografico buscarPorId(int materialId) throws RegistroNaoEncontradoException {

        for (MaterialBibliografico material : materiais) {
            if (material.getId() == materialId) {
                return material;
            }
        }

        throw new RegistroNaoEncontradoException();
    }

    public List<MaterialBibliografico> listarPorTitulo(String titulo) {

        List<MaterialBibliografico> resultado = new ArrayList<>();

        if (titulo == null || titulo.isBlank()) {
            return List.of();
        }
        for (MaterialBibliografico material : materiais) {
            if (material.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                resultado.add(material);
            }
        }

        return resultado;
    }
	
	@Override
    public List<MaterialBibliografico> listarTodos() {
        return new ArrayList<>(materiais);
    }

    @Override
    public void atualizar(MaterialBibliografico material) throws RegistroNaoEncontradoException {

        for (int i = 0; i < materiais.size(); i++) {

            if (materiais.get(i).getId() == material.getId()) {
                materiais.set(i, material);
                return;
            }
        }

        throw new RegistroNaoEncontradoException();
    }
    
    @Override
    public void remover(int materialId) throws RegistroNaoEncontradoException {

        MaterialBibliografico material = buscarPorId(materialId);
        materiais.remove(material);
    }
    
    public void cadastrarMateriaisIniciais() {
    	Livro livro = new Livro("Clean Code", 2008, "Robert C. Martin", "9780132350884", "Prentice Hall", Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg"));
    	Periodico revista = new Periodico("Revista Ciência Hoje", 2024, "SBPC", 395, "21/07/2024", TipoPeriodico.REVISTA, Path.of(MaterialRepositorio.uploadsDirectory, "revista_ciencia_hoje.jpg"));
    	Periodico jornal = new Periodico("Jornal da Cidade", 2025, "Editora Local", 120, "23/01/2025", TipoPeriodico.JORNAL, Path.of(MaterialRepositorio.uploadsDirectory, "jornal_da_cidade.jpg"));
    	try {
			this.adicionar(livro);
		} catch (ItemDuplicadoException e) {
			
		}
    	try {
			this.adicionar(revista);
		} catch (ItemDuplicadoException e) {
			
		}
    	try {
			this.adicionar(jornal);
		} catch (ItemDuplicadoException e) {
			
		}
    }
}
