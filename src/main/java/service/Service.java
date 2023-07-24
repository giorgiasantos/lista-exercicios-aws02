package service;

import valores.Valores;
import java.sql.*;
import java.time.LocalTime;
import static conexao.Conexao.getConnection;

public class Service {

    //ATRIBUTOS
    private Statement statement;

    //CONSTRUCTOR
    public Service() {
        try{
            statement =getConnection().createStatement();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    //MÉTODOS

    // método para cadastrar novo carro no sistema
    public void cadastrarCarro(String nomeDono, String marca, String placa, Boolean estado){

        String sql = "INSERT INTO tb_carro (nomedono, marcacarro, placa, estado, usuario) VALUES ('" +
                nomeDono + "','"  +  marca+ "','"  +  placa + "','"  +  estado + "','giorgia')";

        try {
            statement.executeUpdate(sql);
            System.out.println(nomeDono + ", o seu carro foi cadastrado com sucesso!");
        }catch (SQLException e){
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------------------------------------");

    }

    // método para exibir todos os carros cadastrados
    public void exibirCarrosCadastrados(){
        String sql = "select * from tb_carro";

        try{
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println("-----TODOS OS CARROS CADASTRADOS NO SISTEMA-----");
            while(resultSet.next()){
                System.out.println("ID: " + resultSet.getInt("id") + " | NOME DO DONO: " + resultSet.getString("nomedono") + " | MARCA DO CARRO: " + resultSet.getString("marcacarro") + " | PLACA: " + resultSet.getString("placa") + " | STATUS DE ESTACIONAMENTO: " + resultSet.getBoolean("estado"));
            }
            System.out.println("--------------------------------------------------------------------------------------");

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void exibirCarrosEstacionados(){
        String sql = "select * from tb_carro where estado = 'true'";

        try{
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println("-----CARROS ESTACIONADOS-----");
            while(resultSet.next()){
                System.out.println("ID: " + resultSet.getInt("id") + " | NOME DO DONO: " + resultSet.getString("nomedono") + " | MARCA DO CARRO: " + resultSet.getString("marcacarro") + " | PLACA: " + resultSet.getString("placa") + " | STATUS DE ESTACIONAMENTO: " + resultSet.getBoolean("estado"));
            }
            System.out.println("--------------------------------------------------------------------------------------");

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    // inserir nova entrada de carro no estacionamento
    public void inserirEntradaNoEstacionamento(int idCarro, String entrada){

        LocalTime localTime = LocalTime.parse(entrada);
        String sql = "INSERT INTO tb_estacionamento (carroid, entrada) VALUES ('" + idCarro +"', '" + entrada +"')";

        try{
            statement.executeUpdate(sql);
            System.out.println("O carro de ID " + idCarro + " deu entrada no estacionamento com sucesso!");
        }catch(SQLException e){
            e.printStackTrace();
        }

    }

    // método para exibir todas as entradas registradas
    public void exibirTodasAsEntradas(){
        String sql = "select * from tb_estacionamento";

        try{
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println("ENTRADAS DE CARROS NO ESTACIONAMENTO");
            while(resultSet.next()){
                System.out.println("ID: " + resultSet.getInt("id") + " | ID DO CARRO: " + resultSet.getInt("carroid") + " | HORA DE ENTRADA: " + resultSet.getString("entrada") + " | HORA DE SAÍDA: " + resultSet.getString("saida") + " | VALOR PAGO: " + resultSet.getDouble("valorpago") + " | TEMPO DE PERMANÊNCIA: " + resultSet.getDouble("permanencia"));
            }
            System.out.println("--------------------------------------------------------------------------------------");

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    // método para atualizar horário de permanência
    public void atualizarHoraDePermanencia(int id, String horaNova){
        String sql = "update tb_estacionamento set saida = '" + horaNova+"' where id = '" + id + "'";
        try {
            statement.executeUpdate(sql);
            System.out.println("Você registrou um novo horário de permanência para o carro de ID " + id+" com sucesso!");
            calcularValorTotal(id);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    // método para calcular total de tempo de permanencia
    public double calcularTempoDePermanencia(int id) {

        String sql = "select entrada, saida from tb_estacionamento where id = '" + id + "'";
//        listarEntradas();
        double tempoPermanenciaConvertido = 0;
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            tempoPermanenciaConvertido = 0;
            if (resultSet.next()) {

                //convertendo horarios para localtime
                LocalTime entradaConvertida = LocalTime.parse(resultSet.getString("entrada"));
                LocalTime saidaConvertida = LocalTime.parse(resultSet.getString("saida"));

                // calculando tempo de permanencia
                LocalTime tempoDePermanencia = saidaConvertida.minusHours(entradaConvertida.getHour()).minusMinutes(entradaConvertida.getMinute()).minusSeconds(entradaConvertida.getSecond());

                // convertendo tempo de permanencia para double
                tempoPermanenciaConvertido = ((double) tempoDePermanencia.getHour() * 60.0) + (double) tempoDePermanencia.getMinute();

                // atualizando o tempo de permanência na tabela
                String sqlPermanencia = "update tb_estacionamento set permanencia = '" + tempoPermanenciaConvertido + "' where id = '" + id + "'";
                statement.executeUpdate(sqlPermanencia);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tempoPermanenciaConvertido;
    }

    // método para calcular valor total a ser pago
    public void calcularValorTotal(int id){
        Valores valores = new Valores();

        double tempoTotal = calcularTempoDePermanencia(id);
        double valorAPagar = 0;

        if(tempoTotal <= 60.0) {
            valorAPagar = valores.getValorHora();
        } else if (tempoTotal > 60.0 && tempoTotal < 720) {
            tempoTotal = (tempoTotal - 60.0) / 30.0;
            valorAPagar = (tempoTotal * valores.getValorAdicional())+ valores.getValorHora();
        }else if(tempoTotal == 720.0){
            valorAPagar = valores.getValorMeioPeriodo();
        }else {
            System.out.println("ERRO!");
        }

        String sql = "update tb_estacionamento set valorpago = '" + valorAPagar +"' where id = '"+id+"'";

        try {
            statement.executeUpdate(sql);
            System.out.println("O total a ser pago pelo carro de ID " + id + " é de R$" + valorAPagar);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    // registrar nova saída de veículo do estacionamento
    public void registrarSaidaDoCarro(int id){
        String sql = "update tb_carro set estado = 'false' where id = '" + id + "'";
        try {
            statement.executeUpdate(sql);
            System.out.println("O carro de id " + id + " saiu do estacionamento. Até a próxima!");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

}
