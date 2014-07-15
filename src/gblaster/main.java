package gblaster;

import alphabet.character.amino.AminoAcid;
import alphabet.nucleotide.NucleotideAlphabet;
import alphabet.translate.GeneticCode;
import analisys.bbh.BidirectionalBlastHit;
import base.buffer.IterationBlockingBuffer;
import base.progress.Progress;
import blast.blast.AbstractBlast;
import blast.blast.BlastHelper;
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
import java.util.stream.Stream;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class main {

    final static File propertiesFile = new File("/home/alext/Developer/gBLASTer/src/properties/driver.xml");
    final static Path home = Paths.get("/home/alext/Documents/gBlaster");
    final static Path tmpFolder = home.resolve("tmp");
    final static Path orfFolder = home.resolve("orfs");
    final static Path bbhFolder = home.resolve("bbh");
    final static Path blastdbFolder = home.resolve("blastdb");
    final static Path toMakeBlastDb = Paths.get("/bin/makeblastdb");
    final static Path toBlastP = Paths.get("/bin/blastp");
    final static int maxThreads = 12;
    final static ExecutorService blastExecutorService = Executors.newFixedThreadPool(maxThreads);
    final static ExecutorService helperExecutorService = Executors.newCachedThreadPool();
    final static ExecutorService blastDriverExecutorService = Executors.newCachedThreadPool();
    final static int orfUnloadBalancer = Integer.MIN_VALUE;
    final static int orfBatchSize = 1000;
    final static int blastBufferSize = 1000;
    final static int blastThreadsPerRun = 1;
    final static int largeChromosomeBatchSize = 1;
    final static int minimumOrfLength = 50;
    static int countDown;

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
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost/gblaster", "gblaster", "gblaster");

            mySQLConnector.connectToDatabase();
            ((GMySQLConnector) mySQLConnector).setNumberOfConnections(maxThreads);
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
            final int minORFLenght = Integer.valueOf(gBlasterProperties.getBlastProperties().getMinORFLength().getMin());
            final int maxORFLenght = Integer.valueOf(gBlasterProperties.getBlastProperties().getMaxORFLength().getMax());
            try {
                gBlasterProperties.getGenome().stream().forEach(g -> {

                    try {
                        if (!genomeDAO.genomeForNameExists(g.getName().getName())) {
                            System.out.println("Deploying Genome ".concat(g.getName().getName()));
                            final IntStream chromosomeIdStream = Deployer.deployAndGetchromosomeIds(genomeDAO, g, largeFormat, tmpFolder, nucleotideAlphabet, largeChromosomeBatchSize);
                            System.out.println("Translating ORFs for Genome ".concat(g.getName().getName()));
                            Deployer.translateAndGetORFStreamForGenomeId(chromosomeIdStream, genomeDAO, orfDAO, genomeGeneticCodeMap.get(g), largeFormat, orfBatchSize, minimumOrfLength);
                        } else {
                            System.out.println("Genome " + g.getName().getName() + " has already been processed.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });


                //8.For each genome unload ORFs

                final Map<Genome, File> orfFileMap = new HashMap<>();
                gBlasterProperties.getGenome().stream().forEach(g -> {
                    try {
                        if (!orfFolder.resolve(g.getName().getName()).toFile().exists()) {
                            System.out.println("Unloading ORFs for Genome ".concat(g.getName().getName()));
                            orfFileMap.put(g, Deployer.unloadORFsForGenomeToFile(g.getName().getName(), orfDAO, genomeDAO, largeFormat, minORFLenght, maxORFLenght, orfFolder, orfUnloadBalancer));
                        } else {
                            System.out.println("ORFs for Genome ".concat(g.getName().getName()) + " have already been unloaded");
                            orfFileMap.put(g, orfFolder.resolve(g.getName().getName()).toFile());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                //9.Deploy all blastdbs for all genomes
                final List<Future<Optional<File>>> makeBlastDbFutures = gBlasterProperties.getGenome().stream()
                        .filter(g -> {
                            return !blastdbFolder.resolve(g.getName().getName().concat(".phr")).toFile().exists();
                        })
                        .map(g -> {

                            final MakeBlastDB.MakeBlastDBBuilder makeBlastDBBuilder = new MakeBlastDB.MakeBlastDBBuilder(g.getName().getName());
                            System.out.println("Deploying blast database for Genome ".concat(g.getName().getName()));
                            final MakeBlastDB makeBlastDb = makeBlastDBBuilder
                                    .pathToMakeBlastDb(toMakeBlastDb)
                                    .pathToDbFolder(blastdbFolder)
                                    .pathToSequenceFile(orfFileMap.get(g).toPath())
                                    .type(MakeBlastDB.DBType.PROT)
                                    .build();
                            return blastDriverExecutorService.submit(makeBlastDb);
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

            //10. Form pairs of genomes to blast against each other
            final Genome[][] pairs = matchPairs(gBlasterProperties.getGenome(), orfDAO);
            System.out.println("Blasts to run: " + pairs.length);
            countDown = pairs.length;

            //11. Create and submit all blasts
            final List<Callable<Object>> preparedBlasts = new ArrayList<>();

            for (Genome[] pair : pairs) {
                if (!blastDAO.genomeHasBeenBlastedOver(pair[0], pair[1])) {
                    preparedBlasts.add(wrapInCallable(pair, orfDAO, blastDAO, blastBufferSize, gBlasterProperties.getBlastProperties(), blastThreadsPerRun));
                } else {
                    System.out.println("Genome " + pair[0].getName().getName() + " has already been blasted over " + pair[1].getName().getName() + ".");
                    countDown--;
                }

            }
            // mySQLConnector.getConnection().setAutoCommit(true);
            final List<Future<Object>> blastFutures = new ArrayList<>();
            for (Callable<Object> co : preparedBlasts) {
                blastFutures.add(blastDriverExecutorService.submit(co));
            }
            for (Future<Object> future : blastFutures) {
                future.get();
            }

            //Shutdown services
            blastExecutorService.shutdown();
            blastDriverExecutorService.shutdown();
            helperExecutorService.shutdown();

            //Unload BBHs

            for(int i=0;i<gBlasterProperties.getGenome().size();i++){
                for(int j=i+1;j<gBlasterProperties.getGenome().size();j++){
                    System.out.println("Unloading pair: "+gBlasterProperties.getGenome().get(i).getName().getName()+" <-> "+gBlasterProperties.getGenome().get(j).getName().getName());
                    unloadBBHForGenomePair(
                            gBlasterProperties.getGenome().get(i),
                            gBlasterProperties.getGenome().get(j),
                            orfUnloadBalancer,blastDAO,bbhFolder
                    );
                }
            }


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
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static Callable<Object> wrapInCallable(Genome[] pair, OrfDAO orfDAO, BlastDAO blastDAO, int blastBufferSize, BlastProperties blastProperties, int maxThreadsOnBlast) {
        final Callable<Object> pairRun = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                pairBlast(pair[0], pair[1], blastBufferSize, orfDAO, blastDAO, blastProperties, maxThreadsOnBlast);
                synchronized (System.out.getClass()) {
                    System.out.println("Blasts to run: " + --countDown);
                }
                return new Object();
            }
        };
        return pairRun;
    }

    public static Genome[][] matchPairs(List<? extends Genome> genomes, OrfDAO orfDAO) throws Exception {
        final Genome[][] pairs = new Genome[genomes.size() * genomes.size() - genomes.size()][2];
        int number = 0;
        for (int i = 0; i < genomes.size(); i++) {
            for (int j = 0; j < genomes.size(); j++) {
                if (i != j) {
                    pairs[number][0] = genomes.get(i);
                    pairs[number++][1] = genomes.get(j);
                }
            }
        }
        List<Genome[]> toSort = Arrays.asList(pairs);
        final Set<Genome[]> genomeSet = toSort.stream().collect(Collectors.toSet());
        final Map<Genome[], Integer> genomeMap = new HashMap<>();
        genomeSet.stream().forEach(gen -> {
            try {
                final int num = (int) (orfDAO.reportORFBaseSize(gen[0]) + orfDAO.reportORFBaseSize(gen[1]));
                genomeMap.put(gen, num);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            toSort = toSort.stream().sorted(new Comparator<Genome[]>() {
                @Override
                public int compare(Genome[] o1, Genome[] o2) {

                    final int first = genomeMap.get(o1);
                    final int second = genomeMap.get(o2);
                    if (first == second) {
                        return 0;
                    }
                    if (first > second) {
                        return -1;
                    }
                    return 1;

                }
            }).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw (Exception) e.getCause();
        }
        return toSort.toArray(pairs);
    }

    public static void pairBlast(Genome query, Genome target, int bufferCapasity, OrfDAO orfDAO, BlastDAO blastDAO, BlastProperties blastProperties, int numThreads) throws Exception {

        final Path queryFile = orfFolder.resolve(query.getName().getName());
        final Path db = blastdbFolder.resolve(target.getName().getName());

        final GBlast.GBlastPBuilder gBlastPBuilder = new GBlast.GBlastPBuilder(toBlastP, queryFile, db.toFile().getPath());
        final double evalue = Double.parseDouble(blastProperties.getExpect().getValue());
        final GBlast gBlast = gBlastPBuilder.evalue(Optional.of(evalue)).num_threads(Optional.of(numThreads)).maxTargetSeqs(Optional.of(1)).build();

        final IterationBlockingBuffer buffer = IterationBlockingBuffer.get(query.getName().getName() + " <-> " + target.getName().getName(), bufferCapasity);
        gBlast.addListener(buffer);

        final long iterationsToGo = orfDAO.calculateOrfsForGenomeName(query.getName().getName()
                , Integer.parseInt(blastProperties.getMinORFLength().getMin())
                , Integer.parseInt(blastProperties.getMaxORFLength().getMax()));
        final long logFrequency;
        if (iterationsToGo < 10) {
            logFrequency = 5;
        } else {
            logFrequency = iterationsToGo / 10;
        }

        gBlast.addListener(new AbstractBlast.BlastEventListner<Iteration>() {
            long count = 0;

            @Override
            public int listen(AbstractBlast.BlastEvent<Iteration> event) throws Exception {
                count++;
                if (count % logFrequency == 0) {
                    Progress.updateProgressCuncurrent(query.getName().getName().concat("<->").concat(target.getName().getName()), ((double) count / iterationsToGo) * 100);
                }
                return 0;
            }
        });

        final Future<Optional<BlastOutput>> blastFuture = blastExecutorService.submit(gBlast);

        final Callable<Integer> saver = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int blastsSaved = 0;
                int totalBlasts = 0;
                List<Iteration> iterationsToSave = new ArrayList<>(blastBufferSize);
                while (!buffer.isDone()) {

                    final Iteration iteration = buffer.take();
                    if (iteration == IterationBlockingBuffer.DONE) {
                        blastsSaved += iterationsToSave.size();

                        blastDAO.saveBlastResultBatch(iterationsToSave.stream());
                        break;
                    } else if (iteration.getIterationHits().getHit() != null && !iteration.getIterationHits().getHit().isEmpty()) {
                        iterationsToSave.add(iteration);
                        if (iterationsToSave.size() == blastBufferSize) {
                            synchronized (System.out.getClass()) {
                                System.out.println("Saving results for " + query.getName().getName() + " -> " + target.getName().getName());

                                blastDAO.saveBlastResultBatch(iterationsToSave.stream());

                                System.out.println("Results saved for " + query.getName().getName() + " -> " + target.getName().getName());
                            }
                            blastsSaved += iterationsToSave.size();
                            iterationsToSave.clear();
                        }
                    }
                    totalBlasts++;
                }
                synchronized (System.out) {
                    System.out.println("Total number of blasts: " + totalBlasts + " done for " + query.getName().getName() + " <->" + target.getName().getName());
                }
                return blastsSaved;
            }
        };


        final Future<Integer> saverFuture = helperExecutorService.submit(saver);

        blastFuture.get();
        buffer.release();
        synchronized (System.out.getClass()) {
            System.out.println("Buffer for ".concat(query.getName().getName()).concat(" was released."));
        }
        synchronized (System.out.getClass()) {
            System.out.println("Blast results saved to database: " + saverFuture.get());
        }
    }

    public static Path unloadBBHForGenomePair(Genome one, Genome two, int balancer, BlastDAO blastDAO, Path folder) throws Exception {

        final Stream<BidirectionalBlastHit> blastHitStream = blastDAO.getBBHforGenomePair(one, two, balancer);
        final String unloadFileName = one.getName().getName().concat("_VS_").concat(two.getName().getName());
        final Path toOutput = folder.resolve(unloadFileName);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(toOutput.toFile()))) {

            final StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("FWD_BLAST_ID\t");
            stringBuilder.append("FWD_ORF_ID\t");
            stringBuilder.append("FWD_HITORF_ID\t");
            stringBuilder.append("FWD_ITERATION\t");
            stringBuilder.append("RWD_BLAST_ID\t");
            stringBuilder.append("RWD_ORF_ID\t");
            stringBuilder.append("RWD_HITORF_ID\t");
            stringBuilder.append("RWD_ITERATION");
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.newLine();

            blastHitStream.forEach(
                    bbh -> {
                        try {

                            bufferedWriter.write(String.valueOf(bbh.getForwardHit().getId_blasts()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(String.valueOf(bbh.getForwardHit().getOrfs_id()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(String.valueOf(bbh.getForwardHit().getHitorf_id()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(BlastHelper.marshallIterationToString(bbh.getForwardHit().getIteration()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(String.valueOf(bbh.getReverseHit().getId_blasts()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(String.valueOf(bbh.getReverseHit().getOrfs_id()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(String.valueOf(bbh.getReverseHit().getHitorf_id()));
                            bufferedWriter.write("\t");
                            bufferedWriter.write(BlastHelper.marshallIterationToString(bbh.getReverseHit().getIteration()));
                            bufferedWriter.newLine();

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        return toOutput;
    }
}
