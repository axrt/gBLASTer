package gblaster.blast;

import blast.blast.AbstractBlast;
import blast.blast.BlastHelper;
import blast.blast.nucleotide.BlastN;
import blast.output.BlastOutput;
import blast.output.Iteration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static blast.blast.BlastHelper.*;

/**
 * Created by alext on 5/28/14.
 * TODO document class
 */
public class GBlastN extends BlastN<Iteration> {

    protected final List<String> command;

    protected GBlastN(BlastNBuilder builder) {
        super();
        this.command = builder.getCommand();
    }

    @Override
    public Optional<BlastOutput> call() throws IOException, XMLStreamException, JAXBException {
        final ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        final Process p = processBuilder.start();
        try (InputStream inputStream = p.getInputStream();
             InputStream errorStream = p.getErrorStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream))) {

            final XMLInputFactory xif = XMLInputFactory.newFactory();
            final StreamSource xml = new StreamSource(inputStream);
            final XMLStreamReader xsr = xif.createXMLStreamReader(xml);
            final JAXBContext jc = JAXBContext.newInstance(Iteration.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();

            while(xsr.hasNext()) {
                if(xsr.isStartElement() && xsr.getLocalName().equals("Iteration")) {
                    final JAXBElement<Iteration> jb = unmarshaller.unmarshal(xsr, Iteration.class);
                    final Iteration iteration = jb.getValue();
                    this.notifyListeners(new BlastEvent<>(iteration));
                }
                xsr.next();
            }
            bufferedReader.lines().forEach(l->System.out.println("BLAST ERR:>".concat(l)));
        }

        return Optional.empty();
    }

    public static class BlastNBuilder extends BlastBuilder {

        public BlastNBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);

        }

        @Override
        public GBlastN build() {
            return new GBlastN(this);
        }

        public List<String> getCommand() {
            this.optionalParams.put(OUTFMT, BlastHelper.OUTFMT_VALS.XML.toString());
            final List<String> command = new ArrayList<>();
            command.add(this.pathToBlast.toFile().getPath());
            command.add(QUERY);
            command.add(this.queryFile.toFile().getPath());
            command.add(DB);
            command.add(this.database);
            this.optionalParams.entrySet().stream().forEach(e -> {
                command.add(e.getKey());
                command.add(e.getValue());
            });

            return command;
        }
    }

    @Override
    public synchronized int addListener(BlastEventListner<Iteration> listner){
        this.listeners.add(listner);
        return this.listeners.size();
    }

    @Override
    public synchronized int removeListener(BlastEventListner<Iteration> listner) {
        this.listeners.remove(listner);
        return 0;
    }

    @Override
    public synchronized int notifyListeners(BlastEvent<Iteration> event) {
        return this.listeners.stream().mapToInt((l)->l.listen(event)).sum();
    }
}
