package modelo;

public class Libros {

    private final int id;
    private final String titulo;
    private final String autor;
    private final int anyo;
    private final int paginas;

    public Libros(int id, String titulo, String autor, int anyo, int paginas) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anyo = anyo;
        this.paginas = paginas;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getAnyo() {
        return anyo;
    }

    public int getPaginas() {
        return paginas;
    }

    

   
}
