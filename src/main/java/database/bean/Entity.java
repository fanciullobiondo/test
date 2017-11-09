/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class Entity {

    public <T extends Entity> T fromResultSet(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Extend me!");
    }

    public <T extends Entity> QueryBuilder<T> getBuilder() {
        throw new UnsupportedOperationException("Extend me!");
    };
}
