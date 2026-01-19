package controlador;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.File;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import modelo.Libros;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import static java.lang.System.exit;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import javax.swing.*;

/**
 *
 * @author Molina
 *
 * Conecta a una base de datos MYSQL/MARIADB vía JDBC Usa modelo Libros para
 * gestionar datos
 *
 * Usa librería JasperReport para los informe
 */
public class ControladorLibros implements Initializable {

    //Objetos conexión, declaración y ResulSet de JDBC
    Connection conexion;
    Statement st;
    ResultSet rs;

    //Parámetros de informe
    Map parametros = new HashMap();
    private KeyCodeCombination ctrlI;

    @FXML
    private WebView wv;

    @FXML
    private CheckBox checkCombo;

    @FXML
    private ComboBox<Integer> toolbarCombo;

    //----------------------------
    ObservableList<Libros> listaLibros = FXCollections.observableArrayList();

    @FXML
    private Pane panelInformes;

    @FXML
    private TextField idField;

    @FXML
    private TextField tituloField;

    @FXML
    private TextField autorField;

    @FXML
    private TextField anyoField;

    @FXML
    private TextField paginasField;

    @FXML
    private TableView<Libros> TableView;

    @FXML
    private TableColumn<Libros, Integer> idColumn;

    @FXML
    private TableColumn<Libros, String> tituloColumn;

    @FXML
    private TableColumn<Libros, String> autorColumn;

    @FXML
    private TableColumn<Libros, Integer> anyoColumn;

    @FXML
    private TableColumn<Libros, Integer> paginasColumn;

    @FXML
    private TextField mititulo;

    @FXML
    private Button buttonInformeG;

    @FXML
    private Button buttonInformeI;

    @FXML
    private Button buttonInformeN;

    @FXML
    private Label labelInformes;

    @FXML
    private ComboBox<String> tipoGrafica;

    @FXML
    private void insertButton() {
        String query = "INSERT INTO libros(Id,Titulo,Autor,Anyo,Paginas) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = this.conexion.prepareStatement(query);
            preparedStatement.setInt(1, Integer.parseInt(idField.getText()));
            preparedStatement.setString(2, tituloField.getText());
            preparedStatement.setString(3, autorField.getText());
            preparedStatement.setInt(4, Integer.parseInt(anyoField.getText()));
            preparedStatement.setInt(5, Integer.parseInt(paginasField.getText()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Excepción: " + e.getMessage());
        }
        mostrarLibros();//Tenemos que volver a volcar la BBDD a la OL
    }

    @FXML
    private void updateButton() {
        String query = "UPDATE libros SET Titulo=?, Autor=?, Anyo=?, Paginas=? WHERE ID=?";
        try {
            PreparedStatement preparedStatement = this.conexion.prepareStatement(query);
            preparedStatement.setString(1, tituloField.getText());
            preparedStatement.setString(2, autorField.getText());
            preparedStatement.setInt(3, Integer.parseInt(anyoField.getText()));
            preparedStatement.setInt(4, Integer.parseInt(paginasField.getText()));
            preparedStatement.setInt(5, Integer.parseInt(idField.getText()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Excepción: " + e.getMessage());
        }
        mostrarLibros();//Tenemos que volver a volcar la BBDD a la OL
    }

    @FXML
    private void deleteButton() {
        String query = "DELETE FROM libros WHERE ID=?";
        try {
            PreparedStatement preparedStatement = this.conexion.prepareStatement(query);
            preparedStatement.setInt(1, Integer.parseInt(idField.getText()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Excepción: " + e.getMessage());
        }
        mostrarLibros();//Tenemos que volver a volcar la BBDD a la OL
    }

    public Connection getConnection() throws IOException {
        //Importante: hay que separar los datos de conexión del programa, así, al cambiar, no tendría
        //que cambiar nada internamente, o al menos, el mínimo posible.
        Properties properties = new Properties();
        String IP, PORT, BBDD, USER, PWD;
        //Se lee IP desde fuera del jar
        try {
            InputStream input_ip = new FileInputStream("ip.properties");//archivo debe estar junto al jar
            properties.load(input_ip);
            IP = (String) properties.get("IP");
        } catch (FileNotFoundException e) {
            System.out.println("No se pudo encontrar el archivo de propiedades para IP, se establece localhost por defecto");
            IP = "localhost";
        }

        InputStream input = getClass().getClassLoader().getResourceAsStream("bbdd.properties");
        if (input == null) {
            System.out.println("No se pudo encontrar el archivo de propiedades");
            return null;
        } else {
            // Cargar las propiedades desde el archivo
            properties.load(input);
            // String IP = (String) properties.get("IP"); //Tiene sentido leerlo desde fuera del Jar por si cambiamos la IP, el resto no debería de cambiar
            //ni debería ser público
            PORT = (String) properties.get("PORT");//En vez de crear con new, lo crea por asignación + casting
            BBDD = (String) properties.get("BBDD");
            USER = (String) properties.get("USER");//USER de MARIADB en LAMP 
            PWD = (String) properties.get("PWD");//PWD de MARIADB en LAMP 

            Connection conn;
            try {
                String cadconex = "jdbc:mariadb://" + IP + ":" + PORT + "/" + BBDD + " USER:" + USER + "PWD:" + PWD;
                System.out.println(cadconex);
                //Si usamos LAMP Funciona con ambos conectores
                conn = DriverManager.getConnection("jdbc:mariadb://" + IP + ":" + PORT + "/" + BBDD, USER, PWD);
                return conn;
            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Ha ocurrido un error de conexión");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                exit(0);
                return null;
            }
        }
    }

    public void mostrarLibros() {
        TableView.setItems(dameListaLibros());
    }

    public ObservableList<Libros> dameListaLibros() {
        if (conexion != null) {
            listaLibros.clear(); //Limpiamos el contenido actual
            String query = "SELECT * FROM libros";
            try {
                rs = st.executeQuery(query);
                Libros libro;
                while (rs.next()) { //Se usan los identificadores propios en la BBDD (no es case sensitive). Revisar en phpmyadmin
                    libro = new Libros(rs.getInt("Id"), rs.getString("TITULO"),
                            rs.getString("Autor"), rs.getInt("Anyo"), rs.getInt("Paginas"));//No cogemos img
                    listaLibros.add(libro);
                }
            } catch (SQLException e) {
                System.out.println("Excepción SQL: " + e.getMessage());
            }
            return listaLibros;
        }
        return null;
    }

    void CrearCarpeta() {
        try {//Carpeta "informes" dentro del directorio donde se ejecuta el programa
            Path ruta = Paths.get(System.getProperty("user.dir"), "informes");
            Files.createDirectories(ruta);
            System.out.println("Carpeta creada en: " + ruta.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Creamos la carpeta informes, para que los almacene ordenados
        CrearCarpeta();

        try {
            conexion = this.getConnection();
            if (conexion != null) {
                this.st = conexion.createStatement();
            }
        } catch (IOException | SQLException e) {
        }
        if (conexion != null) {
            ObservableList<Libros> lista = dameListaLibros();

            //Los campos han de coincidir con los campos del objeto Libros
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tituloColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
            autorColumn.setCellValueFactory(new PropertyValueFactory<>("autor"));
            anyoColumn.setCellValueFactory(new PropertyValueFactory<>("anyo"));
            paginasColumn.setCellValueFactory(new PropertyValueFactory<>("paginas"));

            TableView.setItems(lista);
        }

        Platform.runLater(() -> {
            //Accedemos al stage actual mediante cualquier nodo
            Stage primaryStage = (Stage) this.TableView.getScene().getWindow();
            primaryStage.setOnCloseRequest(event -> {
                try {
                    this.conexion.close();
                    System.out.println("Conex. a BBDD cerrada");
                } catch (SQLException ex) {
                    Logger.getLogger(ControladorLibros.class.getName()).log(Level.SEVERE, null, ex);
                }
                primaryStage.close(); // Cierra la ventana si el usuario confirma
            });
        });

        ctrlI = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);

        checkCombo.selectedProperty().addListener((observable, valorAnt, valorAct) -> {
            mititulo.setDisable(valorAct);
        });
        mititulo.setDisable(true);

        //Imágenes
        //3: desde RESOURCES, metemos un GIF animado
        Image icono = new Image(getClass().getClassLoader().getResourceAsStream("report.gif"));
        ImageView imageView = new ImageView(icono);
        labelInformes.setGraphic(imageView);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);

        //Rellenamos combo
        this.tipoGrafica.getItems().add("libros4.jasper");
        this.tipoGrafica.getItems().add("libros5.jasper");
        this.tipoGrafica.getItems().add("libros6.jasper");
        this.tipoGrafica.getItems().add("libros7.jasper");
        this.tipoGrafica.getItems().add("libros8.jasper");
        this.tipoGrafica.getItems().add("libros9.jasper");
        this.tipoGrafica.getItems().add("libros10.jasper");
        this.tipoGrafica.getSelectionModel().selectFirst();

    }

    @FXML
    //Informe Incrustado/No Incrustado Con parámetros
    void buttonInforme(ActionEvent event) {
        if (checkCombo.isSelected()) {//1 - Informe sencillo SÍ incrustado
            lanzaInforme("/reports/libros1.jasper", parametros, 0);
        } else {//2 - Informe NO incrustado usa parámetro(nueva ventana) 
            parametros.put("Parametro", "%" + mititulo.getText() + "%");
            lanzaInforme("/reports/libros2.jasper", parametros, 1);
        }
    }

    @FXML
    void buttonInformeImg(ActionEvent event) {
        if (checkCombo.isSelected()) {//1 - Informe sencillo SÍ incrustado
            lanzaInforme("/reports/libros3.jasper", parametros, 0);
        } else {//2 - Informe NO incrustado usa parámetro(nueva ventana) 
            lanzaInforme("/reports/libros3.jasper", parametros, 1);
        }
    }

    @FXML
    void buttonInformeGraf(ActionEvent event) {
        if (checkCombo.isSelected()) {//1 - Informe sencillo SÍ incrustado
            lanzaInforme("/reports/" + tipoGrafica.getSelectionModel().getSelectedItem(), parametros, 0);
        } else {//2 - Informe NO incrustado usa parámetro(nueva ventana) 
            lanzaInforme("/reports/" + tipoGrafica.getSelectionModel().getSelectedItem(), parametros, 1);
        }
    }

    @FXML
    void teclaPulsada(KeyEvent e) {
        if (ctrlI.match(e)) {
            buttonInforme(null);

        }
    }

   

private void lanzaInforme(String rutaInf, Map<String, Object> param, int tipo) {
        try {
            JasperReport report = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream(rutaInf));
            try {
                // Llena el informe con los datos de la conexión. 
                // En Jaspersoft Studio esto lo hacemos mediante los DataAdapters,
                // pero aquí, tenemos que hacerlo nosotros
                System.out.println(this.conexion);
                JasperPrint jasperPrint = JasperFillManager.fillReport(report, param, this.conexion);

                if (!jasperPrint.getPages().isEmpty()) {

                    //Exporta el informe a un archivo PDF (necesita librería)
                    //Se extrae nombre del fichero jasper para que genera en ficheros distintos
                    //Por LIMPIEZA del proyecto, hay que crear una carpeta llamada "informes"
                    String pdfOutputPath = "informes/"+rutaInf.substring(rutaInf.lastIndexOf('/')+1,rutaInf.lastIndexOf('.'))+"informe.pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, pdfOutputPath);

                    //Exporta el informe a un archivo HTML
                    //Se extrae nombre del fichero jasper para que genera en ficheros distintos
                    //Por LIMPIEZA del proyecto, hay que crear una carpeta llamada "informes"
                    String outputHtmlFile = "informes/"+rutaInf.substring(rutaInf.lastIndexOf('/')+1,rutaInf.lastIndexOf('.'))+"informe.html";
                    JasperExportManager.exportReportToHtmlFile(jasperPrint, outputHtmlFile);

                    //Crea un WebView para mostrar la versión HTML del informe
                    if (tipo == 0) {
                        wv.getEngine().load(new File(outputHtmlFile).toURI().toString());
                    } else { //tipo==1
                        WebView wvnuevo = new WebView();
                        wvnuevo.getEngine().load(new File(outputHtmlFile).toURI().toString());
                        StackPane stackPane = new StackPane(wvnuevo);
                        Scene scene = new Scene(stackPane, 600, 500);
                        Stage stage = new Stage();
                        stage.setTitle("Informe en HTML");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setResizable(true);
                        stage.setScene(scene);
                        stage.show();
                    }
                } else {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Información");
                    alert.setHeaderText("Alerta de Informe");
                    alert.setContentText("La búsqueda " + mititulo.getText() + " no generó páginas");
                    alert.showAndWait();
                }

            } catch (JRException e) {
                System.out.println(e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al generar el informe: " + e.getMessage());
            }
        } catch (JRException ex) {
            System.out.println(ex.getMessage());
        }
    }

}


