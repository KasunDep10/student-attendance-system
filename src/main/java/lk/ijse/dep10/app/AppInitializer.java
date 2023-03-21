package lk.ijse.dep10.app;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lk.ijse.dep10.app.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                if(DBConnection.getInstance().getConnection() != null && !DBConnection.getInstance().getConnection().isClosed()) {
                    System.out.println("Database connection is about to close");
                    DBConnection.getInstance().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));

        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        generateTables();
        System.out.println("Complete");
    }

    private void generateTables() {
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement stm = connection.createStatement();

            ResultSet rst = stm.executeQuery("SHOW TABLES ");
            HashSet<String> tableSet = new HashSet<>();

            while(rst.next()){
                tableSet.add(rst.getString(1));
            }

            boolean tableExists = tableSet.containsAll(Set.of("Attendance", "Picture",
                    "Student", "User"));

            if(!tableExists) stm.execute(readDBScript());

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to create tables").showAndWait();
            e.printStackTrace();
        }
    }

    private String readDBScript(){
        try {
            InputStream is = getClass().getResourceAsStream("/schema.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder dbScript = new StringBuilder();

            while ((line = br.readLine()) != null) {
                dbScript.append(line).append("\n");
            }
            br.close();
            return dbScript.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
