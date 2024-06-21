package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Config {
    Connection con;
    String ur = "jdbc:mysql://localhost/comercial?useSSL=false&serverTimezone=UTC";
    String uss="root";
    String pas="LticPUCE";
    public Connection Conexion(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(ur,uss,pas);
            System.out.println("Conexión exitosa");
           // con.close();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error al conectarse a la BD "+e);
        }
        return con;
    }
    
     public static void main(String[] args) {
        Config config = new Config();
        config.Conexion();
    }
}