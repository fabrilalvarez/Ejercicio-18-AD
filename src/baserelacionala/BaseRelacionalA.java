package baserelacionala;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author oracle
 */
public class BaseRelacionalA {

    private static Connection conn;
    private static String user = "hr",
            pass = "hr",
            driver = "jdbc:oracle:thin:",
            host = "localhost.localdomain",
            porto = "1521", sid = "orcl";
    private static String url = driver + user + "/" + pass + "@" + host + ":" + porto + ":" + sid;

    private static ResultSet respuestaConsulta;
    private static ResultSetMetaData metaDatos;
    private static String[][] datosDevueltos;
    private static String[] nombresColumnas;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        // TODO code application logic here
        conexion();
        do {
            System.out.println("Muy bien, esto funciona así:\n"
                    + "- Para Crear la tabla Productos pulsa 0\n"
                    + "- Para introducir datos por defecto pulsa 1\n"
                    + "- Para modificar una fila de la tabla Productos pulsa 2\n"
                    + "- Para ver los datos que contiene la tabla Productos pulsa 3\n"
                    + "- Para BORRAR la tabla Productos pulsa 4\n"
                    + "- Si quieres irte pulsa cualquier otra tecla\n");
            Scanner sc = new Scanner(System.in);
            String dato = sc.next();
            switch (dato) {
                case "0":
                    crearTablaProductos();
                    break;
                case "1":
                    aniadirProducto("p1", "parafusos", 3);
                    aniadirProducto("p2", "cravos", 4);
                    aniadirProducto("p3", "tachas", 6);
                    break;
                case "2":
                    actualizarProducto(2, "p1", "10");
                    break;
                case "3":
                    listaProducto("productos");
                    break;
                case "4":
                    borrarTablaProductos();
                    break;
                default:
                    cerrarConexion();
                    break;
            }
        } while (conn.isClosed() == false);

    }

    public static void conexion() throws SQLException {
        /*
         * Para conectar con native protocal all java driver: creamos un objeto 
         * Connection usando el metodo getConnection de la clase DriverManager
         */
        conn = DriverManager.getConnection(url);
        conn.setAutoCommit(true);
        System.out.println("Conexión establecida correctamente.");
    }

    public static void cerrarConexion() {
        try {
            conn.close();
            System.out.println("Conexión cerrada");
        } catch (SQLException ex) {
            Logger.getLogger(BaseRelacionalA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void aniadirProducto(String cod, String descripcion, int precio) {
        String producto = "INSERT INTO productos VALUES('" + cod + "','" + descripcion + "','" + precio + "')";
        try {
            Statement stat = conn.createStatement();
            stat.execute(producto);
        } catch (Exception e) {
            System.out.println("aniadirProducto ALGO PASA " + e.getMessage().toString() + " " + producto);
        }
    }

    public static void listaProducto(String tabla) {
        String busqueda = "SELECT * FROM " + tabla;
        try {
            // Se Instancia.
            Statement sentencia = (Statement) conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            // Se ejecuta la consulta y devuelve el resultSet.
            respuestaConsulta = sentencia.executeQuery(busqueda);
            // Se obtienen los metadatos del Resultset.
            metaDatos = respuestaConsulta.getMetaData();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        int columnas = 0, filas = 0;
        //DAME COLUMNAS
        try {
            // Devuelve el numero de columnas.
            columnas = metaDatos.getColumnCount();
            nombresColumnas = new String[columnas];

            for (int i = 0; i < columnas; i++) {
                // Devuelve el nombre
                nombresColumnas[i] = metaDatos.getColumnLabel(i + 1);
                System.out.print(nombresColumnas[i] + "\t");
            }
            System.out.println("\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //DAME DATOS DEVUELTOS
        try {
            // Numero de columnas.
            columnas = metaDatos.getColumnCount();
            // Se recorre el cursor hasta la ultima fila del resultSet.
            respuestaConsulta.last();
            // Numero de filas.
            filas = respuestaConsulta.getRow();
            // Rellenamos el Array.
            datosDevueltos = new String[filas][columnas];
            // Recorremos el cursor antes de la primera fila.
            respuestaConsulta.beforeFirst();
            for (int i = 0; i < filas; i++) {
                // Siguiente fila.
                respuestaConsulta.next();
                for (int j = 0; j < columnas; j++) {
                    // Columna actual
                    datosDevueltos[i][j] = respuestaConsulta.getString(j + 1);
                    System.out.print(datosDevueltos[i][j] + "\t");
                }
                System.out.println("\n");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void actualizarProducto(int posicion, String codigo, String datoNuevo) {
        String producto = "";
        try {
            Statement stat = conn.createStatement();
            if (posicion == 0) {
                producto = "UPDATE productos set cod=" + datoNuevo + " where cod='" + codigo + "'";
                stat.executeQuery(producto);
            } else if (posicion == 1) {
                producto = "UPDATE productos set set descripcion='" + datoNuevo + "' where cod='" + codigo + "'";
                stat.executeQuery(producto);
            } else if (posicion == 2) {
                producto = "UPDATE productos set precio=" + Integer.valueOf(datoNuevo) + " where cod='" + codigo + "'";
                stat.executeQuery(producto);
            }
        } catch (Exception e) {
            System.out.println("actualizarProducto ALGO PASA " + e.getMessage().toString() + " " + producto);
        }
    }

    public static void borrarTablaProductos() {
        String borrarTab = "DROP TABLE productos";
        try {
            Statement stat = conn.createStatement();
            stat.execute(borrarTab);
            System.out.println("TABLA productos BORRADA");
        } catch (Exception e) {
            System.out.println("borrarTablaProductos ALGO PASA " + e.getMessage().toString());
        }
    }

    public static void crearTablaProductos() {
        String crearTab = "CREATE TABLE productos (cod varchar2(3) primary key, descripcion varchar2 (15), precio integer)";
        try {
            Statement stat = conn.createStatement();
            stat.execute(crearTab);
            System.out.println("TABLA productos CREADA");
        } catch (Exception e) {
            System.out.println("crearTablaProductos ALGO PASA " + e.getMessage().toString());
        }

    }

}
