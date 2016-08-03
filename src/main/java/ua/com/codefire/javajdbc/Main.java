/*
 * Copyright (C) 2016 CodeFireUA <edu@codefire.com.ua>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ua.com.codefire.javajdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author CodeFireUA <edu@codefire.com.ua>
 */
public class Main {

    public static void main(String[] args) {
        // DataAccessObject
        String connectionString = "jdbc:sqlite:database.sl3";

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            System.out.println("DB CONNECTED");

            // ::: CREATION
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS messages");
                
                StringBuilder sb = new StringBuilder("CREATE TABLE messages");
                sb.append("(")
                        .append("id INTEGER PRIMARY KEY AUTOINCREMENT").append(",")
                        .append("sender TEXT NOT NULL").append(",")
                        .append("recipient TEXT NOT NULL").append(",")
                        .append("message TEXT NOT NULL").append(",")
                        .append("timestamp INTEGER NOT NULL").append(",")
                        .append("read INTEGER NOT NULL")
                        .append(")");

                // EXECUTE QUERY
                stmt.execute(sb.toString());
            }

            // ::: INSERTING
            String insertQuery = "INSERT INTO messages VALUES (NULL, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                String sender = "192.168.1.100";
                String recipient = "192.168.1.105";
                for (int i = 1; i < 6; i++) {
                    stmt.setString(1, i % 2 != 0 ? sender : recipient);
                    stmt.setString(2, i % 2 != 0 ? recipient : sender);
                    stmt.setString(3, "Hello World!");
                    stmt.setLong(4, new Date().getTime());
                    stmt.setBoolean(5, i % 2 != 0);
                    // EXECUTE PREPARED INSERT QUERY
                    stmt.executeUpdate();
                }
            }

            // ::: SELECTION
            try (Statement stmt = conn.createStatement()) {
                String selectQuery = "SELECT * FROM messages";
                
                ResultSet rs = stmt.executeQuery(selectQuery);
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String sender = rs.getString(2);
                    String recipient = rs.getString(3);
                    String message = rs.getString("message");
                    boolean read = rs.getBoolean("read");
                    Date ts = new Date(rs.getLong("timestamp"));
                    
                    System.out.printf("#%d =%s= (%s -> %s) \"%s\" [%s]\n", id, ts, sender, recipient,
                            message, read);
                }
            }
            
            // ::: UPDATING
            try (Statement stmt = conn.createStatement()) {
                stmt.addBatch("UPDATE messages SET sender = 'ME' WHERE sender LIKE '%1.100'");
                stmt.addBatch("UPDATE messages SET recipient = 'ME' WHERE recipient LIKE '%1.100'");
                int[] affteedRows = stmt.executeBatch();
                System.out.println("Changed: " + Arrays.toString(affteedRows));
            }

            // ::: SELECTION
            try (Statement stmt = conn.createStatement()) {
                String selectQuery = "SELECT * FROM messages";
                
                ResultSet rs = stmt.executeQuery(selectQuery);
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String sender = rs.getString(2);
                    String recipient = rs.getString(3);
                    String message = rs.getString("message");
                    boolean read = rs.getBoolean("read");
                    Date ts = new Date(rs.getLong("timestamp"));
                    
                    System.out.printf("#%d =%s= (%s -> %s) \"%s\" [%s]\n", id, ts, sender, recipient,
                            message, read);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
