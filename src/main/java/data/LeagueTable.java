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
        private String teamName;
        private int idteam;
        private int point;

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public int getIdteam() {
            return idteam;
        }

        public void setIdteam(int idteam) {
            this.idteam = idteam;
        }

        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            this.point = point;
        }

        

        @Override
        public String toString() {
            return "LeagueTableRow{" + "idteam=" + idteam + ", point=" + point + '}';
        }

        
        
    }

}
