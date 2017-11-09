/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nicolo.boschi
 */
public class LeagueTable {

    List<LeagueTableRow> rows = new ArrayList<>();

    public List<LeagueTableRow> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return rows.toString();
    }

    

    public void setRows(List<LeagueTableRow> rows) {
        this.rows = rows;
    }

    public static class LeagueTableRow {

        public LeagueTableRow(int idteam, int point) {
            this.idteam = idteam;
            this.point = point;
        }

        int idteam;
        int point;

        @Override
        public String toString() {
            return "LeagueTableRow{" + "idteam=" + idteam + ", point=" + point + '}';
        }

        
        
    }

}
