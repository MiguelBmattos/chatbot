package application.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import application.db.DB;
import application.db.DbException;
import application.model.entities.Messages;
import application.model.enums.Tipo;

public class MessagesDao {

    private Connection conn;

    // Construtor com conexão passada de fora (para usar no WebhookController)
    public MessagesDao(Connection conn) {
        this.conn = conn;
    }
    
    public MessagesDao() {
        this.conn = DB.getConnection();
    }

    public void insert(Messages msg) {
        String sql = "INSERT INTO messages (telefone, mensagem, tipo, data_hora) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, msg.getTelefone());
            st.setString(2, msg.getMensagem());
            st.setString(3, msg.getTipo().toString());
            st.setTimestamp(4, Timestamp.valueOf(msg.getDataHora()));

            st.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Erro ao inserir mensagem: " + e.getMessage());
        }
    }

    public List<Messages> findByTelefone(String telefone) {
        List<Messages> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE telefone = ? ORDER BY data_hora";

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, telefone);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Messages msg = new Messages();
                    msg.setId(rs.getInt("id"));
                    msg.setTelefone(rs.getString("telefone"));
                    msg.setMensagem(rs.getString("mensagem"));
                    msg.setTipo(Tipo.valueOf(rs.getString("tipo")));
                    msg.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());

                    list.add(msg);
                }
            }

        } catch (SQLException e) {
            throw new DbException("Erro ao buscar mensagens: " + e.getMessage());
        }

        return list;
    }
}
