package cat.uab.crossword.model;

import cat.uab.crossword.exception.CrosswordFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Crossword {

    /**
    * An aux class for catch the restrictions
    */
    private class PairWordPos{
        public Word word;
        public int pos;

        public PairWordPos(Word word, int pos) {
            this.word = word;
            this.pos = pos;
        }
    }

    //Singleton instance
    private static Crossword instance = null;

    //Const
    private final static short BLACK = -1;

    private ArrayList<String> lines;
    private File file;
    private short matrix[][];
    private ArrayList<Word> words;
    private PairWordPos auxRestrictionsMatrix[][];

    public short[][] getMatrix() {
        return matrix;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    /**
     * Getter instance crossword
     * @return Crossword
     */
    public static Crossword getCrossword(){
        return instance;
    }

    /**
     * Load and fill matrix of crossword from file
     * @param file
     * @return Instance of crossword
     */
    public static Crossword loadCrossword(File file){

        if(instance == null)
            instance = new Crossword();

        instance.lines = new ArrayList<>();
        instance.words = new ArrayList<>();
        instance.file = file;

        try {
            instance.loadFile();
        } catch (CrosswordFileException e) {
            e.printStackTrace();
        }
        instance.loadMatrix();
        instance.loadWords();

        return instance;
    }

    private void loadWords(){

        int i, j;
        int rowCount;
        int colCount;

        rowCount = this.getRowHeight();
        colCount = this.getColWidth();
        auxRestrictionsMatrix = new PairWordPos[rowCount][colCount];
        for (i = 0; i<rowCount;i++){
            for(j=0;j<colCount;j++)
                auxRestrictionsMatrix[i][j] = new PairWordPos(null, -1);
        }
        for(i = 0; i < rowCount; i++){

            for(j = 0; j < colCount; j++){
                if(this.matrix[i][j] >0){
                //if(this.matrix[i][j] >= '1' && this.matrix[i][j] <= '9'){

                    if((j-1 == -1 || this.matrix[i][j-1] == BLACK) && ((j+1<colCount) && this.matrix[i][j+1] != BLACK)) {
                        Word wordToAdd = new Word(matrix[i][j], Word.HORIZONTAL);
                        int x = j;
                        int pos = 0;
                        while(x<colCount && matrix[i][x] != BLACK){
                            if(auxRestrictionsMatrix[i][x].word == null){
                                auxRestrictionsMatrix[i][x].word = wordToAdd;
                                auxRestrictionsMatrix[i][x].pos = pos;
                            } else {
                                wordToAdd.AddRestriction(new Restriction(pos, auxRestrictionsMatrix[i][x].word, auxRestrictionsMatrix[i][x].pos));
                                auxRestrictionsMatrix[i][x].word.AddRestriction(new Restriction(auxRestrictionsMatrix[i][x].pos, wordToAdd, pos));
                            }
                            pos++;
                            x++;
                        }
                        wordToAdd.setLength(pos);
                        this.words.add(wordToAdd);
                    }
                    if((i-1 == -1 || this.matrix[i-1][j] == BLACK) && ((i+1<rowCount) && this.matrix[i+1][j] != BLACK)) {
                        Word wordToAdd = new Word(matrix[i][j], Word.VERTICAL);
                        int y = i;
                        int pos = 0;
                        while (y < rowCount && matrix[y][j] != BLACK) {
                            if (auxRestrictionsMatrix[y][j].word == null) {
                                auxRestrictionsMatrix[y][j].word = wordToAdd;
                                auxRestrictionsMatrix[y][j].pos = pos;
                            } else {
                                wordToAdd.AddRestriction(new Restriction(pos, auxRestrictionsMatrix[y][j].word, auxRestrictionsMatrix[y][j].pos));
                                auxRestrictionsMatrix[y][j].word.AddRestriction(new Restriction(auxRestrictionsMatrix[y][j].pos, wordToAdd, pos));
                            }
                            pos++;
                            y++;
                        }
                        wordToAdd.setLength(pos);
                        this.words.add(wordToAdd);
                    }
                }
            }
        }
    }

    public char [][] getStateOfCrossword(){
        char [][] result = new char [Crossword.getCrossword().getMatrix().length][Crossword.getCrossword().getMatrix()[0].length];
        for (int i = 0; i< auxRestrictionsMatrix.length;i++){
            for(int j = 0;j<auxRestrictionsMatrix[0].length;j++){
                if(auxRestrictionsMatrix[i][j].word == null)
                    result [i][j] = '#';
                else
                    result [i][j] = auxRestrictionsMatrix[i][j].word.getWordAssigned().toCharArray()[auxRestrictionsMatrix[i][j].pos];
            }
        }
        return result;
    }

    /**
     * Fill matrix with file data
     */
    private void loadMatrix(){

        int i, j;
        int rowCount;
        int colCount;
        String line[];

        rowCount = this.getRowHeight();
        colCount = this.getColWidth();

        this.matrix = new short[rowCount][colCount];

        for(i = 0; i < rowCount; i++){

            line = this.lines.get(i).split("\t");

            for(j = 0; j < colCount; j++)
                //In this mode, we can save id's greaters than 9
                   this.matrix[i][j] = line[j].toCharArray()[0] == '#'? -1 : Short.parseShort(line[j]);
        }
    }

    /**
     * Return the number of columns
     * @return int
     */
    public int getColWidth(){
        return this.lines.get(0).split("\t").length;
    }

    /**
     * Return the number of rows
     * @return int
     */
    public int getRowHeight(){
        return this.lines.size();
    }

    /**
     * Load file data on crossword structure.
     * @throws CrosswordFileException
     */
    private void loadFile() throws CrosswordFileException {

        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            while ((line = reader.readLine()) != null) {
                this.lines.add(line);
            }

        } catch (Exception e) {
            throw new CrosswordFileException(this.file);
        }
    }

}
