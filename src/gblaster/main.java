package gblaster;

import alphabet.character.amino.AminoAcid;
import alphabet.nucleotide.NucleotideAlphabet;
import alphabet.translate.GeneticCode;
import base.buffer.IterationBlockingBuffer;
import blast.db.MakeBlastDB;
import blast.output.BlastOutput;
import blast.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import db.mysql.GMySQLConnector;
import db.mysql.MySQLConnector;
import format.text.CommonFormats;
import format.text.LargeFormat;
import gblaster.blast.GBlast;
import gblaster.deploy.Deployer;
import org.xml.sax.SAXException;
import properties.PropertiesLoader;
import properties.jaxb.BlastProperties;
import properties.jaxb.GBlasterProperties;
import properties.jaxb.Genome;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class main {

    final static File propertiesFile = new File("/home/alext/Developer/gBLASTer/src/properties/driver.xml");
    final static Path home = Paths.get("/home/alext/Documents/gBlaster");
    final static Path tmpFolder = home.resolve("tmp");
    final static Path orfFolder = home.resolve("orfs");
    final static Path blastdbFolder = home.resolve("blastdb");
    final static Path toMakeBlastDb = Paths.get("/bin/makeblastdb");
    final static Path toBlastP = Paths.get("/bin/blastp");
    final static int maxThreads = 12;
    final static int orfUnloadBalancer = Integer.MIN_VALUE;
    final static int orfBatchSize = 1000;
    final static int largeChromosomeBatchSize = 1;
    final static ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

    /**
     * So far this is just a runscript
     *
     * @param args
     */
    public static void main(String[] args) {

        //Load properties and map folders
        final GBlasterProperties gBlasterProperties;


        try (InputStream inputStream = new FileInputStream(propertiesFile)) {

            //1.Load
            gBlasterProperties = PropertiesLoader.load(inputStream);

            //2.Create a map of genomes and their corresponding genetic codes
            final Map<Genome, GeneticCode<AminoAcid>> genomeGeneticCodeMap = Deployer.mapGenomesToGeneticCode(gBlasterProperties.getGenome().stream());

            //3.Connect to database
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");

            mySQLConnector.connectToDatabase();
            mySQLConnector.getConnection().setAutoCommit(false);


            //4.Define DAOs
            final GenomeDAO genomeDAO = (GenomeDAO) mySQLConnector;
            final OrfDAO orfDAO = (OrfDAO) mySQLConnector;
            final BlastDAO blastDAO = (BlastDAO) mySQLConnector;

            //5.Define large format
            final LargeFormat largeFormat = CommonFormats.LARGE_FASTA;

            //6.Define nucleotide alphabet
            final NucleotideAlphabet nucleotideAlphabet = NucleotideAlphabet.get();

            //7.For each genome: deploy and translate
            final int minORFLenght=Integer.valueOf(gBlasterProperties.getBlastProperties().getMinORFLength().getMin());
            final int maxORFLenght=Integer.valueOf(gBlasterProperties.getBlastProperties().getMaxORFLength().getMax());
            try {
                gBlasterProperties.getGenome().stream().forEach(g -> {

                    try {
                        if(!genomeDAO.genomeForNameExists(g.getName().getName())) {
                            System.out.println("Deploying Genome ".concat(g.getName().getName()));
                            final IntStream chromosomeIdStream = Deployer.deployAndGetchromosomeIds(genomeDAO, g, largeFormat, tmpFolder, nucleotideAlphabet, largeChromosomeBatchSize);
                            System.out.println("Translating ORFs for Genome ".concat(g.getName().getName()));
                            Deployer.translateAndGetORFStreamForGenomeId(chromosomeIdStream, genomeDAO, orfDAO, genomeGeneticCodeMap.get(g), largeFormat, orfBatchSize);
                        }else{
                            System.out.println("Genome "+g.getName().getName()+" has already been processed.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                //If this commit has taken place, that means, that the genome, its chromosomes and the orfs for this genome have been processed
                mySQLConnector.getConnection().commit();

                //8.For each genome unload ORFs
                final Map<Genome, File> orfFileMap = new HashMap<>();
                gBlasterProperties.getGenome().stream().forEach(g -> {
                    try {
                        System.out.println("Unloading ORFs for Genome ".concat(g.getName().getName()));
                        orfFileMap.put(g, Deployer.unloadORFsForGenomeToFile(g.getName().getName(), orfDAO, genomeDAO, largeFormat,minORFLenght,maxORFLenght, orfFolder,orfUnloadBalancer));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                //9.Deploy all bastdbs for all genomes
                final List<Future<Optional<File>>> makeBlastDbFutures = gBlasterProperties.getGenome().stream().map(g -> {
                    final MakeBlastDB.MakeBlastDBBuilder makeBlastDBBuilder = new MakeBlastDB.MakeBlastDBBuilder(g.getName().getName());
                    System.out.println("Deploying blast database for Genome ".concat(g.getName().getName()));
                    final MakeBlastDB makeBlastDb = makeBlastDBBuilder
                            .pathToMakeBlastDb(toMakeBlastDb)
                            .pathToDbFolder(blastdbFolder)
                            .pathToSequenceFile(orfFileMap.get(g).toPath())
                            .type(MakeBlastDB.DBType.PROT)
                            .build();
                    return executorService.submit(makeBlastDb);
                }).collect(Collectors.toList());
                makeBlastDbFutures.stream().forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else throw e;
            }

            final Callable<Object> fwdRun = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    pairBlast(gBlasterProperties.getGenome().get(0), gBlasterProperties.getGenome().get(1), 20, blastDAO, gBlasterProperties.getBlastProperties(),maxThreads/2);
                    mySQLConnector.getConnection().commit();
                    return new Object();
                }
            };
            final Callable<Object> rwdRun = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    pairBlast(gBlasterProperties.getGenome().get(1), gBlasterProperties.getGenome().get(0), 20, blastDAO, gBlasterProperties.getBlastProperties(),maxThreads/2);
                    mySQLConnector.getConnection().commit();
                    return new Object();
                }
            };
            final List<Future<Object>> futures=new ArrayList<>();
            futures.add(executorService.submit(fwdRun));
            futures.add(executorService.submit(rwdRun));
            for(Future<Object>f:futures){
                f.get();
            }
            //Shutdown
            executorService.shutdown();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }


    public static Genome[][] matchPairs(List<? extends Genome> genomes){
        final Genome[][]pairs=new Genome[genomes.size()^2-genomes.size()][2];
        int number=0;
        for(int i=0;i<genomes.size();i++){
            for(int j=0;j<genomes.size();j++){
                if(i!=j){
                   pairs[number][0]=genomes.get(i);
                   pairs[number++][1]=genomes.get(j);
                }
            }
        }
        return pairs;
    }

    public static void pairBlast(Genome query, Genome base, int bufferCapasity, BlastDAO blastDAO, BlastProperties blastProperties, int numThreads) throws ExecutionException, InterruptedException {

        final Path queryFile = orfFolder.resolve(query.getName().getName());
        final Path db = blastdbFolder.resolve(base.getName().getName());

        final GBlast.GBlastPBuilder gBlastPBuilder = new GBlast.GBlastPBuilder(toBlastP, queryFile, db.toFile().getPath());
        final double evalue = Double.parseDouble(blastProperties.getExpect().getValue());
        final GBlast gBlast = gBlastPBuilder.evalue(Optional.of(evalue)).num_threads(Optional.of(numThreads)).maxTargetSeqs(Optional.of(1)).build();

        final IterationBlockingBuffer buffer = IterationBlockingBuffer.get(bufferCapasity);
        gBlast.addListener(buffer);
        final Future<Optional<BlastOutput>> blastFuture = executorService.submit(gBlast);
        final Callable<Integer> saver = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int blastsSaved = 0;
                while (!buffer.isDone()) {

                    final Iteration iteration = buffer.take();
                    if(iteration==IterationBlockingBuffer.DONE){
                        break;
                    }else if (iteration.getIterationHits().getHit() != null && !iteration.getIterationHits().getHit().isEmpty()) {
                        blastDAO.saveBlastResult(iteration);
                        blastsSaved++;
                    }
                }

                return blastsSaved;
            }
        };


        final Future<Integer> saverFuture = executorService.submit(saver);

        blastFuture.get();
        buffer.release();
        System.out.println("Buffer for ".concat(query.getName().getName()).concat(" was released."));

        System.out.println("Blast results saved to database: " + saverFuture.get());
    }
}
