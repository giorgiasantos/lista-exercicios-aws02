package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static Connection getConnection(){

        try{
            Connection connection = DriverManager.getConnection("jdbc:postgresql://estacionamento-aws.cek2nmi2tmwz.sa-east-1.rds.amazonaws.com/estacionamentodeep", "postgres", "root1234");
            return connection;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

    }
}
