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

        public LeagueTableRow(String teamName, int win, int draw, int lose, int gf, int gs) {
            this.teamName = teamName;
            this.win = win;
            this.draw = draw;
            this.lose = lose;
            this.gf = gf;
            this.gs = gs;
            this.played = win + draw + lose;
            this.point = (win*3) + draw;
            this.dg = gf - gs;
        }

        private String teamName;
        private int played;
        private int win;
        private int draw;
        private int lose;
        private int gf;
        private int gs;
        private int dg;
        private int point;

        public int getPlayed() {
            return played;
        }

        public void setPlayed(int played) {
            this.played = played;
        }

        public int getWin() {
            return win;
        }

        public void setWin(int win) {
            this.win = win;
        }

        public int getDraw() {
            return draw;
        }

        public void setDraw(int draw) {
            this.draw = draw;
        }

        public int getLose() {
            return lose;
        }

        public void setLose(int lose) {
            this.lose = lose;
        }

        public int getGf() {
            return gf;
        }

        public void setGf(int gf) {
            this.gf = gf;
        }

        public int getGs() {
            return gs;
        }

        public void setGs(int gs) {
            this.gs = gs;
        }

        public int getDg() {
            return dg;
        }

        public void setDg(int dg) {
            this.dg = dg;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            this.point = point;
        }
    }

}
