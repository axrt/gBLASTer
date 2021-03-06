package gblaster.blast;

import blast.blast.AbstractBlast;
import blast.ncbi.output.BlastOutput;
import blast.ncbi.output.Iteration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class GBlast extends AbstractBlast<Iteration> {

    protected final List<String> command;

    protected GBlast(BlastBuilder builder) {
        super();
        this.command = builder.getCommand();
    }

    @Override
    public Optional<BlastOutput> call() throws Exception {
        final ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        final Process p = processBuilder.start();
        try (InputStream inputStream = p.getInputStream();
             InputStream errorStream = p.getErrorStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream))) {

            this.process(inputStream);

            bufferedReader.lines().forEach(l -> {
                synchronized (System.out.getClass()) {
                    System.out.println("BLAST ERR:> ".concat(l));
                }
            });

            return Optional.empty();
        }
    }

    public void process(InputStream inputStream) throws Exception {

        final XMLInputFactory xif = XMLInputFactory.newFactory();
        final StreamSource xml = new StreamSource(inputStream);
        final XMLStreamReader xsr = xif.createXMLStreamReader(xml);
        final JAXBContext jc = JAXBContext.newInstance(Iteration.class);
        final Unmarshaller unmarshaller = jc.createUnmarshaller();

        while (xsr.hasNext()) {
            if (xsr.isStartElement() && xsr.getLocalName().equals("Iteration")) {
                final JAXBElement<Iteration> jb = unmarshaller.unmarshal(xsr, Iteration.class);
                final Iteration iteration = jb.getValue();
                this.notifyListeners(new AbstractBlast.BlastEvent<>(iteration));
            }
            xsr.next();
        }
    }

    @Override
    public synchronized int addListener(BlastEventListner<Iteration> listner) {
        this.listeners.add(listner);
        return this.listeners.size();
    }

    @Override
    public synchronized int removeListener(BlastEventListner<Iteration> listner) {
        this.listeners.remove(listner);
        return 0;
    }

    @Override
    public synchronized int notifyListeners(BlastEvent<Iteration> event) throws Exception {
        try {
            return this.listeners.stream().mapToInt((l) -> {
                try {
                    return l.listen(event);
                } catch (Exception ie) {
                    throw new RuntimeException(ie);
                }
            }).sum();
        } catch (RuntimeException e) {
            throw (Exception) e.getCause();
        }
    }

    public static class GBlastPBuilder extends BlastPBuilder<Iteration, GBlast> {
        public GBlastPBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }

        @Override
        public GBlast build() {
            return new GBlast(this);
        }
    }

    public static class GBlastNBuilder extends BlastNBuilder<Iteration, GBlast> {
        public GBlastNBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }

        @Override
        public GBlast build() {
            return new GBlast(this);
        }
    }
}
