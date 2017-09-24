package db.legend;

import db.GenomeDAO;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by alext on 7/8/15.
 */
public class GenomeLegend {

    private final List<GenomeLegend.GenomeLegendLine> lines;

    protected GenomeLegend(List<GenomeLegend.GenomeLegendLine> lines) {
        this.lines = lines;
    }

    public Stream<GenomeLegendLine> stream(){
        return this.lines.stream();
    }

    public List<GenomeLegendLine> getLines() {
        return lines;
    }

    public int size(){
        return this.lines.size();
    }

    public GenomeLegend.GenomeLegendLine get(int i){
        return this.lines.get(i);
    }

    public static class GenomeLegendLine {
        private final String name;
        private final int id;
        private final String comment;

        public GenomeLegendLine(String name, int id, String comment) {
            this.name = name;
            this.id = id;
            this.comment = comment;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public String getComment() {
            return comment;
        }
    }

    public static GenomeLegend get(GenomeDAO genomeDAO) throws Exception{
         return new GenomeLegend(genomeDAO.getLegend());
    }
}